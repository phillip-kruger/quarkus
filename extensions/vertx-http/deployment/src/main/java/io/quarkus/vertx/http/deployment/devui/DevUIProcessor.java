package io.quarkus.vertx.http.deployment.devui;

import java.io.IOException;
import java.util.List;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.devui.deployment.DevUIRoutesBuildItem;
import io.quarkus.devui.runtime.DevUIRecorder;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Processor for Dev UI
 */
public class DevUIProcessor {

    private static final String DEVUI = "dev-ui";
    private static final String SLASH = "/";
    private static final String SLASH_ALL = SLASH + "*";
    private static final String JSONRPC = "json-rpc-ws";

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerDevUiHandler(
            List<DevUIRoutesBuildItem> devUIRoutesBuildItems,
            BuildProducer<RouteBuildItem> routeProducer,
            DevUIRecorder recorder,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            ShutdownContextBuildItem shutdownContext) throws IOException {

        // Websocket for JsonRPC comms
        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder().route(DEVUI + SLASH + JSONRPC)
                .handler(recorder.communicationHandler()).build());

        // Static handler for components
        for (DevUIRoutesBuildItem devUIRoutesBuildItem : devUIRoutesBuildItems) {
            String route = devUIRoutesBuildItem.getPath();

            String path = nonApplicationRootPathBuildItem.resolvePath(route);
            Handler<RoutingContext> uihandler = recorder.uiHandler(
                    devUIRoutesBuildItem.getFinalDestination(),
                    path,
                    devUIRoutesBuildItem.getWebRootConfigurations(),
                    shutdownContext);

            // TODO: displayOnNotFoundPage(DESCRIPTION)

            routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder().route(route).handler(uihandler).build());
            routeProducer.produce(
                    nonApplicationRootPathBuildItem.routeBuilder().route(route + SLASH_ALL).handler(uihandler).build());
        }

        // For the Vaadin router
        for (DevUIRoutesBuildItem devUIRoutesBuildItem : devUIRoutesBuildItems) {
            String route = devUIRoutesBuildItem.getPath();

            String basepath = nonApplicationRootPathBuildItem.resolvePath(route);
            Handler<RoutingContext> routerhandler = recorder.routerHandler(basepath);
            routeProducer.produce(
                    nonApplicationRootPathBuildItem.routeBuilder().route(route + SLASH_ALL).handler(routerhandler).build());
        }
    }
}
