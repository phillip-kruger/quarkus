package io.quarkus.devui.runtime.jsonrpc;

public interface JsonRpcKeys {

    public static final String VERSION = "2.0";
    public static final String JSONRPC = "jsonrpc";
    public static final String OBJECT = "object";
    public static final String MESSAGE_TYPE = "messageType";
    public static final String ID = "id";
    public static final String RESULT = "result";
    public static final String MESSAGE = "message";
    public static final String CODE = "code";
    public static final String ERROR = "error";
    public static final String METHOD = "method";
    public static final String PARAMS = "params";

    public static final int PARSE_ERROR = -32700; // Parse error. Invalid JSON was received by the server. An error occurred on the server while parsing the JSON text.
    public static final int INVALID_REQUEST = -32600; // Invalid Request. The JSON sent is not a valid Request object.
    public static final int METHOD_NOT_FOUND = -32601; // Method not found. The method does not exist / is not available.
    public static final int INVALID_PARAMS = -32602; // Invalid params.	Invalid method parameter(s).
    public static final int INTERNAL_ERROR = -32603; //	Internal error. Internal JSON-RPC error.

    // Domain-specific error codes (server error range: -32000 to -32099)
    public static final int VALIDATION_ERROR = -32001; // Validation error. Invalid input data.
    public static final int NOT_ENABLED_ERROR = -32002; // Feature not enabled. The requested feature is disabled.
    public static final int TIMEOUT_ERROR = -32003; // Timeout error. The operation timed out.
    public static final int AUTHORIZATION_ERROR = -32004; // Authorization error. The operation is not authorized.

    // Additional keys for enhanced error responses
    public static final String ERROR_TYPE = "type";
    public static final String ERROR_STACK_TRACE = "stackTrace";

}