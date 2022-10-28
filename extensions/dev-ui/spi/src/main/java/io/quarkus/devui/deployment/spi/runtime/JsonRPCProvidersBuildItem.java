package io.quarkus.devui.deployment.spi.runtime;

import io.quarkus.devui.deployment.spi.AbstractDevUIBuildItem;

/**
 * This allows you to register a class that will provide data during runtime for JsonRPC Requests
 */
public final class JsonRPCProvidersBuildItem extends AbstractDevUIBuildItem {

    private final Class jsonRPCMethodProviderClass;

    public JsonRPCProvidersBuildItem(String extensionName, Class jsonRPCMethodProviderClass) {
        super(extensionName);
        this.jsonRPCMethodProviderClass = jsonRPCMethodProviderClass;
    }

    public Class getJsonRPCMethodProviderClass() {
        return jsonRPCMethodProviderClass;
    }
}
