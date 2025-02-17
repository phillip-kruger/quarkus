package io.quarkus.deployment.dev.ai;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AIClient {
    @Deprecated
    default CompletableFuture<String> request(String method, Map<String, String> params) {
        return request(method, Optional.empty(), params);
    }

    @Deprecated
    public CompletableFuture<String> request(String method, Optional<String> extraContext, Map<String, String> params);

    default CompletableFuture<ManipulationOutput> manipulate(String userMessage, Path path, String content) {
        return manipulate(Optional.empty(), userMessage, path, content);
    }

    public CompletableFuture<ManipulationOutput> manipulate(Optional<String> systemMessage, String userMessage,
            Path path, String content);

    default CompletableFuture<GenerationOutput> generate(String userMessage, Path path, String content) {
        return generate(Optional.empty(), userMessage, path, content);
    }

    public CompletableFuture<GenerationOutput> generate(Optional<String> systemMessage, String userMessage,
            Path path, String content);

    default CompletableFuture<InterpretationOutput> interpret(String userMessage, Path path, String content) {
        return interpret(Optional.empty(), userMessage, path, content);
    }

    public CompletableFuture<InterpretationOutput> interpret(Optional<String> systemMessage, String userMessage,
            Path path, String content);

    default CompletableFuture<ExceptionOutput> exception(String userMessage, String stacktrace, Path path, String content) {
        return exception(Optional.empty(), userMessage, stacktrace, path, content);
    }

    public CompletableFuture<ExceptionOutput> exception(Optional<String> systemMessage, String userMessage, String stacktrace,
            Path path, String content);

}
