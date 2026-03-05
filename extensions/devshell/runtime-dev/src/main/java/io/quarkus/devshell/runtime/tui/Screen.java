package io.quarkus.devshell.runtime.tui;

import dev.tamboui.terminal.Frame;

/**
 * Interface for TUI screens.
 * Each screen represents a distinct view in the terminal UI.
 * Rendering uses TamboUI's Frame/Buffer system.
 */
public interface Screen {

    /**
     * Get the title of this screen.
     */
    String getTitle();

    /**
     * Called when this screen becomes active.
     *
     * @param ctx the app context providing access to router, navigation, etc.
     */
    void onEnter(AppContext ctx);

    /**
     * Called when this screen is no longer active.
     */
    void onLeave();

    /**
     * Render this screen to the frame's buffer.
     *
     * @param frame the TamboUI frame with buffer for rendering
     * @param ctx the app context
     */
    void render(Frame frame, AppContext ctx);

    /**
     * Handle a key press.
     *
     * @param key the parsed key code (see KeyCode class)
     * @param ctx the app context
     * @return true if the key was handled, false to pass to parent handler
     */
    boolean handleKey(int key, AppContext ctx);

    /**
     * Called when the terminal is resized.
     *
     * @param width new terminal width
     * @param height new terminal height
     */
    default void onResize(int width, int height) {
    }

    /**
     * Called periodically for screens that need updates.
     *
     * @param ctx the app context
     */
    default void tick(AppContext ctx) {
    }
}
