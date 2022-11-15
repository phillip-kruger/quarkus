package io.quarkus.devui.runtime.jsonrpc;

public class JsonRpcErrorResponse extends JsonRpcMessage {
    public static final int PARSE_ERROR = -32700; // Parse error. Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text.
    public static final int INVALID_REQUEST = -32600; // Invalid Request. The JSON sent is not a valid Request object.
    public static final int METHOD_NOT_FOUND = -32601; // Method not found. The method does not exist / is not available.
    public static final int INVALID_PARAMS = -32602; // Invalid params.	Invalid method parameter(s).
    public static final int INTERNAL_ERROR = -32603; //	Internal error. Internal JSON-RPC error.

    private JsonRpcError error;

    public static JsonRpcErrorResponse methodNotFound(String message) {
        JsonRpcErrorResponse e = new JsonRpcErrorResponse();
        e.setError(new JsonRpcError(METHOD_NOT_FOUND, message));
        return e;
    }

    public JsonRpcError getError() {
        return error;
    }

    public void setError(JsonRpcError error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "JsonRpcErrorResponse [id=" + id + ", jsonrpc=" + jsonrpc + ", error=" + error + "]";
    }
}