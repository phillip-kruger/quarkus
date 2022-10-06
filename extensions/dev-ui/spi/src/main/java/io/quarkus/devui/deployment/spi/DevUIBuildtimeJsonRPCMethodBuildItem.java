package io.quarkus.devui.deployment.spi;

import java.util.Map;

import io.quarkus.builder.item.MultiBuildItem;

// TODO: ??
public final class DevUIBuildtimeJsonRPCMethodBuildItem extends MultiBuildItem {

    private final String extensionName;
    private final Map<String, Object> methodNameAndResponseData;

    public DevUIBuildtimeJsonRPCMethodBuildItem(String extensionName, Map<String, Object> methodNameAndResponseData) {
        this.extensionName = extensionName;
        this.methodNameAndResponseData = methodNameAndResponseData;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public Map<String, Object> getMethodNameAndResponseData() {
        return methodNameAndResponseData;
    }
}
