package io.quarkus.deployment.dev.ai.workspace;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is an action on a workspace item (file).
 * This interprets content. The output will be in markdown
 */
public final class InterpretationWorkspaceActionBuildItem extends AbstractWorkspaceActionBuildItem {

    /**
     * Construction
     *
     * @param label the label that will show in the action dropdown
     * @param systemMessage AI System message appendix. There will already be a general system message giving some context.
     * @param userMessage Specific to your use case
     * @param filter Filter applied to include only certain files, i.e. the action is not available on all files. Leaving this
     *        empty will include the action on all files.
     */
    public InterpretationWorkspaceActionBuildItem(String label, Optional<String> systemMessage, String userMessage,
            Optional<Pattern> filter) {
        super(label, systemMessage, userMessage, filter);
    }

}
