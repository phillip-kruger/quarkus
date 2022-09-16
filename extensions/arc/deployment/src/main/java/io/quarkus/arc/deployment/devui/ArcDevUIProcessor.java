package io.quarkus.arc.deployment.devui;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.CardLink;
import io.quarkus.devui.deployment.spi.DevUICardLinksBuildItem;

public class ArcDevUIProcessor {

    private static final String NAME = "ArC";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void cardLinks(BuildProducer<DevUICardLinksBuildItem> devUICardLinksProducer) {
        List<CardLink> links = new ArrayList<>();

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:egg")
                .component("qwc-arc-beans.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:eye")
                .component("qwc-arc-observers.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:traffic-light")
                .component("qwc-arc-interceptors.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:traffic-light")
                .component("qwc-arc-decorators.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:fire")
                .component("qwc-arc-fired-events.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:diagram-project")
                .component("qwc-arc-invocation-trees.js").build());

        links.add(new CardLink.Builder(NAME).iconName("font-awesome-solid:trash-can")
                .component("qwc-arc-removed-components.js").build());

        devUICardLinksProducer.produce(new DevUICardLinksBuildItem(NAME, links));
    }

}
