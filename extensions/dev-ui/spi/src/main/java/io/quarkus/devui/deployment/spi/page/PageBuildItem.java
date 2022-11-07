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

    private final List<Page> pages;
    private final Map<String, Object> buildTimeData;

    public PageBuildItem(String extensionName) {
        super(extensionName);
        this.pages = new ArrayList<>();
        this.buildTimeData = new HashMap<>();
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

    public void addBuildTimeData(String fieldName, Object fieldData) {
        this.buildTimeData.put(fieldName, fieldData);
    }

    public List<Page> getPages() {
        return this.pages;
    }

    public Map<String, Object> getBuildTimeData() {
        return this.buildTimeData;
    }

    public boolean hasBuildTimeData() {
        return this.buildTimeData != null && !this.buildTimeData.isEmpty();
    }
}
