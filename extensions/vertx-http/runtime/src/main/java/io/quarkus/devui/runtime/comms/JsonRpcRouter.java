package io.quarkus.devui.runtime.comms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Flow;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcCodec;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethodName;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRequest;
import io.quarkus.devui.runtime.jsonrpc.json.JsonMapper;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.StartupEvent;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;

/**
 * Route JsonRPC message to the correct method
 */
public class JsonRpcRouter {

    private final Map<Integer, Cancellable> subscriptions = new ConcurrentHashMap<>();

    // Map json-rpc method to java in runtime classpath
    private final Map<String, ReflectionInfo> jsonRpcToRuntimeClassPathJava = new HashMap<>();

    // Map json-rpc method to java in deployment classpath
    private final List<String> jsonRpcMethodToDeploymentClassPathJava = new ArrayList<>();
    // Map json-rpc subscriptions to java in deployment classpath
    private final List<String> jsonRpcSubscriptionToDeploymentClassPathJava = new ArrayList<>();
    // Map json-rpc methods responses that is recorded
    private final Map<String, RuntimeValue> recordedValues = new HashMap<>();

    private static final List<JsonRpcResponseWriter> SESSIONS = Collections.synchronizedList(new ArrayList<>());

    private JsonRpcCodec codec;

    /**
     * This gets called on build to build into of the classes we are going to call in runtime
     *
     * @param extensionMethodsMap
     */
    public void populateJsonRPCRuntimeMethods(Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap) {
        for (Map.Entry<String, Map<JsonRpcMethodName, JsonRpcMethod>> extension : extensionMethodsMap.entrySet()) {
            String extensionName = extension.getKey();
            Map<JsonRpcMethodName, JsonRpcMethod> jsonRpcMethods = extension.getValue();
            for (Map.Entry<JsonRpcMethodName, JsonRpcMethod> method : jsonRpcMethods.entrySet()) {
                JsonRpcMethodName methodName = method.getKey();
                JsonRpcMethod jsonRpcMethod = method.getValue();

                @SuppressWarnings("unchecked")
                Object providerInstance = Arc.container().select(jsonRpcMethod.getClazz()).get();

                try {
                    Method javaMethod;
                    Map<String, Class> params = null;
                    if (jsonRpcMethod.hasParams()) {
                        params = jsonRpcMethod.getParams();
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName(),
                                params.values().toArray(new Class[] {}));
                    } else {
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName());
                    }
                    ReflectionInfo reflectionInfo = new ReflectionInfo(jsonRpcMethod.getClazz(), providerInstance, javaMethod,
                            params, jsonRpcMethod.getExplicitlyBlocking(), jsonRpcMethod.getExplicitlyNonBlocking());
                    String jsonRpcMethodName = extensionName + SLASH + methodName;
                    jsonRpcToRuntimeClassPathJava.put(jsonRpcMethodName, reflectionInfo);
                } catch (NoSuchMethodException | SecurityException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void setJsonRPCDeploymentActions(List<String> methods, List<String> subscriptions) {
        this.jsonRpcMethodToDeploymentClassPathJava.clear();
        this.jsonRpcMethodToDeploymentClassPathJava.addAll(methods);
        this.jsonRpcSubscriptionToDeploymentClassPathJava.clear();
        this.jsonRpcSubscriptionToDeploymentClassPathJava.addAll(subscriptions);
    }

    public void setRecordedValues(Map<String, RuntimeValue> recordedValues) {
        this.recordedValues.clear();
        this.recordedValues.putAll(recordedValues);
    }

    public void initializeCodec(JsonMapper jsonMapper) {
        this.codec = new JsonRpcCodec(jsonMapper);
    }

    public void addSocket(ServerWebSocket socket) {
        WsResponseWriter writer = new WsResponseWriter(socket);
        SESSIONS.add(new WsResponseWriter(socket));
        socket.textMessageHandler((e) -> {
            JsonRpcRequest jsonRpcRequest = codec.readRequest(e);
            route(jsonRpcRequest, writer);
        }).closeHandler((e) -> {
            purge();
        });
        purge();
    }

    public void addSseSession(RoutingContext ctx) {
        SseResponseWriter writer = new SseResponseWriter(ctx.response());
        SESSIONS.add(writer);

        ctx.request().handler(buffer -> {
            JsonRpcRequest jsonRpcRequest = codec.readRequest(buffer.toString());
            route(jsonRpcRequest, writer);
        });

        ctx.request().connection().closeHandler(v -> purge());

        purge();
    }

    public Set<String> getRuntimeMethods() {
        return this.jsonRpcToRuntimeClassPathJava.keySet();
    }

    public Set<String> getDeploymentMethods() {
        return Set.copyOf(jsonRpcMethodToDeploymentClassPathJava);
    }

    public Set<String> getRecordedMethods() {
        return recordedValues.keySet();
    }

    void onStart(@Observes StartupEvent ev) {
        purge();
        for (JsonRpcResponseWriter jrrw : new ArrayList<>(SESSIONS)) {
            if (!jrrw.isClosed()) {
                codec.writeResponse(jrrw, -1, LocalDateTime.now().toString(), MessageType.HotReload);
            }
        }
    }

    private void purge() {
        SESSIONS.removeIf(JsonRpcResponseWriter::isClosed);
    }

    @Inject
    Logger logger;

    @SuppressWarnings("unchecked")
    private void route(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        String jsonRpcMethodName = jsonRpcRequest.getMethod();

        if (jsonRpcMethodName.equalsIgnoreCase(UNSUBSCRIBE)) {
            // This is a Dev UI subscription that terminated
            this.routeUnsubscribe(jsonRpcRequest, jrrw);
        } else if (jsonRpcMethodName.equalsIgnoreCase(INITIALIZE)) {
            // This is a MCP server that connects
            jrrw.write("{\"id\":" + jsonRpcRequest.getId() + ",\"result\":\"ok\"}");
            //codec.writeResponse(jrrw, jsonRpcRequest.getId(), "ok", null);
        } else if (this.jsonRpcToRuntimeClassPathJava.containsKey(jsonRpcMethodName)) {
            // This is a Runtime method that needs to route to the extension
            this.routeToRuntime(jsonRpcRequest, jrrw);
        } else if (this.jsonRpcMethodToDeploymentClassPathJava.contains(jsonRpcMethodName)
                || this.jsonRpcSubscriptionToDeploymentClassPathJava.contains(jsonRpcMethodName)) {
            // This is Deployment method that needs to route to the extension
            this.routeToDeployment(jsonRpcRequest, jrrw);
        } else {
            // This is an error. Method not found
            codec.writeMethodNotFoundResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName);
        }
    }

    private void routeUnsubscribe(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        if (this.subscriptions.containsKey(jsonRpcRequest.getId())) {
            Cancellable cancellable = this.subscriptions.remove(jsonRpcRequest.getId());
            cancellable.cancel();
        }
        codec.writeResponse(jrrw, jsonRpcRequest.getId(), null, MessageType.Void);
    }

    private void routeToRuntime(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        String jsonRpcMethodName = jsonRpcRequest.getMethod();
        ReflectionInfo reflectionInfo = this.jsonRpcToRuntimeClassPathJava.get(jsonRpcMethodName);
        Object target = Arc.container().select(reflectionInfo.bean).get();

        if (reflectionInfo.isReturningMulti()) {
            this.routeToRuntimeSubscription(jsonRpcRequest, jrrw, jsonRpcMethodName, reflectionInfo, target);
        } else {
            // The invocation will return a Uni<JsonObject>
            this.routeToRuntimeMethod(jsonRpcRequest, jrrw, jsonRpcMethodName, reflectionInfo, target);
        }
    }

    private void routeToRuntimeSubscription(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw, String jsonRpcMethodName,
            ReflectionInfo reflectionInfo, Object target) {

        if (this.subscriptions.containsKey(jsonRpcRequest.getId())) {
            // Cancel and resubscribe
            Cancellable cancellable = this.subscriptions.remove(jsonRpcRequest.getId());
            cancellable.cancel();
        }

        Multi<?> multi;
        try {
            if (jsonRpcRequest.hasParams()) {
                Object[] args = getArgsAsObjects(reflectionInfo.params, jsonRpcRequest);
                multi = (Multi<?>) reflectionInfo.method.invoke(target, args);
            } else {
                multi = (Multi<?>) reflectionInfo.method.invoke(target);
            }
        } catch (Exception e) {
            logger.errorf(e, "Unable to invoke method %s using JSON-RPC, request was: %s", jsonRpcMethodName,
                    jsonRpcRequest);
            codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, e);
            return;
        }

        Cancellable cancellable = multi.subscribe()
                .with(
                        item -> {
                            codec.writeResponse(jrrw, jsonRpcRequest.getId(), item, MessageType.SubscriptionMessage);
                        },
                        failure -> {
                            codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, failure);
                            this.subscriptions.remove(jsonRpcRequest.getId());
                        },
                        () -> this.subscriptions.remove(jsonRpcRequest.getId()));

        this.subscriptions.put(jsonRpcRequest.getId(), cancellable);
        codec.writeResponse(jrrw, jsonRpcRequest.getId(), null, MessageType.Void);
    }

    private void routeToRuntimeMethod(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw, String jsonRpcMethodName,
            ReflectionInfo reflectionInfo, Object target) {
        Uni<?> uni;
        try {
            if (jsonRpcRequest.hasParams()) {
                Object[] args = getArgsAsObjects(reflectionInfo.params, jsonRpcRequest);
                uni = invoke(reflectionInfo, target, args);
            } else {
                uni = invoke(reflectionInfo, target, new Object[0]);
            }
        } catch (Exception e) {
            logger.errorf(e, "Unable to invoke method %s using JSON-RPC, request was: %s", jsonRpcMethodName,
                    jsonRpcRequest);
            codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, e);
            return;
        }
        uni.subscribe()
                .with(item -> {
                    if (item != null && JsonRpcMessage.class.isAssignableFrom(item.getClass())) {
                        JsonRpcMessage<?> jsonRpcMessage = (JsonRpcMessage<?>) item;
                        Object response = jsonRpcMessage.getResponse();
                        if (jsonRpcMessage.isAlreadySerialized()) {
                            // The message response was already serialized, write text directly to socket
                            jrrw.write("{\"id\":" + jsonRpcRequest.getId() + ",\"result\":{\"messageType\":\""
                                    + jsonRpcMessage.getMessageType().name() + "\",\"object\":" + response + "}}");
                        } else {
                            codec.writeResponse(jrrw, jsonRpcRequest.getId(), response, jsonRpcMessage.getMessageType());
                        }
                    } else {
                        codec.writeResponse(jrrw, jsonRpcRequest.getId(), item, MessageType.Response);
                    }
                }, failure -> {
                    Throwable actualFailure;
                    // If the jsonrpc method is actually
                    // synchronous, the failure is wrapped in an
                    // InvocationTargetException, so unwrap it here
                    if (failure instanceof InvocationTargetException f) {
                        actualFailure = f.getTargetException();
                    } else if (failure.getCause() != null
                            && failure.getCause() instanceof InvocationTargetException f) {
                        actualFailure = f.getTargetException();
                    } else {
                        actualFailure = failure;
                    }
                    codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, actualFailure);
                });
    }

    private void routeToDeployment(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        String jsonRpcMethodName = jsonRpcRequest.getMethod();

        if (this.subscriptions.containsKey(jsonRpcRequest.getId())) {
            // Cancel and resubscribe
            Cancellable cancellable = this.subscriptions.remove(jsonRpcRequest.getId());
            cancellable.cancel();
        }

        Object returnedObject = null;
        if (this.recordedValues.containsKey(jsonRpcMethodName)) {
            returnedObject = this.recordedValues.get(jsonRpcMethodName).getValue();
        } else {
            returnedObject = DevConsoleManager.invoke(jsonRpcMethodName, getArgsAsMap(jsonRpcRequest));
        }
        if (returnedObject != null) {
            // Support for Mutiny is diffcult because we are between the runtime and deployment classpath.
            // Supporting something like CompletableFuture and Flow.Publisher that is in the JDK works fine
            if (returnedObject instanceof Flow.Publisher) {
                Flow.Publisher<?> publisher = (Flow.Publisher) returnedObject;

                Cancellable cancellable = Multi.createFrom().publisher(publisher).subscribe()
                        .with(
                                item -> {
                                    codec.writeResponse(jrrw, jsonRpcRequest.getId(), item, MessageType.SubscriptionMessage);
                                },
                                failure -> {
                                    codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, failure);
                                    this.subscriptions.remove(jsonRpcRequest.getId());
                                },
                                () -> this.subscriptions.remove(jsonRpcRequest.getId()));

                this.subscriptions.put(jsonRpcRequest.getId(), cancellable);
                codec.writeResponse(jrrw, jsonRpcRequest.getId(), null, MessageType.Void);
            } else if (returnedObject instanceof CompletionStage) {
                CompletionStage<?> future = (CompletionStage) returnedObject;
                future.thenAccept(r -> {
                    codec.writeResponse(jrrw, jsonRpcRequest.getId(), r,
                            MessageType.Response);
                }).exceptionally(throwable -> {
                    codec.writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName, throwable);
                    return null;
                });
            } else {
                codec.writeResponse(jrrw, jsonRpcRequest.getId(), returnedObject,
                        MessageType.Response);
            }
        }
    }

    private Uni<?> invoke(ReflectionInfo info, Object target, Object[] args) {
        if (info.isReturningUni()) {
            try {
                Uni<?> uni = ((Uni<?>) info.method.invoke(target, args));
                if (info.isExplicitlyBlocking()) {
                    return uni.runSubscriptionOn(Infrastructure.getDefaultExecutor());
                } else {
                    return uni;
                }
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        } else {
            Uni<?> uni = Uni.createFrom().item(Unchecked.supplier(() -> info.method.invoke(target, args)));
            if (!info.isExplicitlyNonBlocking()) {
                return uni.runSubscriptionOn(Infrastructure.getDefaultExecutor());
            } else {
                return uni;
            }
        }
    }

    private Object[] getArgsAsObjects(Map<String, Class> params, JsonRpcRequest jsonRpcRequest) {
        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, Class> expectedParams : params.entrySet()) {
            String paramName = expectedParams.getKey();
            Class paramType = expectedParams.getValue();
            Object param = jsonRpcRequest.getParam(paramName, paramType);
            objects.add(param);
        }
        return objects.toArray(Object[]::new);
    }

    private Map<String, String> getArgsAsMap(JsonRpcRequest jsonRpcRequest) {
        if (jsonRpcRequest.hasParams()) {
            return (Map<String, String>) jsonRpcRequest.getParams();
        }
        return Map.of();
    }

    public JsonMapper getJsonMapper() {
        return codec.getJsonMapper();
    }

    private static final String SLASH = "/";
    private static final String UNSUBSCRIBE = "unsubscribe";
    private static final String INITIALIZE = "initialize";

}
