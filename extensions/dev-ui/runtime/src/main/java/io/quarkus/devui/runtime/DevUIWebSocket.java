package io.quarkus.devui.runtime;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.spi.CDI;

import io.quarkus.devui.runtime.jsonrpc.JsonRpcRouter;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.RoutingContext;

/**
 * This is the main entry point for Dev UI Json RPC communication
 */
public class DevUIWebSocket implements Handler<RoutingContext> {
    private static final Logger log = Logger.getLogger(DevUIWebSocket.class.getName());

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
                        SessionState state = new SessionState(socket);

                        socket.closeHandler(new Handler<Void>() {
                            @Override
                            public void handle(Void event) {
                                onStop(state);
                            }
                        });
                        socket.textMessageHandler(new Handler<String>() {
                            @Override
                            public void handle(String event) {
                                onMessage(event, state);
                            }
                        });
                        onStart(state);
                    } else {
                        log.log(Level.SEVERE, "Failed to connect to dev ui communication server", event.cause());
                    }
                }
            });
        } else {
            event.next();
        }
    }

    private void onMessage(String message, SessionState session) {
        session.writeTextMessage(jsonRpcRouter.route(message));
    }

    private void onStop(SessionState session) {
        // ?
    }

    private void onStart(SessionState session) {
        // ?
    }

    static class SessionState {
        ServerWebSocket session;
        String id;

        public SessionState(ServerWebSocket session) {
            this.session = session;
            this.id = UUID.randomUUID().toString();
        }

        public void writeTextMessage(String message) {
            session.writeTextMessage(message);
        }
    }

    private static final String UPGRADE = "Upgrade";
    private static final String WEBSOCKET = "websocket";
}
