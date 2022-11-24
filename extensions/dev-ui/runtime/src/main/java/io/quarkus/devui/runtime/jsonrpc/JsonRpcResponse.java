package io.quarkus.devui.runtime.jsonrpc;

public class JsonRpcResponse extends JsonRpcMessage {
    private DevUIResponse result;

    public DevUIResponse getResult() {
        return result;
    }

    public void setResult(DevUIResponse result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "JsonRpcResponse [id=" + id + ", jsonrpc=" + jsonrpc + ", result=" + result + "]";
    }
}