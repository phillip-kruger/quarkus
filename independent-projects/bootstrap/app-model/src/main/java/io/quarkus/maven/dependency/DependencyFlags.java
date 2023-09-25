package io.quarkus.maven.dependency;

public interface DependencyFlags {

    /* @formatter:off */
    public static final int OPTIONAL =                             0b0000000000001;
    public static final int DIRECT =                               0b0000000000010;
    public static final int RUNTIME_CP =                           0b0000000000100;
    public static final int DEPLOYMENT_CP =                        0b0000000001000;
    public static final int RUNTIME_EXTENSION_ARTIFACT =           0b0000000010000;
    public static final int WORKSPACE_MODULE =                     0b0000000100000;
    public static final int RELOADABLE =                           0b0000001000000;
    // A top-level runtime extension artifact is either a direct
    // dependency or a first extension dependency on the branch
    // navigating from the root to leaves
    public static final int TOP_LEVEL_RUNTIME_EXTENSION_ARTIFACT = 0b0000010000000;
    public static final int CLASSLOADER_PARENT_FIRST             = 0b0000100000000;
    public static final int CLASSLOADER_RUNNER_PARENT_FIRST      = 0b0001000000000;
    public static final int CLASSLOADER_LESSER_PRIORITY          = 0b0010000000000;
    // General purpose flag that could be re-used for various
    // kinds of processing indicating that a dependency has been
    // visited. This flag is meant to be cleared for all the nodes
    // once the processing of the whole tree has completed.
    public static final int VISITED                              = 0b0100000000000;

    /**
     * Compile-only dependencies are those that are configured in the project
     * to be included only in the compile phase ({@code provided} dependency scope in Maven,
     * {@code compileOnly} configuration in Gradle).
     * <p>
     * These dependencies will not be present on the Quarkus application runtime or
     * augmentation (deployment) classpath when the application is bootstrapped in production mode
     * (io.quarkus.runtime.LaunchMode.NORMAL).
     * <p>
     * Compile-only dependencies will be present on both the runtime and the augmentation classpath
     * of a Quarkus application launched in test and dev modes.
     * <p>
     * In any case though, these dependencies will be available during augmentation for processing
     * using {@link io.quarkus.bootstrap.model.ApplicationModel#getDependencies(int)} by passing
     * this flag as an argument.
     */
    public static final int COMPILE_ONLY                         = 0b1000000000000;

    /* @formatter:on */

}
