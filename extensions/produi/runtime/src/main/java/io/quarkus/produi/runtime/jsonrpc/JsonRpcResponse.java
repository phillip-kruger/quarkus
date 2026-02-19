package io.quarkus.produi.runtime.jsonrpc;

import static io.quarkus.produi.runtime.jsonrpc.JsonRpcKeys.VERSION;

/**
 * JSON-RPC response object for Prod UI.
 */
public final class JsonRpcResponse {

    public final int id;
    public final Object result;
    public final Error error;

    public JsonRpcResponse(int id, Object result) {
        this.id = id;
        this.result = result;
        this.error = null;
    }

    public JsonRpcResponse(int id, Error error) {
        this.id = id;
        this.result = null;
        this.error = error;
    }

    public String getJsonrpc() {
        return VERSION;
    }

    @Override
    public String toString() {
        return "JsonRpcResponse{" +
                "id=" + id +
                ", result=" + result +
                ", error=" + error +
                '}';
    }

    public static final class Error {
        public final int code;
        public final String message;

        public Error(int code, String message) {
            this.code = code;
            this.message = message;
        }

        @Override
        public String toString() {
            return "Error{" +
                    "code=" + code +
                    ", message='" + message + '\'' +
                    '}';
        }
    }
}
