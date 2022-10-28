package io.quarkus.devui.deployment.spi.buildtime;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.devui.deployment.spi.AbstractDevUIBuildItem;

/**
 * Contains info on the build time template used to build static content for Dev UI
 * All files are relative to dev-ui-templates/build-time/{extensionName} (in src/main/resources)
 *
 * This contain the fileName to the template, and the template data (variables)
 *
 * This allows extensions developers to add "static files" that they generate with Qute at build time.
 * From a runtime p.o.v this is file served from "disk"
 */
public final class QuteTemplateBuildItem extends AbstractDevUIBuildItem {
    private final Map<String, Map<String, Object>> fileAndData = new HashMap<>();

    public QuteTemplateBuildItem(String extensionName) {
        super(extensionName);
    }

    public Map<String, Map<String, Object>> getFileAndData() {
        return fileAndData;
    }

    public void add(String filename, Map<String, Object> data) {
        fileAndData.put(filename, data);
    }
}
