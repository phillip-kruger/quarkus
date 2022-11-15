package io.quarkus.devui.runtime.jsonrpc;

public class JsonRpcError {

    private int code; // A Number that indicates the error type that occurred. See above. This MUST be an integer.
    private String message; // A String providing a short description of the error. The message SHOULD be limited to a concise single sentence.
    private Object data; // A Primitive or Structured value that contains additional information about the error. This may be omitted. The value of this member is defined by the Server (e.g. detailed error information, nested errors etc.).

    public JsonRpcError() {
    }

    public JsonRpcError(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public JsonRpcError(int code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JsonRpcError{" + "code=" + code + ", message=" + message + ", data=" + data + '}';
    }
}
