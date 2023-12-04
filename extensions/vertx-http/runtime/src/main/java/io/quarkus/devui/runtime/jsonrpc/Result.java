package io.quarkus.devui.runtime.jsonrpc;

public class Result<T> {
    private String messageType;
    private T object;

    public Result() {
    }

    public Result(String messageType, T object) {
        this.messageType = messageType;
        this.object = object;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public T getObject() {
        return object;
    }

    public void setObject(T object) {
        this.object = object;
    }
}
