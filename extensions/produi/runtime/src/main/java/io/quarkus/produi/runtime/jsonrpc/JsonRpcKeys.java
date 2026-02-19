package io.quarkus.produi.runtime.jsonrpc;

/**
 * JSON-RPC 2.0 protocol keys and error codes.
 */
public interface JsonRpcKeys {

    String VERSION = "2.0";
    String JSONRPC = "jsonrpc";
    String OBJECT = "object";
    String MESSAGE_TYPE = "messageType";
    String ID = "id";
    String RESULT = "result";
    String MESSAGE = "message";
    String CODE = "code";
    String ERROR = "error";
    String METHOD = "method";
    String PARAMS = "params";

    int PARSE_ERROR = -32700;
    int INVALID_REQUEST = -32600;
    int METHOD_NOT_FOUND = -32601;
    int INVALID_PARAMS = -32602;
    int INTERNAL_ERROR = -32603;
}
