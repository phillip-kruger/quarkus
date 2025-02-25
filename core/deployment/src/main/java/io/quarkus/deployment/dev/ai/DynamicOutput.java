package io.quarkus.deployment.dev.ai;

public record DynamicOutput<T>(T response) {

    @Override
    public String toString() {
        if (response == null) {
            return null;
        }
        // For primitives, their wrappers, and String, return the simple string.
        if (response instanceof String ||
                response instanceof Number ||
                response instanceof Boolean ||
                response instanceof Character) {
            return String.valueOf(response);
        }
        return response.toString(); // We don't have a Object->Json lib available here. Maybe move to vertx-http ?
    }
}
