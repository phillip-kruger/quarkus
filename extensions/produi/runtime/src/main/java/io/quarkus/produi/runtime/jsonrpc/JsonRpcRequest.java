package io.quarkus.produi.runtime.jsonrpc;

import java.util.Map;

import io.vertx.core.json.JsonObject;

/**
 * JSON-RPC request object for Prod UI.
 */
public final class JsonRpcRequest {
    private int id;
    private String jsonrpc = JsonRpcKeys.VERSION;
    private String method;
    private Map<String, Object> params;

    public JsonRpcRequest() {
    }

    public static JsonRpcRequest fromJson(JsonObject jsonObject) {
        JsonRpcRequest request = new JsonRpcRequest();
        if (jsonObject.containsKey(JsonRpcKeys.ID)) {
            request.setId(jsonObject.getInteger(JsonRpcKeys.ID));
        }
        if (jsonObject.containsKey(JsonRpcKeys.JSONRPC)) {
            request.setJsonrpc(jsonObject.getString(JsonRpcKeys.JSONRPC));
        }
        request.setMethod(jsonObject.getString(JsonRpcKeys.METHOD));
        if (jsonObject.containsKey(JsonRpcKeys.PARAMS)) {
            request.setParams(jsonObject.getJsonObject(JsonRpcKeys.PARAMS).getMap());
        }
        return request;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public boolean hasParams() {
        return this.params != null && !this.params.isEmpty();
    }

    public boolean hasParam(String key) {
        return this.params != null && this.params.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, Class<T> paramType) {
        if (hasParam(key)) {
            Object value = params.get(key);
            if (value == null) {
                return null;
            }
            if (paramType.isInstance(value)) {
                return (T) value;
            }
            // Handle basic type conversions
            if (paramType == String.class) {
                return (T) value.toString();
            }
            if (paramType == Integer.class || paramType == int.class) {
                return (T) Integer.valueOf(value.toString());
            }
            if (paramType == Long.class || paramType == long.class) {
                return (T) Long.valueOf(value.toString());
            }
            if (paramType == Boolean.class || paramType == boolean.class) {
                return (T) Boolean.valueOf(value.toString());
            }
            if (paramType == Double.class || paramType == double.class) {
                return (T) Double.valueOf(value.toString());
            }
            return (T) value;
        }
        return null;
    }
}
