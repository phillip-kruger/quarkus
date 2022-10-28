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
import io.quarkus.devui.deployment.spi.page.WebComponentPage;
import io.quarkus.devui.deployment.spi.page.WebComponentsPageBuildItem;

public class ArcDevUIProcessor {

    private static final String NAME = "ArC";

    @BuildStep(onlyIf = IsDevelopment.class)
    public void cardLinks(ArcBeanInfoBuildItem arcBeanInfoBuildItem,
            BuildProducer<WebComponentsPageBuildItem> webComponentsPageProducer) {
        List<WebComponentPage> componentPages = new ArrayList<>();

        DevBeanInfos beanInfos = arcBeanInfoBuildItem.getBeanInfos();

        List<DevBeanInfo> beans = beanInfos.getBeans();
        if (!beans.isEmpty()) {
            componentPages.add(new WebComponentPage.Builder(NAME)
                    .iconName("font-awesome-solid:egg")
                    .webComponent("qwc-arc-beans.js")
                    .label(String.valueOf(beans.size()))
                    .buildTimeData(GET_BEANS, toDevBeanWithInterceptorInfo(beans, beanInfos))
                    .build());
        }

        List<DevObserverInfo> observers = beanInfos.getObservers();
        if (!observers.isEmpty()) {
            componentPages.add(new WebComponentPage.Builder(NAME)
                    .iconName("font-awesome-solid:eye")
                    .webComponent("qwc-arc-observers.js")
                    .label(String.valueOf(observers.size()))
                    .buildTimeData(GET_OBSERVERS, observers)
                    .build());
        }

        List<DevInterceptorInfo> interceptors = beanInfos.getInterceptors();
        if (!interceptors.isEmpty()) {
            componentPages.add(new WebComponentPage.Builder(NAME)
                    .iconName("font-awesome-solid:traffic-light")
                    .webComponent("qwc-arc-interceptors.js")
                    .label(String.valueOf(interceptors.size()))
                    .buildTimeData(GET_INTERCEPTORS, interceptors)
                    .build());
        }

        List<DevDecoratorInfo> decorators = beanInfos.getDecorators();
        if (!decorators.isEmpty()) {
            componentPages.add(new WebComponentPage.Builder(NAME)
                    .iconName("font-awesome-solid:traffic-light")
                    .webComponent("qwc-arc-decorators.js")
                    .label(String.valueOf(decorators.size()))
                    .buildTimeData(GET_DECORATORS, decorators)
                    .build());
        }

        componentPages.add(new WebComponentPage.Builder(NAME)
                .iconName("font-awesome-solid:fire")
                .webComponent("qwc-arc-fired-events.js")
                .build());

        componentPages.add(new WebComponentPage.Builder(NAME)
                .iconName("font-awesome-solid:diagram-project")
                .webComponent("qwc-arc-invocation-trees.js")
                .build());

        int removedComponents = beanInfos.getRemovedComponents();
        if (removedComponents > 0) {
            componentPages.add(new WebComponentPage.Builder(NAME)
                    .iconName("font-awesome-solid:trash-can")
                    .webComponent("qwc-arc-removed-components.js")
                    .label(String.valueOf(removedComponents))
                    .buildTimeData(GET_REMOVED_BEANS, beanInfos.getRemovedBeans())
                    .buildTimeData(GET_REMOVED_COMPONENTS, beanInfos.getRemovedComponents())
                    .buildTimeData(GET_REMOVED_DECORATORS, beanInfos.getRemovedDecorators())
                    .buildTimeData(GET_REMOVED_INTERCEPTORS, beanInfos.getRemovedInterceptors())
                    .build());
        }

        webComponentsPageProducer.produce(new WebComponentsPageBuildItem(NAME, componentPages));
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
