package io.quarkus.deployment.dev.ai;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * If available, a handle on the AI Client
 *
 * This is intended for use in dev mode to allow Quarkus to help the developer.
 */
public final class AIBuildItem extends SimpleBuildItem {

    public AIBuildItem() {

    }

    public String sayHello(String name) {
        return "Hello " + name;
    }
}
