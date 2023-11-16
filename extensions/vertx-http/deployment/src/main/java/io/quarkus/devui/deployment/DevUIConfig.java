package io.quarkus.devui.deployment;

import java.util.Optional;

import io.quarkus.runtime.annotations.ConfigGroup;
import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot(name = "dev-ui")
public class DevUIConfig {

    /**
     * The number of history log entries to remember.
     */
    @ConfigItem(defaultValue = "50")
    public int historySize;

    /**
     * CORS configuration.
     */
    public Cors cors = new Cors();

    /**
     * Production configuration
     */
    public Prod prod = new Prod();

    @ConfigGroup
    public static class Cors {

        /**
         * Enable CORS filter.
         */
        @ConfigItem(defaultValue = "true")
        public boolean enabled = true;
    }

    @ConfigGroup
    public static class Prod {

        /**
         * Enable in Production
         */
        @ConfigItem(defaultValue = "false")
        public Optional<Boolean> enabled = Optional.empty();
    }
}
