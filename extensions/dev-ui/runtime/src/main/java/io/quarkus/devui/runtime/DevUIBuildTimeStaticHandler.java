package io.quarkus.devui.runtime;

import java.util.Map;

import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler to return the "static" content created a build time
 */
public class DevUIBuildTimeStaticHandler implements Handler<RoutingContext> {
    private Map<String, String> pathAndContentMap;
    private String basePath; // Like /q/dev-ui

    public DevUIBuildTimeStaticHandler() {

    }

    public DevUIBuildTimeStaticHandler(String basePath, Map<String, String> pathAndContentMap) {
        this.basePath = basePath;
        this.pathAndContentMap = pathAndContentMap;
    }

    public Map<String, String> getPathAndContentMap() {
        return pathAndContentMap;
    }

    public void setPathAndContentMap(Map<String, String> pathAndContentMap) {
        this.pathAndContentMap = pathAndContentMap;
    }

    public String getBasePath() {
        return basePath;
    }

    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public void handle(RoutingContext event) {
        String normalizedPath = event.normalizedPath();
        if (normalizedPath.contains(SLASH)) {
            int si = normalizedPath.lastIndexOf(SLASH) + 1;
            String path = normalizedPath.substring(0, si);
            System.out.println(">>>>>>>>> basePath = " + basePath);
            System.out.println(">>>>>>>>> PATH = " + path);
            String fileName = normalizedPath.substring(si);
            System.out.println(">>>>>>>>> fileName = " + fileName);
            // TODO: Handle params ?
            if (path.startsWith(basePath) && this.pathAndContentMap.containsKey(fileName)) {
                String content = this.pathAndContentMap.get(fileName);
                event.response()
                        .setStatusCode(STATUS)
                        .setStatusMessage(OK)
                        // .headers() // TODO: Add content type ?
                        .end(Buffer.buffer(content));
            } else {
                event.next();
            }
        } else {
            event.next();
        }

    }

    private static final int STATUS = 200;
    private static final String OK = "OK";
    private static final String SLASH = "/";
}
