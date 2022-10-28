package io.quarkus.devui.deployment.spi.page;

import java.util.List;

import io.quarkus.devui.deployment.spi.*;

/**
 * Add a "page" to the Dev UI. This is typically the middle part of the screen.
 * You can link to this page from :
 * - your extension card (on by default)
 * - the side menu (off by default)
 *
 * This page is specifically when you create the UI externally. You have the option to render this inside Dev UI, or link out in
 * a new tab
 */
public final class ExternalPageBuildItem extends AbstractDevUIBuildItem {

    private final List<ExternalPage> pages;

    public ExternalPageBuildItem(String extensionName, List<ExternalPage> pages) {
        super(extensionName);
        this.pages = pages;
    }

    public List<ExternalPage> getExternalPages() {
        return pages;
    }
}
