package io.quarkus.vertx.http.deployment.devui;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.ShutdownContextBuildItem;
import io.quarkus.devui.deployment.DevUIRoutesBuildItem;
import io.quarkus.devui.deployment.spi.DevUIContent;
import io.quarkus.devui.deployment.spi.buildtime.StaticContentBuildItem;
import io.quarkus.devui.runtime.DevUIRecorder;
import io.quarkus.qute.Engine;
import io.quarkus.qute.Template;
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
    DevUIQuteEngineBuildItem devUIQuteEngine() {
        Engine engine = Engine.builder()
                .addDefaults()
                .strictRendering(true)
                //.addLocator(id -> locateTemplate(id)) // We would need this for runtime ? Or paths ?
                .build();

        return new DevUIQuteEngineBuildItem(engine);
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.STATIC_INIT)
    void registerDevUiHandler(
            DevUIQuteEngineBuildItem engineBuildItem,
            List<DevUIRoutesBuildItem> devUIRoutesBuildItems,
            List<StaticContentBuildItem> staticContentBuildItem,
            BuildProducer<RouteBuildItem> routeProducer,
            DevUIRecorder recorder,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            ShutdownContextBuildItem shutdownContext) throws IOException {

        // Websocket for JsonRPC comms
        routeProducer.produce(
                nonApplicationRootPathBuildItem
                        .routeBuilder().route(DEVUI + SLASH + JSONRPC)
                        .handler(recorder.communicationHandler())
                        .build());

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

        String basepath = nonApplicationRootPathBuildItem.resolvePath(DEVUI);
        // For static content generated at build time
        Engine engine = engineBuildItem.getEngine();
        for (StaticContentBuildItem buildTimeContent : staticContentBuildItem) {

            Map<String, String> urlAndPath = new HashMap<>();
            if (buildTimeContent.isInternal()) {
                List<DevUIContent> content = buildTimeContent.getContent();
                for (DevUIContent c : content) {
                    Template t = engine.parse(new String(c.getTemplate()));
                    String parsedContent = t.data(c.getData()).render();

                    Path tempFile = Files.createTempFile("quarkus-dev-ui-", c.getFileName());
                    Files.write(tempFile, parsedContent.getBytes(StandardCharsets.UTF_8));

                    urlAndPath.put(c.getFileName(), tempFile.toString());
                }
                Handler<RoutingContext> buildTimeStaticHandler = recorder.buildTimeStaticHandler(basepath, urlAndPath);
                routeProducer.produce(
                        nonApplicationRootPathBuildItem.routeBuilder().route(basepath + SLASH_ALL)
                                .handler(buildTimeStaticHandler)
                                .build());
            } else {
                // TODO: Handle extension content

                System.err.println(">>>>>>>>>>>>>>> IGNORING " + buildTimeContent.getExtensionName()
                        + buildTimeContent.getExtensionPathName());

            }
        }

        // For the Vaadin router (So that bookmarks/url refreshes work)
        for (DevUIRoutesBuildItem devUIRoutesBuildItem : devUIRoutesBuildItems) {
            String route = devUIRoutesBuildItem.getPath();

            basepath = nonApplicationRootPathBuildItem.resolvePath(route);
            Handler<RoutingContext> routerhandler = recorder.routerHandler(basepath);
            routeProducer.produce(
                    nonApplicationRootPathBuildItem.routeBuilder().route(route + SLASH_ALL).handler(routerhandler).build());
        }
    }
}
