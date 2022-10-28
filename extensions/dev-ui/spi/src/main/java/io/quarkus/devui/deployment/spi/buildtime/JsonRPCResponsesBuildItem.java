package io.quarkus.devui.deployment.spi.buildtime;

import java.util.Map;

import io.quarkus.devui.deployment.spi.AbstractDevUIBuildItem;

/**
 * This allows you to make data, generated at build time, available on the JsonRPC Endpoint during Runtime
 */
public final class JsonRPCResponsesBuildItem extends AbstractDevUIBuildItem {

    private final Map<String, Object> methodNameAndResponseData;

    public JsonRPCResponsesBuildItem(String extensionName, Map<String, Object> methodNameAndResponseData) {
        super(extensionName);
        this.methodNameAndResponseData = methodNameAndResponseData;
    }

    public Map<String, Object> getMethodNameAndResponseData() {
        return methodNameAndResponseData;
    }
}
