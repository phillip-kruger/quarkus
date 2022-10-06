package io.quarkus.devui.runtime.jsonrpc;

public class DevUIJsonRPCProviderNamer {

    private DevUIJsonRPCProviderNamer() {
    }

    public static String createBeanName(String extensionName) {
        return BEAN_NAME_PRE + extensionName + BEAN_NAME_POST;
    }

    private static final String BEAN_NAME_PRE = "DevUI";
    private static final String BEAN_NAME_POST = "JsonRPCMethodProvider";
}
