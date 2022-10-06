package io.quarkus.arc.deployment.devui;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.arc.deployment.devconsole.DevBeanInfo;
import io.quarkus.arc.deployment.devconsole.DevBeanInfos;
import io.quarkus.arc.deployment.devconsole.DevDecoratorInfo;
import io.quarkus.arc.deployment.devconsole.DevInterceptorInfo;
import io.quarkus.arc.deployment.devconsole.DevObserverInfo;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.CardLink;
import io.quarkus.devui.deployment.spi.DevUICardLinksBuildItem;

public class ArcDevUIProcessor {

    private static final String NAME = "ArC";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void cardLinks(ArcBeanInfoBuildItem arcBeanInfoBuildItem,
            BuildProducer<DevUICardLinksBuildItem> devUICardLinksProducer) {
        List<CardLink> links = new ArrayList<>();

        DevBeanInfos beanInfos = arcBeanInfoBuildItem.getBeanInfos();

        List<DevBeanInfo> beans = beanInfos.getBeans();
        if (!beans.isEmpty()) {
            links.add(new CardLink.Builder(NAME)
                    .iconName("font-awesome-solid:egg")
                    .component("qwc-arc-beans.js")
                    .label(String.valueOf(beans.size()))
                    .buildTimeData(GET_BEANS, toDevBeanWithInterceptorInfo(beans, beanInfos))
                    .build());
        }

        List<DevObserverInfo> observers = beanInfos.getObservers();
        if (!observers.isEmpty()) {
            links.add(new CardLink.Builder(NAME)
                    .iconName("font-awesome-solid:eye")
                    .component("qwc-arc-observers.js")
                    .label(String.valueOf(observers.size()))
                    .buildTimeData(GET_OBSERVERS, observers)
                    .build());
        }

        List<DevInterceptorInfo> interceptors = beanInfos.getInterceptors();
        if (!interceptors.isEmpty()) {
            links.add(new CardLink.Builder(NAME)
                    .iconName("font-awesome-solid:traffic-light")
                    .component("qwc-arc-interceptors.js")
                    .label(String.valueOf(interceptors.size()))
                    .buildTimeData(GET_INTERCEPTORS, interceptors)
                    .build());
        }

        List<DevDecoratorInfo> decorators = beanInfos.getDecorators();
        if (!decorators.isEmpty()) {
            links.add(new CardLink.Builder(NAME)
                    .iconName("font-awesome-solid:traffic-light")
                    .component("qwc-arc-decorators.js")
                    .label(String.valueOf(decorators.size()))
                    .buildTimeData(GET_DECORATORS, decorators)
                    .build());
        }

        links.add(new CardLink.Builder(NAME)
                .iconName("font-awesome-solid:fire")
                .component("qwc-arc-fired-events.js")
                .build());

        links.add(new CardLink.Builder(NAME)
                .iconName("font-awesome-solid:diagram-project")
                .component("qwc-arc-invocation-trees.js")
                .build());

        int removedComponents = beanInfos.getRemovedComponents();
        if (removedComponents > 0) {
            links.add(new CardLink.Builder(NAME)
                    .iconName("font-awesome-solid:trash-can")
                    .component("qwc-arc-removed-components.js")
                    .label(String.valueOf(removedComponents))
                    .buildTimeData(GET_REMOVED_BEANS, beanInfos.getRemovedBeans())
                    .buildTimeData(GET_REMOVED_COMPONENTS, beanInfos.getRemovedComponents())
                    .buildTimeData(GET_REMOVED_DECORATORS, beanInfos.getRemovedDecorators())
                    .buildTimeData(GET_REMOVED_INTERCEPTORS, beanInfos.getRemovedInterceptors())
                    .build());
        }

        devUICardLinksProducer.produce(new DevUICardLinksBuildItem(NAME, links));
    }

    private List<DevBeanWithInterceptorInfo> toDevBeanWithInterceptorInfo(List<DevBeanInfo> beans, DevBeanInfos devBeanInfos) {
        List<DevBeanWithInterceptorInfo> l = new ArrayList<>();
        for (DevBeanInfo dbi : beans) {
            l.add(new DevBeanWithInterceptorInfo(dbi, devBeanInfos));
        }
        return l;
    }

    private static final String GET_BEANS = "getBeans";
    private static final String GET_OBSERVERS = "getObservers";
    private static final String GET_INTERCEPTORS = "getInterceptors";
    private static final String GET_DECORATORS = "getDecorators";
    private static final String GET_REMOVED_BEANS = "getRemovedBeans";
    private static final String GET_REMOVED_COMPONENTS = "getRemovedComponents";
    private static final String GET_REMOVED_DECORATORS = "getRemovedDecorators";
    private static final String GET_REMOVED_INTERCEPTORS = "getRemovedInterceptors";
}
