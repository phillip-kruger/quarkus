package io.quarkus.devui.runtime.comms;

import io.quarkus.devui.runtime.jsonrpc.JsonRpcRequest;
import io.quarkus.devui.runtime.jsonrpc.json.JsonMapper;

public interface JsonRpcResponseWriter {

    void write(String message);

    void close();

    boolean isOpen();

    boolean isClosed();

    Object decorateObject(JsonMapper jsonMapper, JsonRpcRequest jsonRpcRequest, Object object, MessageType messageType);

}