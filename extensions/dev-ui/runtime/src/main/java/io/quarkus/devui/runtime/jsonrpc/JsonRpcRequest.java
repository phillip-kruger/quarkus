package io.quarkus.devui.runtime.jsonrpc;

import java.util.Map;

public class JsonRpcRequest extends JsonRpcMessage {
    private String method;
    private Map<String, ?> params;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, ?> getParams() {
        return params;
    }

    public void setParams(Map<String, ?> params) {
        this.params = params;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String key) {
        if (this.params == null || !this.params.containsKey(key)) {
            return null;
        }
        return (T) this.params.get(key);
    }

    @Override
    public String toString() {
        return "JsonRpcRequest [id=" + id + ", jsonrpc=" + jsonrpc + ", method=" + method + ", params=" + params + "]";
    }
}
