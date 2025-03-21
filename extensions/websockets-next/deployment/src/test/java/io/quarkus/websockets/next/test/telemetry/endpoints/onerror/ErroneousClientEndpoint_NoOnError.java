package io.quarkus.websockets.next.test.telemetry.endpoints.onerror;

import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/client-error-no-on-error")
public class ErroneousClientEndpoint_NoOnError {

    @OnTextMessage
    public Multi<String> onMessage(String message) {
        return Multi.createFrom().items("echo 0: " + message, "echo 1: " + message);
    }

}
