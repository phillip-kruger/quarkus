package io.quarkus.produi.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Runtime configuration for Prod UI.
 */
@ConfigMapping(prefix = "quarkus.prod-ui")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface ProdUIConfig {

    /**
     * Whether Prod UI should be enabled on the management interface.
     */
    @WithDefault("true")
    boolean managementEnabled();
}
