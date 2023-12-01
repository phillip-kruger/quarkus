package io.quarkus.devui.runtime.jsonrpc;

public class Result {
    private String messageType;
    private Object object;

    public Result() {
    }

    public Result(String messageType, Object object) {
        this.messageType = messageType;
        this.object = object;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
