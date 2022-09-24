package io.quarkus.devui.deployment.spi;

import io.quarkus.builder.item.MultiBuildItem;

// TODO: ??
public final class DevUIJsonRPCMethodBuildItem extends MultiBuildItem {

    private final String extensionName;
    private final Class jsonRPCMethodProviderClass;

    public DevUIJsonRPCMethodBuildItem(String extensionName, Class jsonRPCMethodProviderClass) {
        this.extensionName = extensionName;
        this.jsonRPCMethodProviderClass = jsonRPCMethodProviderClass;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public Class getJsonRPCMethodProviderClass() {
        return jsonRPCMethodProviderClass;
    }
}
