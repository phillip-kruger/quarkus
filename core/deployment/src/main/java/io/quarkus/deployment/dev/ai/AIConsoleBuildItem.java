package io.quarkus.deployment.dev.ai;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;

/**
 * Add a menu item in the Assistant console menu
 */
public final class AIConsoleBuildItem extends MultiBuildItem {
    private final ConsoleCommand consoleCommand;

    private final String label;
    private final char key;
    private final Optional<String> systemMessage;
    private final String userMessage;
    private final Optional<String> initMessage;
    private final Optional<String> messageFormat;
    private final Map<String, String> variables;
    private final Optional<Function<AIClient, String>> function;

    public AIConsoleBuildItem(ConsoleCommand consoleCommand) {
        this.consoleCommand = consoleCommand;
        this.function = null;
        this.label = null;
        this.key = '-';
        this.systemMessage = Optional.empty();
        this.userMessage = null;
        this.initMessage = Optional.empty();
        this.messageFormat = Optional.empty();
        this.variables = Map.of();
    }

    private AIConsoleBuildItem(Builder builder) {
        this.label = builder.label;
        this.key = builder.key;
        this.systemMessage = builder.systemMessage;
        this.userMessage = builder.userMessage;
        this.initMessage = builder.initMessage;
        this.messageFormat = builder.messageFormat;
        this.variables = builder.variables;
        this.consoleCommand = null;
        this.function = builder.function;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String label;
        private char key = Character.MIN_VALUE;
        private Optional<String> systemMessage = Optional.empty();
        private String userMessage;
        private Optional<String> initMessage = Optional.empty();
        private Optional<String> messageFormat = Optional.empty();
        private Map<String, String> variables = Map.of();
        private Optional<Function<AIClient, String>> function;

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder key(char key) {
            this.key = key;
            return this;
        }

        public Builder systemMessage(Optional<String> systemMessage) {
            this.systemMessage = systemMessage;
            return this;
        }

        public Builder userMessage(String userMessage) {
            this.userMessage = userMessage;
            return this;
        }

        public Builder initMessage(Optional<String> initMessage) {
            this.initMessage = initMessage;
            return this;
        }

        public Builder messageFormat(Optional<String> messageFormat) {
            this.messageFormat = messageFormat;
            return this;
        }

        public Builder variables(Map<String, String> variables) {
            this.variables = variables;
            return this;
        }

        public Builder function(Function<AIClient, String> function) {
            this.function = Optional.of(function);
            return this;
        }

        public AIConsoleBuildItem build() {
            return new AIConsoleBuildItem(this);
        }
    }

    public ConsoleCommand getConsoleCommand() {
        return consoleCommand;
    }

    public String getLabel() {
        return consoleCommand != null ? consoleCommand.getDescription() : label;
    }

    public char getKey() {
        return key;
    }

    public Optional<String> getSystemMessage() {
        return systemMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public Optional<String> getInitMessage() {
        return initMessage;
    }

    public Optional<String> getMessageFormat() {
        return messageFormat;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    public Optional<Function<AIClient, String>> getFunction() {
        return function;
    }
}
