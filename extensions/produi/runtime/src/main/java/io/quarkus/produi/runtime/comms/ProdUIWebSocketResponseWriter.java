package io.quarkus.produi.runtime.comms;

import io.quarkus.produi.runtime.jsonrpc.Result;
import io.vertx.core.http.ServerWebSocket;

/**
 * WebSocket-based response writer for Prod UI JSON-RPC.
 */
public class ProdUIWebSocketResponseWriter implements JsonRpcResponseWriter {
    private final ServerWebSocket socket;

    public ProdUIWebSocketResponseWriter(ServerWebSocket socket) {
        this.socket = socket;
    }

    @Override
    public void write(String message) {
        if (!socket.isClosed()) {
            socket.writeTextMessage(message);
        }
    }

    @Override
    public void close() {
        socket.close();
    }

    @Override
    public boolean isOpen() {
        return !socket.isClosed();
    }

    @Override
    public boolean isClosed() {
        return socket.isClosed();
    }

    @Override
    public Object decorateObject(Object object, MessageType messageType) {
        return new Result(messageType.name(), object);
    }
}
