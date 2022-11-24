package io.quarkus.devui.runtime;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.impl.StaticHandlerImpl;

/**
 * Handler to load mvnpm jars
 * TODO: Change this to load from deployment classpath
 */
public class MvnpmHandler extends StaticHandlerImpl {
    public static volatile ClassLoader classLoader;

    public MvnpmHandler() {
        super.setCachingEnabled(true);
    }

    @Override
    public void handle(RoutingContext event) {
        // Find the "filename" and see if it has a file extension
        String fullPath = event.normalizedPath();
        String parts[] = fullPath.split("/");
        String fileName = parts[parts.length - 1];
        if (fileName.contains(".")) {
            super.handle(event);
        } else {
            event.reroute(fullPath + ".js"); // Default to js. Some modules reference other module without the extension
        }
    }
}
