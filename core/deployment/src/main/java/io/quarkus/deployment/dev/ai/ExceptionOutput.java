package io.quarkus.deployment.dev.ai;

public record ExceptionOutput(String response, String explanation, String diff, String manipulatedContent) {
}
