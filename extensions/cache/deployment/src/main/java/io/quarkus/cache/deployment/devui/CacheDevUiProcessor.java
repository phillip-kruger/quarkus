package io.quarkus.cache.deployment.devui;

import io.quarkus.cache.runtime.devui.CacheJsonRPCService;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.devui.spi.IsDevUI;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.CardPageBuildItem;
import io.quarkus.devui.spi.page.Page;

public class CacheDevUiProcessor {

    @BuildStep(onlyIf = IsDevUI.class)
    CardPageBuildItem create(CurateOutcomeBuildItem bi) {
        CardPageBuildItem pageBuildItem = new CardPageBuildItem();
        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .title("Caches")
                .componentLink("qwc-cache-caches.js")
                .icon("font-awesome-solid:database"));

        return pageBuildItem;
    }

    @BuildStep(onlyIf = IsDevUI.class)
    JsonRPCProvidersBuildItem createJsonRPCServiceForCache() {
        return new JsonRPCProvidersBuildItem(CacheJsonRPCService.class);
    }
}
