package io.quarkus.deployment.dev.ai.workspace;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * This is an action on a workspace item (file).
 * This is used to manipulate existing source in some way using AI. The manipulated source can be used to override
 * the provided source.
 */
public final class ManipulationWorkspaceActionBuildItem extends AbstractWorkspaceActionBuildItem {

    /**
     * Construction
     *
     * @param label the label that will show in the action dropdown
     * @param systemMessage AI System message appendix. There will already be a general system message giving some context.
     * @param userMessage Specific to your use case
     * @param filter Filter applied to include only certain files, i.e. the action is not available on all files. Leaving this
     *        empty will include the action on all files.
     */
    public ManipulationWorkspaceActionBuildItem(String label, Optional<String> systemMessage, String userMessage,
            Optional<Pattern> filter) {
        super(label, systemMessage, userMessage, filter);
    }

}
