package io.quarkus.devui.runtime.jsonrpc;

public class JsonRpcResponse extends JsonRpcMessage {
    private Object result;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "JsonRpcResponse [id=" + id + ", jsonrpc=" + jsonrpc + ", result=" + result + "]";
    }
}