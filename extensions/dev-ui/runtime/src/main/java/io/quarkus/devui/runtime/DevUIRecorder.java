package io.quarkus.devui.runtime;

import java.util.List;
import java.util.Map;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.devui.runtime.comms.JsonRpcRouter;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethodName;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.webjar.runtime.FileSystemStaticHandler;
import io.quarkus.webjar.runtime.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class DevUIRecorder {

    public void createJsonRpcRouter(BeanContainer beanContainer,
            Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap) {
        JsonRpcRouter jsonRpcRouter = beanContainer.instance(JsonRpcRouter.class);
        jsonRpcRouter.populateJsonRPCMethods(extensionMethodsMap);
    }

    public Handler<RoutingContext> mvnpmHandler() {
        MvnpmHandler.classLoader = Thread.currentThread().getContextClassLoader();
        return new MvnpmHandler();
    }

    public Handler<RoutingContext> uiHandler(String finalDestination,
            String path,
            List<FileSystemStaticHandler.StaticWebRootConfiguration> webRootConfigurations,
            ShutdownContext shutdownContext) {

        WebJarStaticHandler handler = new WebJarStaticHandler(finalDestination, path, webRootConfigurations);
        shutdownContext.addShutdownTask(new ShutdownContext.CloseRunnable(handler));
        return handler;
    }

    public Handler<RoutingContext> vaadinRouterHandler(String basePath) {
        return new VaadinRouterHandler(basePath);
    }

    public Handler<RoutingContext> buildTimeStaticHandler(String basePath, Map<String, String> urlAndPath) {
        return new DevUIBuildTimeStaticHandler(basePath, urlAndPath);
    }

    public Handler<RoutingContext> communicationHandler() {
        return new DevUIWebSocket();
    }

}
