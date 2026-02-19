package io.quarkus.produi.runtime.comms;

/**
 * Interface for writing JSON-RPC responses.
 */
public interface JsonRpcResponseWriter {

    void write(String message);

    void close();

    boolean isOpen();

    boolean isClosed();

    Object decorateObject(Object object, MessageType messageType);
}
