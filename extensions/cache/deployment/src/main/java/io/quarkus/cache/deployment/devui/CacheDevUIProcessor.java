package io.quarkus.cache.deployment.devui;

import io.quarkus.cache.runtime.devui.CacheJsonRPCService;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.page.Page;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;
import io.quarkus.devui.deployment.spi.runtime.JsonRPCProvidersBuildItem;

/**
 * Show the current caches in Dev UI
 */
public class CacheDevUIProcessor {

    private static final String NAME = "Cache";

    @BuildStep(onlyIf = IsDevelopment.class)
    PageBuildItem createCachePage() {

        PageBuildItem pageBuildItem = new PageBuildItem(NAME);

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:boxes-stacked")
                .componentLink("qwc-cache-caches.js")
                .label("1")); // TODO Get this from recording

        return pageBuildItem;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem(NAME, CacheJsonRPCService.class);
    }

}
