package io.quarkus.devui.runtime;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethodName;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRouter;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.webjar.runtime.FileSystemStaticHandler;
import io.quarkus.webjar.runtime.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class DevUIRecorder {

    public RuntimeValue<?> createJsonRpcProvider(Class c) {
        try {
            @SuppressWarnings("unchecked")
            Object instance = c.getConstructor().newInstance();
            return new RuntimeValue<>(instance);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void createJsonRpcRouter(BeanContainer beanContainer,
            Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap) {
        JsonRpcRouter jsonRpcRouter = beanContainer.instance(JsonRpcRouter.class);
        jsonRpcRouter.setExtensionMethodsMap(extensionMethodsMap);
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
