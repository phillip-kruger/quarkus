package io.quarkus.devui.deployment.internal;

import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.BuildTimeConstBuildItem;
import io.quarkus.devui.deployment.ExtensionsBuildItem;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;
import io.quarkus.devui.runtime.jsonrpc.handler.MenuItem;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionGroup;

/**
 * Processor that creates the parts needed for the Dev UI Internal components
 */
public class InternalProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createBuildTimeData(BuildProducer<BuildTimeConstBuildItem> buildTimeConstProducer,
            ExtensionsBuildItem extensionsBuildItem) {

        // Extensions
        Map<ExtensionGroup, List<Extension>> response = Map.of(
                ExtensionGroup.active, extensionsBuildItem.getActiveExtensions(),
                ExtensionGroup.inactive, extensionsBuildItem.getInactiveExtensions());

        BuildTimeConstBuildItem internalBuildTimeData = new BuildTimeConstBuildItem(PageBuildItem.DEV_UI);

        internalBuildTimeData.addBuildTimeData("extensions", response);

        // Sections Menu
        // TODO: Get this from PageBuildItem
        List<MenuItem> menuItems = List.of(new MenuItem("qwc-extensions", "puzzle-piece", true),
                new MenuItem("qwc-configuration", "sliders"),
                new MenuItem("qwc-continuous-testing", "flask-vial"),
                new MenuItem("qwc-dev-services", "wand-magic-sparkles"),
                new MenuItem("qwc-build-steps", "hammer"));

        internalBuildTimeData.addBuildTimeData("menuItems", menuItems);

        // TODO: Implement below
        internalBuildTimeData.addBuildTimeData("allConfiguration", "Loading Configuration");
        internalBuildTimeData.addBuildTimeData("continuousTesting", "Loading Continuous Testing");
        internalBuildTimeData.addBuildTimeData("devServices", "Loading Dev Services");
        internalBuildTimeData.addBuildTimeData("buildSteps", "Loading Build Steps");

        buildTimeConstProducer.produce(internalBuildTimeData);
    }

}
