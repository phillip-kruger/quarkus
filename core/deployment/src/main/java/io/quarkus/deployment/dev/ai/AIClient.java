package io.quarkus.deployment.dev.ai;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AIClient {

    // Workspace update (manipulate)
    default CompletableFuture<ManipulationOutput> workspaceUpdate(String userMessage, Path path, String content) {
        return workspaceUpdate(Optional.empty(), userMessage, Map.of(), path, content);
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(Optional<String> systemMessage, String userMessage,
            Path path, String content) {
        return workspaceUpdate(systemMessage, userMessage, Map.of(), Map.of(path, content));
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(String userMessage, Map<Path, String> pathAndContent) {
        return workspaceUpdate(Optional.empty(), userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(Optional<String> systemMessage, String userMessage,
            Map<Path, String> pathAndContent) {
        return workspaceUpdate(systemMessage, userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(String userMessage, Map<String, String> variables, Path path,
            String content) {
        return workspaceUpdate(Optional.empty(), userMessage, variables, path, content);
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Path path, String content) {
        return workspaceUpdate(systemMessage, userMessage, variables, Map.of(path, content));
    }

    default CompletableFuture<ManipulationOutput> workspaceUpdate(String userMessage, Map<String, String> variables,
            Map<Path, String> pathAndContent) {
        return workspaceUpdate(Optional.empty(), userMessage, variables, pathAndContent);
    }

    public CompletableFuture<ManipulationOutput> workspaceUpdate(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables, Map<Path, String> pathAndContent);

    // Workspace create (generate)
    default CompletableFuture<GenerationOutput> workspaceCreate(String userMessage, Path path, String content) {
        return workspaceCreate(Optional.empty(), userMessage, Map.of(), path, content);
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(Optional<String> systemMessage, String userMessage,
            Path path, String content) {
        return workspaceCreate(systemMessage, userMessage, Map.of(), Map.of(path, content));
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(String userMessage, Map<Path, String> pathAndContent) {
        return workspaceCreate(Optional.empty(), userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(Optional<String> systemMessage, String userMessage,
            Map<Path, String> pathAndContent) {
        return workspaceCreate(systemMessage, userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(String userMessage, Map<String, String> variables, Path path,
            String content) {
        return workspaceCreate(Optional.empty(), userMessage, variables, path, content);
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Path path, String content) {
        return workspaceCreate(systemMessage, userMessage, variables, Map.of(path, content));
    }

    default CompletableFuture<GenerationOutput> workspaceCreate(String userMessage, Map<String, String> variables,
            Map<Path, String> pathAndContent) {
        return workspaceCreate(Optional.empty(), userMessage, variables, pathAndContent);
    }

    public CompletableFuture<GenerationOutput> workspaceCreate(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Map<Path, String> pathAndContent);

    // Workspace Read (interpret)
    default CompletableFuture<InterpretationOutput> workspaceRead(String userMessage, Path path, String content) {
        return workspaceRead(Optional.empty(), userMessage, Map.of(), path, content);
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(Optional<String> systemMessage, String userMessage,
            Path path, String content) {
        return workspaceRead(systemMessage, userMessage, Map.of(), Map.of(path, content));
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(String userMessage, Map<Path, String> pathAndContent) {
        return workspaceRead(Optional.empty(), userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(Optional<String> systemMessage, String userMessage,
            Map<Path, String> pathAndContent) {
        return workspaceRead(systemMessage, userMessage, Map.of(), pathAndContent);
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(String userMessage, Map<String, String> variables, Path path,
            String content) {
        return workspaceRead(Optional.empty(), userMessage, variables, path, content);
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Path path, String content) {
        return workspaceRead(systemMessage, userMessage, variables, Map.of(path, content));
    }

    default CompletableFuture<InterpretationOutput> workspaceRead(String userMessage, Map<String, String> variables,
            Map<Path, String> pathAndContent) {
        return workspaceRead(Optional.empty(), userMessage, variables, pathAndContent);
    }

    public CompletableFuture<InterpretationOutput> workspaceRead(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables, Map<Path, String> pathAndContent);

    // Workspace Dynamic (interpret)
    default CompletableFuture<DynamicOutput> workspaceDynamic(String userMessage, Map<String, String> variables, Path path,
            String content) {
        return workspaceDynamic(Optional.empty(), userMessage, variables, path, content);
    }

    default CompletableFuture<DynamicOutput> workspaceDynamic(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Path path, String content) {
        return workspaceDynamic(systemMessage, userMessage, variables, Map.of(path, content));
    }

    default CompletableFuture<DynamicOutput> workspaceDynamic(String userMessage, Map<String, String> variables,
            Map<Path, String> pathAndContent) {
        return workspaceDynamic(Optional.empty(), userMessage, variables, pathAndContent);
    }

    public CompletableFuture<DynamicOutput> workspaceDynamic(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables,
            Map<Path, String> pathAndContent);

    // Exception
    default CompletableFuture<ExceptionOutput> exception(String userMessage, String stacktrace, Path path, String content) {
        return exception(Optional.empty(), userMessage, stacktrace, path, content);
    }

    public CompletableFuture<ExceptionOutput> exception(Optional<String> systemMessage, String userMessage, String stacktrace,
            Path path, String content);

    // Dynamic (generic)
    default CompletableFuture<DynamicOutput> dynamic(String userMessage) {
        return dynamic(Optional.empty(), userMessage, Map.of());
    }

    default CompletableFuture<DynamicOutput> dynamic(Optional<String> systemMessage, String userMessage) {
        return dynamic(systemMessage, userMessage, Map.of());
    }

    default CompletableFuture<DynamicOutput> dynamic(String userMessage, Map<String, String> variables) {
        return dynamic(Optional.empty(), userMessage, variables);
    }

    public CompletableFuture<DynamicOutput> dynamic(Optional<String> systemMessage, String userMessage,
            Map<String, String> variables);
}
