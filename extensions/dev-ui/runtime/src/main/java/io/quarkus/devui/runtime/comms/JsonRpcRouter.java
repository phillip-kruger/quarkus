package io.quarkus.devui.runtime.comms;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import io.quarkus.arc.Arc;
import io.quarkus.devui.runtime.jsonrpc.DevUIResponse;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcErrorResponse;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMessage;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethodName;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRequest;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcResponse;
import io.quarkus.devui.runtime.jsonrpc.MessageType;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.Cancellable;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Route JsonRPC message to the correct method
 */
@ApplicationScoped
public class JsonRpcRouter {

    private final Map<Integer, Cancellable> subscriptions = new ConcurrentHashMap<>();

    // Map json-rpc method to java
    private Map<String, ReflectionInfo> jsonRpcToJava = new HashMap<>();

    private ServerWebSocket socket;

    /**
     * This gets called on build to build into of the classes we are going to call in runtime
     *
     * @param extensionMethodsMap
     */
    public void populateJsonRPCMethods(Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap) {
        for (Map.Entry<String, Map<JsonRpcMethodName, JsonRpcMethod>> extension : extensionMethodsMap.entrySet()) {
            String extensionName = extension.getKey();
            Map<JsonRpcMethodName, JsonRpcMethod> jsonRpcMethods = extension.getValue();
            for (Map.Entry<JsonRpcMethodName, JsonRpcMethod> method : jsonRpcMethods.entrySet()) {
                JsonRpcMethodName methodName = method.getKey();
                JsonRpcMethod jsonRpcMethod = method.getValue();

                @SuppressWarnings("unchecked")
                Object providerInstance = Arc.container().select(jsonRpcMethod.getClazz()).get();

                try {
                    Method javaMethod = null;
                    Map<String, Class> params = null;
                    if (jsonRpcMethod.hasParams()) {
                        params = jsonRpcMethod.getParams();
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName(),
                                params.values().toArray(new Class[] {}));
                    } else {
                        javaMethod = providerInstance.getClass().getMethod(jsonRpcMethod.getMethodName());
                    }
                    ReflectionInfo reflectionInfo = new ReflectionInfo(jsonRpcMethod.getClazz(), providerInstance, javaMethod,
                            params);
                    String jsonRpcMethodName = extensionName + DOT + methodName;
                    jsonRpcToJava.put(jsonRpcMethodName, reflectionInfo);
                } catch (NoSuchMethodException | SecurityException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public void setSocket(ServerWebSocket socket) {
        this.socket = socket;

        this.socket.closeHandler((e) -> {
            this.onStop();
        });

        this.socket.textMessageHandler((e) -> {
            this.onMessage(e);
        });

        this.socket.exceptionHandler((e) -> {
            this.onError(e);
        });

        this.onStart();

    }

    private String route(String message) {
        JsonRpcRequest jsonRpcRequest = toJsonRpcRequest(message);
        JsonRpcMessage jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    @SuppressWarnings("unchecked")
    private JsonRpcMessage route(JsonRpcRequest jsonRpcRequest) {

        String jsonRpcMethodName = jsonRpcRequest.getMethod();

        // First check some internal methods
        if (jsonRpcMethodName.equalsIgnoreCase("unsubscribe")) {
            JsonRpcResponse jsonRpcResponse = toJsonRpcResponse(MessageType.Void, jsonRpcRequest.getId(), null);

            if (this.subscriptions.containsKey(jsonRpcRequest.getId())) {
                Cancellable cancellable = this.subscriptions.remove(jsonRpcRequest.getId());
                cancellable.cancel();
            }
            return jsonRpcResponse;

        } else if (this.jsonRpcToJava.containsKey(jsonRpcMethodName)) { // Route to extension
            ReflectionInfo reflectionInfo = this.jsonRpcToJava.get(jsonRpcMethodName);
            Object providerInstance = Arc.container().select(reflectionInfo.bean).get();
            try {
                Object result;
                if (jsonRpcRequest.hasParams()) {
                    Object[] args = getArgsAsObjects(reflectionInfo.params, jsonRpcRequest);
                    result = reflectionInfo.method.invoke(providerInstance, args);
                } else {
                    result = reflectionInfo.method.invoke(providerInstance);
                }

                // Here wrap in our own object that contain some more metadata
                JsonRpcResponse jsonRpcResponse;
                if (reflectionInfo.isSubscription()) {
                    // Subscription
                    Multi<?> subscription = (Multi) result;

                    Cancellable cancellable = subscription.subscribe().with((t) -> {
                        String serverMessage = Json
                                .encode(toJsonRpcResponse(MessageType.SubscriptionMessage, jsonRpcRequest.getId(), t));
                        socket.writeTextMessage(serverMessage);
                    });

                    this.subscriptions.put(jsonRpcRequest.getId(), cancellable);

                    jsonRpcResponse = toJsonRpcResponse(MessageType.Void, jsonRpcRequest.getId(), null);

                } else {
                    // Normal response

                    jsonRpcResponse = toJsonRpcResponse(MessageType.Response, jsonRpcRequest.getId(), result);
                }

                return jsonRpcResponse;
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                throw new RuntimeException(ex);
            }
        }

        // Method not found
        return JsonRpcErrorResponse.methodNotFound("Method [" + jsonRpcMethodName + "] not found");

    }

    private void onMessage(String message) {
        this.socket.writeTextMessage(route(message));
    }

    private void onStop() {
        // ?
    }

    private void onStart() {
        // ?
    }

    private void onError(Throwable t) {
        // ?
        t.printStackTrace();
    }

    private Object[] getArgsAsObjects(Map<String, Class> params, JsonRpcRequest jsonRpcRequest) {
        List<Object> objects = new ArrayList<>();
        for (Map.Entry<String, Class> expectedParams : params.entrySet()) {
            String paramName = expectedParams.getKey();
            Class paramType = expectedParams.getValue();
            Object param = jsonRpcRequest.getParam(paramName);
            Object casted = paramType.cast(param);
            objects.add(casted);
        }
        return objects.toArray(Object[]::new);
    }

    private JsonRpcRequest toJsonRpcRequest(String message) {
        JsonObject jsonObject = (JsonObject) Json.decodeValue(message);
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setJsonrpc(jsonObject.getString(JSONRPC, VERSION));
        jsonRpcRequest.setMethod(jsonObject.getString(METHOD));
        jsonRpcRequest.setParams(jsonObject.getJsonObject(PARAMS).getMap());
        jsonRpcRequest.setId(jsonObject.getInteger(ID));

        return jsonRpcRequest;
    }

    private <T> JsonRpcResponse toJsonRpcResponse(MessageType type, int id, T t) {
        DevUIResponse devUIResponse = new DevUIResponse(t, type);

        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse();
        jsonRpcResponse.setId(id);
        jsonRpcResponse.setJsonrpc(VERSION);
        jsonRpcResponse.setResult(devUIResponse);
        return jsonRpcResponse;
    }

    private static final String JSONRPC = "jsonrpc";
    private static final String VERSION = "2.0";
    private static final String METHOD = "method";
    private static final String PARAMS = "params";
    private static final String ID = "id";
    private static final String DOT = ".";

}
