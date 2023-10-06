package io.quarkus.amazon.lambda.http.deployment;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigRoot;

@ConfigRoot
public class LambdaHttpBuildTimeConfig {
    /**
     * Enable security mechanisms to process lambda and AWS based security (i.e. Cognito, IAM) from
     * the http event sent from API Gateway
     */
    @ConfigItem(defaultValue = "false")
    public boolean enableSecurity;

    /**
     * If true, HTTP will run in the virtual server in Dev Mode.
     * Dev UI do not work in this mode.
     *
     * True by default
     */
    @ConfigItem(defaultValue = "true")
    public boolean virtualInDev;
}
