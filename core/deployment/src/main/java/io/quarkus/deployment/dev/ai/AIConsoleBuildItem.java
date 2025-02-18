package io.quarkus.deployment.dev.ai;

import java.util.Optional;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import java.util.Map;

/**
 * Add an menu item in the Assistant console menu
 *
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public final class AIConsoleBuildItem extends MultiBuildItem {
    private final ConsoleCommand consoleCommand;
    private final String label;
    private final char key;
    private final Optional<String> systemMessage;
    private final String userMessage;
    private final Optional<String> initMessage;
    private final Optional<String> messageFormat;
    private final Map<String,String> variables;

    public AIConsoleBuildItem(String label, char key, Optional<String> systemMessage, String userMessage,
            Optional<String> initMessage, Optional<String> messageFormat, Map<String,String> variables) {
        this.label = label;
        this.key = key;
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
        this.initMessage = initMessage;
        this.messageFormat = messageFormat;
        this.variables = variables;
        this.consoleCommand = null;
    }

    public AIConsoleBuildItem(ConsoleCommand consoleCommand) {
        this.consoleCommand = consoleCommand;
        this.label = null;
        this.key = Character.MIN_VALUE;
        this.systemMessage = Optional.empty();
        this.userMessage = null;
        this.initMessage = Optional.empty();
        this.messageFormat = Optional.empty();
        this.variables = Map.of();
    }

    public ConsoleCommand getConsoleCommand() {
        return consoleCommand;
    }

    public String getLabel() {
        if (consoleCommand != null)
            return consoleCommand.getDescription();
        return label;
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
}
