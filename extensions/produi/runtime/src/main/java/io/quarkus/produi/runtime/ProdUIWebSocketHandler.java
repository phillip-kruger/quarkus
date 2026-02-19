package io.quarkus.produi.runtime;

import jakarta.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.quarkus.produi.runtime.comms.ProdUIJsonRpcRouter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;

/**
 * WebSocket handler for Prod UI JSON-RPC communication.
 */
public class ProdUIWebSocketHandler implements Handler<RoutingContext> {
    private static final Logger LOG = Logger.getLogger(ProdUIWebSocketHandler.class.getName());
    private static final String UPGRADE = "Upgrade";
    private static final String WEBSOCKET = "websocket";

    @Override
    public void handle(RoutingContext event) {
        if (WEBSOCKET.equalsIgnoreCase(event.request().getHeader(UPGRADE)) && !event.request().isEnded()) {
            event.request().toWebSocket(new Handler<AsyncResult<ServerWebSocket>>() {
                @Override
                public void handle(AsyncResult<ServerWebSocket> event) {
                    if (event.succeeded()) {
                        ServerWebSocket socket = event.result();
                        addSocket(socket);
                    } else {
                        LOG.debug("Failed to connect to prod ui ws server", event.cause());
                    }
                }
            });
            return;
        }
        event.next();
    }

    private void addSocket(ServerWebSocket session) {
        try {
            ProdUIJsonRpcRouter jsonRpcRouter = CDI.current().select(ProdUIJsonRpcRouter.class).get();
            jsonRpcRouter.addSocket(session);
        } catch (IllegalStateException ise) {
            LOG.debug("Failed to connect to prod ui ws server, " + ise.getMessage());
        }
    }
}
