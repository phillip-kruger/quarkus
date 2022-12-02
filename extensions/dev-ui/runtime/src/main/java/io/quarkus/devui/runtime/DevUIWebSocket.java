package io.quarkus.devui.runtime;

import javax.enterprise.inject.spi.CDI;

import org.jboss.logging.Logger;

import io.quarkus.devui.runtime.comms.JsonRpcRouter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;

/**
 * This is the main entry point for Dev UI Json RPC communication
 */
public class DevUIWebSocket implements Handler<RoutingContext> {
    private static final Logger LOG = Logger.getLogger(DevUIWebSocket.class.getName());

    private JsonRpcRouter jsonRpcRouter;

    public DevUIWebSocket() {
        this.jsonRpcRouter = CDI.current().select(JsonRpcRouter.class).get();
    }

    @Override
    public void handle(RoutingContext event) {
        if (WEBSOCKET.equalsIgnoreCase(event.request().getHeader(UPGRADE)) && !event.request().isEnded()) {
            event.request().toWebSocket(new Handler<AsyncResult<ServerWebSocket>>() {
                @Override
                public void handle(AsyncResult<ServerWebSocket> event) {
                    if (event.succeeded()) {
                        ServerWebSocket socket = event.result();
                        setSocket(socket);
                    } else {
                        LOG.debug("Failed to connect to dev ui communication server", event.cause());
                    }
                }
            });
        } else {
            event.next();
        }
    }

    private void setSocket(ServerWebSocket session) {
        this.jsonRpcRouter.setSocket(session);
    }

    private static final String UPGRADE = "Upgrade";
    private static final String WEBSOCKET = "websocket";
}
