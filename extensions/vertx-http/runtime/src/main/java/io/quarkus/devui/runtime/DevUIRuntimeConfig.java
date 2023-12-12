package io.quarkus.devui.runtime;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(phase = ConfigPhase.BUILD_AND_RUN_TIME_FIXED, name = "dev-ui")
public class DevUIRuntimeConfig {

    /**
     * The Dev UI CORS config
     */
    public DevUICORSConfig cors;

}
