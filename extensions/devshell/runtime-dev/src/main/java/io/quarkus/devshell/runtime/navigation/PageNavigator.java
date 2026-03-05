package io.quarkus.devshell.runtime.navigation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.devshell.runtime.DevShellRouter;
import io.quarkus.devshell.runtime.pages.ShellPage;

/**
 * Manages navigation between pages in the Dev Shell TUI.
 */
public class PageNavigator {

    private final Map<String, ShellPage> pages = new LinkedHashMap<>();
    private final List<String> history = new ArrayList<>();
    private String currentPageId = null;
    private DevShellRouter router;

    /**
     * Set the router used for JSON-RPC calls.
     */
    public void setRouter(DevShellRouter router) {
        this.router = router;
    }

    /**
     * Register a page with the navigator.
     *
     * @param pageId unique identifier for the page
     * @param page the page implementation
     */
    public void registerPage(String pageId, ShellPage page) {
        pages.put(pageId, page);
        if (currentPageId == null) {
            currentPageId = pageId;
        }
    }

    /**
     * Navigate to a page by ID.
     *
     * @param pageId the page identifier
     * @return true if navigation succeeded
     */
    public boolean navigateTo(String pageId) {
        if (!pages.containsKey(pageId)) {
            return false;
        }

        ShellPage currentPage = getCurrentPage();
        if (currentPage != null) {
            currentPage.onLeave();
            history.add(currentPageId);
        }

        currentPageId = pageId;
        ShellPage newPage = pages.get(pageId);
        if (newPage != null && router != null) {
            newPage.onEnter(router);
        }

        return true;
    }

    /**
     * Navigate to a page by shortcut key.
     *
     * @param key the shortcut key (1-9)
     * @return true if navigation succeeded
     */
    public boolean navigateByKey(char key) {
        for (Map.Entry<String, ShellPage> entry : pages.entrySet()) {
            if (entry.getValue().getShortcutKey() == key) {
                return navigateTo(entry.getKey());
            }
        }
        return false;
    }

    /**
     * Go back to the previous page.
     *
     * @return true if there was a page to go back to
     */
    public boolean goBack() {
        if (history.isEmpty()) {
            return false;
        }

        ShellPage currentPage = getCurrentPage();
        if (currentPage != null) {
            currentPage.onLeave();
        }

        currentPageId = history.remove(history.size() - 1);
        ShellPage newPage = pages.get(currentPageId);
        if (newPage != null && router != null) {
            newPage.onEnter(router);
        }

        return true;
    }

    /**
     * Get the currently active page.
     */
    public ShellPage getCurrentPage() {
        return currentPageId != null ? pages.get(currentPageId) : null;
    }

    /**
     * Get the current page ID.
     */
    public String getCurrentPageId() {
        return currentPageId;
    }

    /**
     * Get all registered pages.
     */
    public Map<String, ShellPage> getPages() {
        return pages;
    }

    /**
     * Get the list of pages for navigation bar rendering.
     */
    public List<NavigationItem> getNavigationItems() {
        List<NavigationItem> items = new ArrayList<>();
        for (Map.Entry<String, ShellPage> entry : pages.entrySet()) {
            ShellPage page = entry.getValue();
            boolean isActive = entry.getKey().equals(currentPageId);
            items.add(new NavigationItem(
                    entry.getKey(),
                    page.getTitle(),
                    page.getShortcutKey(),
                    isActive));
        }
        return items;
    }

    /**
     * Notify all pages of a hot reload.
     */
    public void onHotReload() {
        ShellPage currentPage = getCurrentPage();
        if (currentPage != null && router != null) {
            currentPage.onHotReload(router);
        }
    }

    /**
     * Represents a navigation item for the navigation bar.
     */
    public record NavigationItem(String id, String title, char shortcutKey, boolean active) {
    }
}
