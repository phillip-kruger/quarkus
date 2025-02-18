package io.quarkus.deployment.dev.ai;

import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.deployment.console.ConsoleCommand;
import java.util.Optional;

/**
 * Add an menu item in the Assistant console menu
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
    private final Optional<Runnable> runnable;        
    
    public AIConsoleBuildItem(String label, char key, Optional<String> systemMessage, String userMessage, Optional<String> initMessage, Optional<String> messageFormat, Optional<Runnable> runnable){
        this.label = label;
        this.key = key;
        this.systemMessage = systemMessage;
        this.userMessage = userMessage;
        this.initMessage = initMessage;
        this.messageFormat = messageFormat;
        this.runnable = runnable;
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
        this.runnable = Optional.empty();
    }

    public ConsoleCommand getConsoleCommand() {
        return consoleCommand;
    }
    
    public String getLabel() {
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

    public Optional<Runnable> getRunnable() {
        return runnable;
    }
}
