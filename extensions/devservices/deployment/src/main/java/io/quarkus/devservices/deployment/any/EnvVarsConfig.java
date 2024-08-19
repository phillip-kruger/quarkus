package io.quarkus.devservices.deployment.any;

import java.util.Map;

import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithDefaults;
import io.smallrye.config.WithParentName;

@ConfigGroup
public interface EnvVarsConfig {

    /**
     * env vars
     */
    @ConfigDocMapKey("vars")
    @WithParentName
    @WithDefaults
    Map<String, EnvValueConfig> vars();

}
