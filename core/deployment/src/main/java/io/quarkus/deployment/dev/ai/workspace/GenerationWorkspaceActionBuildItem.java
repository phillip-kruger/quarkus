package io.quarkus.deployment.dev.ai.workspace;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * This is an action on a workspace item (file).
 * This generates new source using existing source as input. The generated source can be save at a new location.
 */
public final class GenerationWorkspaceActionBuildItem extends AbstractWorkspaceActionBuildItem {

    private final Function<Path, Path> storePathFunction;

    /**
     * Construction
     *
     * @param label the label that will show in the action dropdown
     * @param systemMessage AI System message appendix. There will already be a general system message giving some context.
     * @param userMessage Specific to your use case.
     * @param storePathFunction A function that takes the source path as input and returns the path where the newly generated
     *        content should be stored if the user chooses to save it.
     * @param filter Filter applied to include only certain files, i.e. the action is not available on all files. Leaving this
     *        empty will include the action on all files.
     */
    public GenerationWorkspaceActionBuildItem(String label, Optional<String> systemMessage, String userMessage,
            Function<Path, Path> storePathFunction,
            Optional<Pattern> filter) {
        super(label, systemMessage, userMessage, filter);
        this.storePathFunction = storePathFunction;
    }

    public Function<Path, Path> getStorePathFunction() {
        return storePathFunction;
    }

    public Path resolveStorePath(Path sourcePath) {
        return storePathFunction.apply(sourcePath);
    }
}
