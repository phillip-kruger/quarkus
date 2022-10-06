package io.quarkus.devui.runtime.jsonrpc.handler;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionGroup;
import io.quarkus.devui.runtime.service.extension.ExtensionsService;

/**
 * Handles all core communication (before any extensions)
 */
@ApplicationScoped
@Named("DevUIInternalJsonRPCMethodProvider")
public class DevUIInternalJsonRPCMethodProvider {

    @ConfigProperty(name = "quarkus.application.name")
    String applicationName;

    @ConfigProperty(name = "quarkus.application.version")
    String applicationVersion;

    @ConfigProperty(name = "quarkus.platform.version", defaultValue = "999-SNAPSHOT")
    String platformVersion;

    @Inject
    ExtensionsService extensionsService;

    public VersionInfo getVersionInfo() {
        return new VersionInfo(platformVersion, applicationName, applicationVersion);
    }

    // TODO: Allow extension to contribute to this
    public List<MenuItem> getMenuItems() {
        return List.of(
                new MenuItem("qwc-extensions", "puzzle-piece", true),
                new MenuItem("qwc-configuration", "sliders"),
                new MenuItem("qwc-continuous-testing", "flask-vial"),
                new MenuItem("qwc-dev-services", "wand-magic-sparkles"),
                new MenuItem("qwc-build-steps", "hammer"));
    }

    // TODO: Allow extension to contribute to this
    public List<BottomDrawerItem> getBottomDrawerItems() {
        return List.of(
                new BottomDrawerItem("qwc-log"));
    }

    public Map<ExtensionGroup, List<Extension>> getExtensions() {
        return extensionsService.getExtensions();
    }

    public String getAllConfiguration() {
        return "Loading Configuration";
    }

    public String getContinuousTesting() {
        return "Loading Continuous testing";
    }

    public String getDevServices() {
        return "Loading Dev services";
    }

    public String getBuildSteps() {
        return "Loading Build steps";
    }

    private static final String INTERNAL = "Internal";
    private static final String DOT = ".";

    private static final String GET_VERSION_INFO = "getVersionInfo";
    private static final String GET_MENU_ITEMS = "getMenuItems";
    private static final String GET_BOTTOM_DRAWER_ITEMS = "getBottomDrawerItems";
    private static final String GET_EXTENSIONS = "getExtensions";
    private static final String GET_CONFIGURATION = "getAllConfiguration";
    private static final String GET_CONTINUOUS_TESTING = "getContinuousTesting";
    private static final String GET_DEV_SERVICES = "getDevServices";
    private static final String GET_BUILD_STEPS = "getBuildSteps";

}
