package io.quarkus.produi.runtime;

import java.util.Map;

import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.produi.runtime.comms.ProdUIJsonRpcRouter;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.runtime.annotations.Recorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Recorder for Prod UI runtime initialization.
 */
@Recorder
public class ProdUIRecorder {

    /**
     * Creates and populates the JSON-RPC router with runtime methods.
     */
    public void createJsonRpcRouter(BeanContainer beanContainer,
            Map<String, JsonRpcMethod> runtimeMethods,
            Map<String, JsonRpcMethod> runtimeSubscriptions) {

        ProdUIJsonRpcRouter jsonRpcRouter = beanContainer.beanInstance(ProdUIJsonRpcRouter.class);
        jsonRpcRouter.populateJsonRpcEndpoints(runtimeMethods, runtimeSubscriptions);
    }

    /**
     * Creates the WebSocket handler for JSON-RPC communication.
     */
    public Handler<RoutingContext> prodUIWebSocketHandler() {
        return new ProdUIWebSocketHandler();
    }

    /**
     * Creates a handler for serving static Prod UI content.
     */
    public Handler<RoutingContext> staticHandler(String prodUIPath, String indexContent) {
        return new ProdUIStaticHandler(prodUIPath, indexContent);
    }
}
