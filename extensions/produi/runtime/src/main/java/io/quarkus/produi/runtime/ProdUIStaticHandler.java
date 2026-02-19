package io.quarkus.produi.runtime;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Handler for serving static Prod UI content.
 */
public class ProdUIStaticHandler implements Handler<RoutingContext> {

    private final String prodUIPath;
    private final String indexContent;

    public ProdUIStaticHandler(String prodUIPath, String indexContent) {
        this.prodUIPath = prodUIPath;
        this.indexContent = indexContent;
    }

    @Override
    public void handle(RoutingContext routingContext) {
        String path = routingContext.normalizedPath();

        // Handle index.html and root path
        if (path.endsWith("/") || path.endsWith("/index.html") || path.equals(prodUIPath)) {
            routingContext.response()
                    .putHeader("Content-Type", "text/html; charset=UTF-8")
                    .putHeader("Cache-Control", "no-cache, no-store, must-revalidate")
                    .putHeader("X-Frame-Options", "SAMEORIGIN")
                    .putHeader("X-Content-Type-Options", "nosniff")
                    .setStatusCode(HttpResponseStatus.OK.code())
                    .end(indexContent);
            return;
        }

        // For other static resources, delegate to the next handler
        routingContext.next();
    }
}
