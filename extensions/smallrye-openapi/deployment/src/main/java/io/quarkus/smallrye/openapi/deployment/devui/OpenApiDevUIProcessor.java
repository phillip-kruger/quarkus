package io.quarkus.smallrye.openapi.deployment.devui;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.CardLink;
import io.quarkus.devui.deployment.spi.DevUICardLinksBuildItem;

public class OpenApiDevUIProcessor {

    private static final String NAME = "smallrye openapi";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void cardLinks(BuildProducer<DevUICardLinksBuildItem> devUICardLinksProducer) {
        List<CardLink> links = new ArrayList<>();

        links.add(new CardLink.Builder(NAME)
                .iconName("font-awesome-solid:file-circle-check")
                .displayName("Schema Document")
                .path("/q/openapi").build());

        links.add(new CardLink.Builder(NAME)
                .iconName("font-awesome-solid:signs-post")
                .displayName("Swagger UI")
                .path("/q/swagger-ui").build());

        devUICardLinksProducer.produce(new DevUICardLinksBuildItem(NAME, links));
    }

}
