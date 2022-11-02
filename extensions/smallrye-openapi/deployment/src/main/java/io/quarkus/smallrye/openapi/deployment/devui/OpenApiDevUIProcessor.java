package io.quarkus.smallrye.openapi.deployment.devui;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.page.Page;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;

public class OpenApiDevUIProcessor {

    private static final String NAME = "Smallrye Openapi";

    @BuildStep(onlyIf = IsDevelopment.class)
    public PageBuildItem pages(NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {

        PageBuildItem pageBuildItem = new PageBuildItem(NAME);

        pageBuildItem.addPage(Page.externalPageBuilder("Schema yaml")
                .url(nonApplicationRootPathBuildItem.resolvePath("openapi"))
                .icon("font-awesome-solid:file-lines").build());

        pageBuildItem.addPage(Page.externalPageBuilder("Schema json")
                .url(nonApplicationRootPathBuildItem.resolvePath("openapi") + "?format=json")
                .icon("font-awesome-solid:file-code").build());

        pageBuildItem.addPage(Page.externalPageBuilder("Swagger UI")
                .url(nonApplicationRootPathBuildItem.resolvePath("swagger-ui"))
                .icon("font-awesome-solid:signs-post").build());

        return pageBuildItem;
    }

}
