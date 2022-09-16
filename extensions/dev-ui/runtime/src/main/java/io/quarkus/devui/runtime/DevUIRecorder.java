package io.quarkus.devui.runtime;

import java.util.List;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionsService;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;
import io.quarkus.webjar.runtime.FileSystemStaticHandler;
import io.quarkus.webjar.runtime.WebJarStaticHandler;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class DevUIRecorder {

    public void createExtensionService(BeanContainer beanContainer, List<Extension> activeExtensions,
            List<Extension> inactiveExtensions) {
        ExtensionsService extensionsService = beanContainer.instance(ExtensionsService.class);
        extensionsService.initialize(activeExtensions, inactiveExtensions);
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

    public Handler<RoutingContext> communicationHandler() {
        return new DevUIWebSocket();
    }
}
