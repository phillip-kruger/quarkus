package io.quarkus.devui.runtime.service.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

/**
 * Get all the extensions as discovered during build time
 */
@ApplicationScoped
public class ExtensionsService {

    private final Map<ExtensionGroup, List<Extension>> extensions = new HashMap<>();

    public void initialize(List<Extension> activeExtensions, List<Extension> inactiveExtensions) {
        this.extensions.put(ExtensionGroup.active, activeExtensions);
        this.extensions.put(ExtensionGroup.inactive, inactiveExtensions);
    }

    public Map<ExtensionGroup, List<Extension>> getExtensions() {
        return this.extensions;
    }
}
