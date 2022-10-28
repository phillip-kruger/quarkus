package io.quarkus.devui.deployment;

import java.util.List;

import io.quarkus.builder.item.SimpleBuildItem;
import io.quarkus.devui.runtime.service.extension.Extension;

public final class ExtensionsBuildItem extends SimpleBuildItem {

    private final List<Extension> activeExtensions;
    private final List<Extension> inactiveExtensions;

    public ExtensionsBuildItem(List<Extension> activeExtensions, List<Extension> inactiveExtensions) {
        this.activeExtensions = activeExtensions;
        this.inactiveExtensions = inactiveExtensions;
    }

    public List<Extension> getActiveExtensions() {
        return activeExtensions;
    }

    public List<Extension> getInactiveExtensions() {
        return inactiveExtensions;
    }

}
