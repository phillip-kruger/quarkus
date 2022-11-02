package io.quarkus.arc.deployment.devui;

import java.util.ArrayList;
import java.util.List;

import io.quarkus.arc.deployment.devconsole.DevBeanInfo;
import io.quarkus.arc.deployment.devconsole.DevBeanInfos;
import io.quarkus.arc.deployment.devconsole.DevDecoratorInfo;
import io.quarkus.arc.deployment.devconsole.DevInterceptorInfo;
import io.quarkus.arc.deployment.devconsole.DevObserverInfo;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.spi.page.Page;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;

public class ArcDevUIProcessor {

    private static final String NAME = "ArC";

    @BuildStep(onlyIf = IsDevelopment.class)
    public PageBuildItem pages(ArcBeanInfoBuildItem arcBeanInfoBuildItem) {
        DevBeanInfos beanInfos = arcBeanInfoBuildItem.getBeanInfos();

        PageBuildItem pageBuildItem = new PageBuildItem(NAME);

        List<DevBeanInfo> beans = beanInfos.getBeans();
        if (!beans.isEmpty()) {
            pageBuildItem.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:egg")
                    .componentLink("qwc-arc-beans.js")
                    .label(String.valueOf(beans.size()))
                    .build());

            pageBuildItem.addBuildTimeData(BEANS, toDevBeanWithInterceptorInfo(beans, beanInfos));
        }

        List<DevObserverInfo> observers = beanInfos.getObservers();
        if (!observers.isEmpty()) {
            pageBuildItem.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:eye")
                    .componentLink("qwc-arc-observers.js")
                    .label(String.valueOf(observers.size()))
                    .build());

            pageBuildItem.addBuildTimeData(OBSERVERS, observers);
        }

        List<DevInterceptorInfo> interceptors = beanInfos.getInterceptors();
        if (!interceptors.isEmpty()) {
            pageBuildItem.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:traffic-light")
                    .componentLink("qwc-arc-interceptors.js")
                    .label(String.valueOf(interceptors.size()))
                    .build());

            pageBuildItem.addBuildTimeData(INTERCEPTORS, interceptors);
        }

        List<DevDecoratorInfo> decorators = beanInfos.getDecorators();
        if (!decorators.isEmpty()) {
            pageBuildItem.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:traffic-light")
                    .componentLink("qwc-arc-decorators.js")
                    .label(String.valueOf(decorators.size()))
                    .build());

            pageBuildItem.addBuildTimeData(DECORATORS, decorators);
        }

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:fire")
                .componentLink("qwc-arc-fired-events.js")
                .build());

        pageBuildItem.addPage(Page.webComponentPageBuilder()
                .icon("font-awesome-solid:diagram-project")
                .componentLink("qwc-arc-invocation-trees.js")
                .build());

        int removedComponents = beanInfos.getRemovedComponents();
        if (removedComponents > 0) {
            pageBuildItem.addPage(Page.webComponentPageBuilder()
                    .icon("font-awesome-solid:trash-can")
                    .componentLink("qwc-arc-removed-components.js")
                    .label(String.valueOf(removedComponents))
                    .build());

            pageBuildItem.addBuildTimeData(REMOVED_BEANS, beanInfos.getRemovedBeans());
            pageBuildItem.addBuildTimeData(REMOVED_COMPONENTS, beanInfos.getRemovedComponents());
            pageBuildItem.addBuildTimeData(REMOVED_DECORATORS, beanInfos.getRemovedDecorators());
            pageBuildItem.addBuildTimeData(REMOVED_INTERCEPTORS, beanInfos.getRemovedInterceptors());
        }

        return pageBuildItem;
    }

    private List<DevBeanWithInterceptorInfo> toDevBeanWithInterceptorInfo(List<DevBeanInfo> beans, DevBeanInfos devBeanInfos) {
        List<DevBeanWithInterceptorInfo> l = new ArrayList<>();
        for (DevBeanInfo dbi : beans) {
            l.add(new DevBeanWithInterceptorInfo(dbi, devBeanInfos));
        }
        return l;
    }

    private static final String BEANS = "beans";
    private static final String OBSERVERS = "observers";
    private static final String INTERCEPTORS = "interceptors";
    private static final String DECORATORS = "decorators";
    private static final String REMOVED_BEANS = "removedBeans";
    private static final String REMOVED_COMPONENTS = "removedComponents";
    private static final String REMOVED_DECORATORS = "removedDecorators";
    private static final String REMOVED_INTERCEPTORS = "removedInterceptors";

}
