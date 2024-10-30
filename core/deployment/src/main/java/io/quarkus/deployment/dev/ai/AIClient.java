package io.quarkus.deployment.dev.ai;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AIClient {
    default CompletableFuture<String> request(String method, Map<String, String> params) {
        return request(method, Optional.empty(), params);
    }

    public CompletableFuture<String> request(String method, Optional<String> extraContext, Map<String, String> params);
}
