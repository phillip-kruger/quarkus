package io.quarkus.devui.runtime;

import java.util.List;
import java.util.Map;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRouter;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.webjar.runtime.FileSystemStaticHandler;
import io.quarkus.webjar.runtime.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class DevUIRecorder {

    public void createJsonRpcRouter(BeanContainer beanContainer, Map<String, String> jsonResponses) {
        JsonRpcRouter jsonRpcRouter = beanContainer.instance(JsonRpcRouter.class);
        jsonRpcRouter.buildTimeData(jsonResponses);
    }

    public Handler<RoutingContext> uiHandler(String finalDestination,
            String path,
            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations,
            ShutdownContext shutdownContext) {

        WebJarStaticHandler handler = new WebJarStaticHandler(finalDestination, path, webRootConfigurations);
        shutdownContext.addShutdownTask(new ShutdownContext.CloseRunnable(handler));
        return handler;
    }

    public Handler<RoutingContext> routerHandler(String basePath) {
        return new DevUIRouterHandler(basePath);
    }

    public Handler<RoutingContext> buildTimeStaticHandler(String basePath, Map<String, String> urlAndPath) {
        return new DevUIBuildTimeStaticHandler(basePath, urlAndPath);
    }

    public Handler<RoutingContext> communicationHandler() {
        return new DevUIWebSocket();
    }
}
