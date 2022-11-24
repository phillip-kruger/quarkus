package io.quarkus.devui.runtime.jsonrpc;

public class DevUIResponse {
    private Object object;
    private MessageType messageType;

    public DevUIResponse() {
    }

    public DevUIResponse(Object object, MessageType messageType) {
        this.object = object;
        this.messageType = messageType;
    }

    public Object getObject() {
        return object;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String className() {
        return object.getClass().getName();
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    @Override
    public String toString() {
        return "DevUIResponse{" + "object=" + object + ", type=" + messageType + '}';
    }
}
