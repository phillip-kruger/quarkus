package io.quarkus.devui.runtime.jsonrpc;

import static io.quarkus.devui.runtime.jsonrpc.JsonRpcKeys.VERSION;

public final class JsonRpcResponse {

    // Public for serialization
    private int id;
    private Result result;
    private Error error;

    public JsonRpcResponse() {

    }

    public JsonRpcResponse(int id, Result result) {
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }
}
