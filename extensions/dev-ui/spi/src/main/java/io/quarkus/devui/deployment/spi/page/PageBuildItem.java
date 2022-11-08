package io.quarkus.devui.deployment.spi.page;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.devui.deployment.spi.*;

/**
 * Add a page (or section) to the Dev UI. This is typically the middle part of the screen.
 * This will also add links to this pages
 */
public final class PageBuildItem extends AbstractDevUIBuildItem {

    private final List<PageBuilder> pageBuilders;
    private final Map<String, Object> buildTimeData;

    public PageBuildItem(String extensionName) {
        super(extensionName);
        this.pageBuilders = new ArrayList<>();
        this.buildTimeData = new HashMap<>();
    }

    public void addPage(PageBuilder page) {
        this.pageBuilders.add(page);
    }

    public void addBuildTimeData(String fieldName, Object fieldData) {
        this.buildTimeData.put(fieldName, fieldData);
    }

    public List<PageBuilder> getPages() {
        return this.pageBuilders;
    }

    public Map<String, Object> getBuildTimeData() {
        return this.buildTimeData;
    }

    public boolean hasBuildTimeData() {
        return this.buildTimeData != null && !this.buildTimeData.isEmpty();
    }
}
