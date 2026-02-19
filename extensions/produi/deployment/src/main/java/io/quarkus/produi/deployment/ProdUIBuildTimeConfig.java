package io.quarkus.produi.deployment;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Build-time configuration for Prod UI.
 */
@ConfigMapping(prefix = "quarkus.prod-ui")
@ConfigRoot(phase = ConfigPhase.BUILD_TIME)
public interface ProdUIBuildTimeConfig {

    /**
     * Whether Prod UI should be enabled.
     */
    @WithDefault("true")
    boolean enabled();

    /**
     * The root path of the Prod UI.
     */
    @WithDefault("prod-ui")
    String rootPath();
}
