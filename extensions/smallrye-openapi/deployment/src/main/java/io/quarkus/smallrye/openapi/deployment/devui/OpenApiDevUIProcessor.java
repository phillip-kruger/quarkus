package io.quarkus.smallrye.openapi.deployment.devui;

import java.util.List;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.page.ExternalPage;
import io.quarkus.devui.deployment.spi.page.ExternalPageBuildItem;

public class OpenApiDevUIProcessor {

    private static final String NAME = "smallrye openapi";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void cardLinks(BuildProducer<ExternalPageBuildItem> externalPageProducer) {
        List<ExternalPage> links = List.of(
                new ExternalPage.Builder(NAME)
                        .iconName("font-awesome-solid:file-circle-check")
                        .displayName("Schema Document")
                        .externalURL("/q/openapi").build(),
                new ExternalPage.Builder(NAME)
                        .iconName("font-awesome-solid:signs-post")
                        .displayName("Swagger UI")
                        .externalURL("/q/swagger-ui").build());

        externalPageProducer.produce(new ExternalPageBuildItem(NAME, links));
    }

}
