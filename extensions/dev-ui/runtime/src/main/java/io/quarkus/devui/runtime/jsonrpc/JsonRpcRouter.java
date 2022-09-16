package io.quarkus.devui.runtime.jsonrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import io.quarkus.devui.runtime.jsonrpc.handler.DevUIPlatformHandler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

/**
 * Route JsonRPC message to the correct method
 * TODO: Find a way to route to extensions. (Some Build item)
 */
@ApplicationScoped
public class JsonRpcRouter {

    @Inject
    DevUIPlatformHandler platformHandler;

    public String route(String message) {
        JsonRpcRequest jsonRpcRequest = toJsonRpcRequest(message);
        JsonRpcResponse jsonRpcResponse = route(jsonRpcRequest);
        return Json.encode(jsonRpcResponse);
    }

    public JsonRpcResponse route(JsonRpcRequest jsonRpcRequest) {

        try {
            Method method = lookupMethod(jsonRpcRequest.getMethod());
            return (JsonRpcResponse) method.invoke(platformHandler, jsonRpcRequest);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Method lookupMethod(String methodName) {
        try {
            return DevUIPlatformHandler.class.getMethod(methodName, JsonRpcRequest.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private JsonRpcRequest toJsonRpcRequest(String message) {
        // TODO: Handle parsing error ?
        JsonObject jsonObject = (JsonObject) Json.decodeValue(message);
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest();
        jsonRpcRequest.setJsonrpc(jsonObject.getString(JSONRPC, VERSION));
        jsonRpcRequest.setMethod(jsonObject.getString(METHOD));
        jsonRpcRequest.setParams(jsonObject.getJsonObject(PARAMS).getMap());
        jsonRpcRequest.setId(jsonObject.getInteger(ID));

        return jsonRpcRequest;
    }

    private static final String JSONRPC = "jsonrpc";
    private static final String VERSION = "2.0";
    private static final String METHOD = "method";
    private static final String PARAMS = "params";
    private static final String ID = "id";
}
