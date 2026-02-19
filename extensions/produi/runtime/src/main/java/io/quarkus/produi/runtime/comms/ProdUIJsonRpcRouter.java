package io.quarkus.produi.runtime.comms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.jboss.logging.Logger;

import io.quarkus.arc.Arc;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcKeys;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcRequest;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcResponse;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.subscription.Cancellable;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Routes JSON-RPC messages to the correct method.
 * <p>
 * Unlike Dev UI's JsonRpcRouter, this only supports runtime methods.
 * There is no deployment classpath access in production.
 */
@Singleton
public class ProdUIJsonRpcRouter {

    private static final String UNSUBSCRIBE = "unsubscribe";

    private final Map<Integer, Cancellable> activeSubscriptions = new ConcurrentHashMap<>();
    private static final List<JsonRpcResponseWriter> SESSIONS = Collections.synchronizedList(new ArrayList<>());

    // Map json-rpc method to java in runtime classpath
    private Map<String, JsonRpcMethod> runtimeMethodsMap = Map.of();
    // Map json-rpc subscriptions to java in runtime classpath
    private Map<String, JsonRpcMethod> runtimeSubscriptionMap = Map.of();

    @Inject
    Logger logger;

    /**
     * Populate the router with runtime JSON-RPC endpoints.
     */
    public void populateJsonRpcEndpoints(Map<String, JsonRpcMethod> runtimeMethods,
            Map<String, JsonRpcMethod> runtimeSubscriptions) {

        this.runtimeMethodsMap = enhanceRuntimeJsonRpcEndpoints(runtimeMethods);
        this.runtimeSubscriptionMap = enhanceRuntimeJsonRpcEndpoints(runtimeSubscriptions);
    }

    public void addSocket(ServerWebSocket socket) {
        ProdUIWebSocketResponseWriter writer = new ProdUIWebSocketResponseWriter(socket);
        SESSIONS.add(writer);
        socket.textMessageHandler((e) -> {
            JsonObject jsonObject = new JsonObject(e);
            JsonRpcRequest jsonRpcRequest = JsonRpcRequest.fromJson(jsonObject);
            route(jsonRpcRequest, writer);
        }).closeHandler((e) -> {
            purge();
        });
        purge();
    }

    private void purge() {
        SESSIONS.removeIf(JsonRpcResponseWriter::isClosed);
    }

    public void route(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        String jsonRpcMethodName = jsonRpcRequest.getMethod();

        if (jsonRpcMethodName.equalsIgnoreCase(UNSUBSCRIBE)) {
            routeToUnsubscribe(jsonRpcRequest, jrrw);
        } else if (this.runtimeMethodsMap.containsKey(jsonRpcMethodName)) {
            routeToRuntimeMethod(jsonRpcRequest, jrrw);
        } else if (this.runtimeSubscriptionMap.containsKey(jsonRpcMethodName)) {
            routeToRuntimeSubscription(jsonRpcRequest, jrrw);
        } else {
            writeMethodNotFoundResponse(jrrw, jsonRpcRequest.getId(), jsonRpcMethodName);
        }
    }

    private void routeToUnsubscribe(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        if (this.activeSubscriptions.containsKey(jsonRpcRequest.getId())) {
            Cancellable cancellable = this.activeSubscriptions.remove(jsonRpcRequest.getId());
            cancellable.cancel();
        }
        writeResponse(jrrw, jsonRpcRequest.getId(), null, MessageType.Void);
    }

    private void routeToRuntimeMethod(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        JsonRpcMethod runtimeJsonRpcMethod = this.runtimeMethodsMap.get(jsonRpcRequest.getMethod());
        Object target = Arc.container().select(runtimeJsonRpcMethod.getBean()).get();
        Uni<?> uni;
        try {
            Object[] args = new Object[0];
            if (jsonRpcRequest.hasParams()) {
                args = getArgsAsObjects(runtimeJsonRpcMethod.getParameters(), jsonRpcRequest);
            }
            uni = invoke(runtimeJsonRpcMethod, target, args);

        } catch (Exception e) {
            logger.errorf(e, "Unable to invoke method %s using JSON-RPC, request was: %s", jsonRpcRequest.getMethod(),
                    jsonRpcRequest);
            writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcRequest.getMethod(), e);
            return;
        }
        uni.subscribe()
                .with(item -> {
                    writeResponse(jrrw, jsonRpcRequest.getId(), item, MessageType.Response);
                }, failure -> {
                    Throwable actualFailure;
                    if (failure instanceof InvocationTargetException f) {
                        actualFailure = f.getTargetException();
                    } else if (failure != null && failure.getCause() != null
                            && failure.getCause() instanceof InvocationTargetException f) {
                        actualFailure = f.getTargetException();
                    } else {
                        actualFailure = failure;
                    }
                    writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcRequest.getMethod(), actualFailure);
                });
    }

    private void routeToRuntimeSubscription(JsonRpcRequest jsonRpcRequest, JsonRpcResponseWriter jrrw) {
        JsonRpcMethod runtimeJsonRpcSubscription = this.runtimeSubscriptionMap.get(jsonRpcRequest.getMethod());
        Object target = Arc.container().select(runtimeJsonRpcSubscription.getBean()).get();

        if (this.activeSubscriptions.containsKey(jsonRpcRequest.getId())) {
            Cancellable cancellable = this.activeSubscriptions.remove(jsonRpcRequest.getId());
            cancellable.cancel();
        }

        Multi<?> multi;
        try {
            if (jsonRpcRequest.hasParams()) {
                Object[] args = getArgsAsObjects(runtimeJsonRpcSubscription.getParameters(), jsonRpcRequest);
                multi = (Multi<?>) runtimeJsonRpcSubscription.getJavaMethod().invoke(target, args);
            } else {
                multi = (Multi<?>) runtimeJsonRpcSubscription.getJavaMethod().invoke(target);
            }
        } catch (Exception e) {
            logger.errorf(e, "Unable to invoke method %s using JSON-RPC, request was: %s", jsonRpcRequest.getMethod(),
                    jsonRpcRequest);
            writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcRequest.getMethod(), e);
            return;
        }

        Cancellable cancellable = multi.subscribe()
                .with(
                        item -> {
                            writeResponse(jrrw, jsonRpcRequest.getId(), item, MessageType.SubscriptionMessage);
                        },
                        failure -> {
                            writeErrorResponse(jrrw, jsonRpcRequest.getId(), jsonRpcRequest.getMethod(), failure);
                            this.activeSubscriptions.remove(jsonRpcRequest.getId());
                        },
                        () -> this.activeSubscriptions.remove(jsonRpcRequest.getId()));

        this.activeSubscriptions.put(jsonRpcRequest.getId(), cancellable);
        writeResponse(jrrw, jsonRpcRequest.getId(), null, MessageType.Void);
    }

    @SuppressWarnings("unchecked")
    private Uni<?> invoke(JsonRpcMethod runtimeJsonRpcMethod, Object target, Object[] args) {
        Uni<?> uni;
        if (runtimeJsonRpcMethod.isReturningUni()) {
            try {
                uni = ((Uni<?>) runtimeJsonRpcMethod.getJavaMethod().invoke(target, args));
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        } else if (runtimeJsonRpcMethod.isReturningCompletableFuture() || runtimeJsonRpcMethod.isReturningCompletionStage()) {
            try {
                uni = Uni.createFrom()
                        .completionStage((CompletionStage<Object>) runtimeJsonRpcMethod.getJavaMethod().invoke(target, args));
            } catch (Exception e) {
                return Uni.createFrom().failure(e);
            }
        } else {
            uni = Uni.createFrom()
                    .item(Unchecked.supplier(() -> runtimeJsonRpcMethod.getJavaMethod().invoke(target, args)));
        }
        if (!runtimeJsonRpcMethod.isExplicitlyNonBlocking()) {
            return uni.runSubscriptionOn(Infrastructure.getDefaultExecutor());
        } else {
            return uni;
        }
    }

    private Object[] getArgsAsObjects(Map<String, JsonRpcMethod.Parameter> parameters, JsonRpcRequest jsonRpcRequest) {
        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, JsonRpcMethod.Parameter> expectedParams : parameters.entrySet()) {
            String paramName = expectedParams.getKey();
            Class<?> paramType = expectedParams.getValue().getType();
            Object param = jsonRpcRequest.getParam(paramName, paramType);
            objects.add(param);
        }
        return objects.toArray(Object[]::new);
    }

    private void writeResponse(JsonRpcResponseWriter writer, int id, Object object, MessageType messageType) {
        Object decoratedObject = writer.decorateObject(object, messageType);
        JsonRpcResponse response = new JsonRpcResponse(id, decoratedObject);
        writer.write(Json.encode(response));
    }

    private void writeMethodNotFoundResponse(JsonRpcResponseWriter writer, int id, String jsonRpcMethodName) {
        JsonRpcResponse response = new JsonRpcResponse(id,
                new JsonRpcResponse.Error(JsonRpcKeys.METHOD_NOT_FOUND, "Method [" + jsonRpcMethodName + "] not found"));
        writer.write(Json.encode(response));
    }

    private void writeErrorResponse(JsonRpcResponseWriter writer, int id, String jsonRpcMethodName, Throwable exception) {
        logger.error("Error in JsonRPC Call", exception);
        JsonRpcResponse response = new JsonRpcResponse(id,
                new JsonRpcResponse.Error(JsonRpcKeys.INTERNAL_ERROR,
                        "Method [" + jsonRpcMethodName + "] failed: " + exception.getMessage()));
        writer.write(Json.encode(response));
    }

    /**
     * Goes through all runtime endpoints and gets the correct Java method.
     */
    private Map<String, JsonRpcMethod> enhanceRuntimeJsonRpcEndpoints(Map<String, JsonRpcMethod> runtimeMethods) {
        for (Map.Entry<String, JsonRpcMethod> method : runtimeMethods.entrySet()) {
            JsonRpcMethod jsonRpcMethod = method.getValue();

            Object providerInstance = Arc.container().select(jsonRpcMethod.getBean()).get();

            try {
                Method javaMethod;
                if (jsonRpcMethod.hasParameters()) {
                    Class<?>[] types = jsonRpcMethod.getParameters().values().stream()
                            .map(JsonRpcMethod.Parameter::getType)
                            .toArray(Class<?>[]::new);
                    javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getJavaMethodName(), types);
                } else {
                    javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getJavaMethodName());
                }
                jsonRpcMethod.setJavaMethod(javaMethod);
            } catch (NoSuchMethodException | SecurityException ex) {
                throw new RuntimeException(ex);
            }
        }
        return runtimeMethods;
    }
}
