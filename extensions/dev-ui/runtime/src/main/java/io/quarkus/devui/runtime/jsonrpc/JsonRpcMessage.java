package io.quarkus.devui.runtime.jsonrpc;

public abstract class JsonRpcMessage {
    protected String jsonrpc = "2.0";
    protected int id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
