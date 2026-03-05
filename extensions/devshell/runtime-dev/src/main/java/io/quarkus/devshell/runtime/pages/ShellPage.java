package io.quarkus.devshell.runtime.pages;

import io.quarkus.devshell.runtime.DevShellRouter;

/**
 * Interface for Dev Shell TUI pages.
 * Each page represents a distinct view in the terminal UI,
 * similar to web pages in the browser-based Dev UI.
 */
public interface ShellPage {

    /**
     * Get the title displayed in the navigation bar.
     */
    String getTitle();

    /**
     * Get the keyboard shortcut key (1-9) for this page.
     */
    char getShortcutKey();

    /**
     * Called when the page becomes active.
     * Use this to fetch initial data or start subscriptions.
     *
     * @param router the router for making JSON-RPC calls
     */
    void onEnter(DevShellRouter router);

    /**
     * Called when the page becomes inactive.
     * Use this to cancel subscriptions and clean up resources.
     */
    void onLeave();

    /**
     * Print the page content to stdout.
     * This is a simple text-based output for the initial implementation.
     */
    void print();

    /**
     * Handle keyboard/command input specific to this page.
     *
     * @param command the command entered by the user
     * @return true if the command was handled, false to pass to global handler
     */
    boolean handleCommand(String command);

    /**
     * Called periodically to update page state.
     * Useful for refreshing data or handling async updates.
     */
    default void tick() {
    }

    /**
     * Called when the application is hot-reloaded.
     * Pages should refresh their data.
     *
     * @param router the router for making JSON-RPC calls
     */
    default void onHotReload(DevShellRouter router) {
        onEnter(router);
    }
}
