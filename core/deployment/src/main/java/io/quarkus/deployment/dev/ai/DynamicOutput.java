package io.quarkus.deployment.dev.ai;

public record DynamicOutput(String jsonResponse) {

    @Override
    public String toString() {
        return jsonResponse;
    }
}
