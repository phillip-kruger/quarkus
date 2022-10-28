package io.quarkus.vertx.http.deployment.devui;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.qute.Engine;

final public class DevUIQuteEngineBuildItem extends SimpleBuildItem {
    private final Engine engine;

    public DevUIQuteEngineBuildItem(Engine engine) {
        this.engine = engine;
    }

    public Engine getEngine() {
        return engine;
    }
}
