package io.quarkus.devui.runtime.jsonrpc.handler;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.devui.runtime.jsonrpc.JsonRpcRequest;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcResponse;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionGroup;
import io.quarkus.devui.runtime.service.extension.ExtensionsService;

/**
 * Handles all core communication (before any extensions)
 */
@ApplicationScoped
public class DevUIPlatformHandler {

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    @ConfigProperty(name = "quarkus.platform.version", defaultValue = "999-SNAPSHOT")
    String platformVersion;

    @Inject
    ExtensionsService extensionsService;

    public JsonRpcResponse getVersionInfo(JsonRpcRequest message) {
        return toJsonRpcResponse(message, new VersionInfo(platformVersion, applicationName, applicationVersion));
    }

    public JsonRpcResponse getMenuItems(JsonRpcRequest message) {
        return toJsonRpcResponse(message, getMenuItems());
    }

    public JsonRpcResponse getBottomDrawerItems(JsonRpcRequest message) {
        return toJsonRpcResponse(message, getBottomDrawerItems());
    }

    public JsonRpcResponse getExtensions(JsonRpcRequest message) {
        Map<ExtensionGroup, List<Extension>> extensions = extensionsService.getExtensions();
        return toJsonRpcResponse(message, extensions);
    }

    public JsonRpcResponse getAllConfiguration(JsonRpcRequest message) {
        return toJsonRpcResponse(message, "To be implemented");
    }

    public JsonRpcResponse getContinuousTesting(JsonRpcRequest message) {
        return toJsonRpcResponse(message, "To be implemented");
    }

    public JsonRpcResponse getDevServices(JsonRpcRequest message) {
        return toJsonRpcResponse(message, "To be implemented");
    }

    public JsonRpcResponse getBuildSteps(JsonRpcRequest message) {
        return toJsonRpcResponse(message, "To be implemented");
    }

    // TODO: Allow extension to contribute to this
    private List<MenuItem> getMenuItems() {
        return List.of(
                new MenuItem("qwc-extensions", "puzzle-piece", true),
                new MenuItem("qwc-configuration", "sliders"),
                new MenuItem("qwc-continuous-testing", "flask-vial"),
                new MenuItem("qwc-dev-services", "wand-magic-sparkles"),
                new MenuItem("qwc-build-steps", "hammer"));
    }

    // TODO: Allow extension to contribute to this
    private List<BottomDrawerItem> getBottomDrawerItems() {
        return List.of(
                new BottomDrawerItem("qwc-log"));
    }

    private <T> JsonRpcResponse toJsonRpcResponse(JsonRpcRequest message, T t) {
        JsonRpcResponse jsonRpcResponse = new JsonRpcResponse();
        jsonRpcResponse.setId(message.getId());
        jsonRpcResponse.setJsonrpc(message.getJsonrpc());
        jsonRpcResponse.setResult(t);
        return jsonRpcResponse;
    }
}
