package io.quarkus.devui.runtime.jsonrpc.handler;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

/**
 * Handles all core communication (before any extensions)
 */
@ApplicationScoped
@Named("DevUIInternalJsonRPCMethodProvider")
public class DevUIInternalJsonRPCMethodProvider {

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

}
