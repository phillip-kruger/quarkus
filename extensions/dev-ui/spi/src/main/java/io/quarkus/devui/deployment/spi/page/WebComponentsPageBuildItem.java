package io.quarkus.devui.deployment.spi.page;

import java.util.List;

import io.quarkus.devui.deployment.spi.*;

/**
 * Add a "page" to the Dev UI. This is typically the middle part of the screen.
 * You can link to this page from :
 * - your extension card (on by default)
 * - the side menu (off by default)
 *
 * There will also be a navigation bar that will have links to all your extension's pages.
 * You can choose to hide the page from there. (on by default)
 *
 * This page is specifically when you create the UI using Web Components.
 */
public final class WebComponentsPageBuildItem extends AbstractDevUIBuildItem {

    private final List<WebComponentPage> pages;

    public WebComponentsPageBuildItem(String extensionName, List<WebComponentPage> pages) {
        super(extensionName);
        this.pages = pages;
    }

    public List<WebComponentPage> getWebComponentPages() {
        return pages;
    }

    //    public boolean hasBuildTimeData() {
    //        if (this.pages != null && !this.pages.isEmpty()) {
    //            for (WebComponentPage page : this.pages) {
    //                if (page.hasBuildTimeData()) {
    //                    return true;
    //                }
    //            }
    //        }
    //        return false;
    //    }
}
