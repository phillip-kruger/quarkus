package io.quarkus.devui.runtime.jsonrpc;

public interface JsonRPCMethodProvider {

    public static String createBeanName(String extensionName) {
        return BEAN_NAME_PRE + extensionName + BEAN_NAME_POST;
    }

    default <T> JsonRpcResponse toJsonRpcResponse(JsonRpcRequest message, T t) {
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse();
        jsonRpcResponse.setId(message.getId());
        jsonRpcResponse.setJsonrpc(message.getJsonrpc());
        jsonRpcResponse.setResult(t);
        return jsonRpcResponse;
    }

    public <T> T request(JsonRpcRequest request);

    static final String BEAN_NAME_PRE = "DevUI";
    static final String BEAN_NAME_POST = "JsonRPCMethodProvider";
}
