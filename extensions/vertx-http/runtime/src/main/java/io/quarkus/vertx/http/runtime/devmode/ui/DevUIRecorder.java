package io.quarkus.vertx.http.runtime.devmode.ui;

import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

@Recorder
public class DevUIRecorder {

    public Handler<RoutingContext> uiHandler(String jarFileUri, String relativeRootDirectory, String relativeRootPath) {
        return new DevUIStaticHandler(jarFileUri, relativeRootDirectory, relativeRootPath);
    }
}
