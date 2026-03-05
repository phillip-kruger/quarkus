package io.quarkus.devshell.runtime.tui;

/**
 * ANSI escape code utilities for terminal rendering.
 */
public final class AnsiRenderer {

    private AnsiRenderer() {
    }

    // Reset
    public static final String RESET = "\u001b[0m";

    // Text styles
    public static final String BOLD = "\u001b[1m";
    public static final String DIM = "\u001b[2m";
    public static final String ITALIC = "\u001b[3m";
    public static final String UNDERLINE = "\u001b[4m";
    public static final String BLINK = "\u001b[5m";
    public static final String REVERSE = "\u001b[7m";

    // Foreground colors
    public static final String FG_BLACK = "\u001b[30m";
    public static final String FG_RED = "\u001b[31m";
    public static final String FG_GREEN = "\u001b[32m";
    public static final String FG_YELLOW = "\u001b[33m";
    public static final String FG_BLUE = "\u001b[34m";
    public static final String FG_MAGENTA = "\u001b[35m";
    public static final String FG_CYAN = "\u001b[36m";
    public static final String FG_WHITE = "\u001b[37m";

    // Bright foreground colors
    public static final String FG_BRIGHT_BLACK = "\u001b[90m";
    public static final String FG_BRIGHT_RED = "\u001b[91m";
    public static final String FG_BRIGHT_GREEN = "\u001b[92m";
    public static final String FG_BRIGHT_YELLOW = "\u001b[93m";
    public static final String FG_BRIGHT_BLUE = "\u001b[94m";
    public static final String FG_BRIGHT_MAGENTA = "\u001b[95m";
    public static final String FG_BRIGHT_CYAN = "\u001b[96m";
    public static final String FG_BRIGHT_WHITE = "\u001b[97m";

    // Background colors
    public static final String BG_BLACK = "\u001b[40m";
    public static final String BG_RED = "\u001b[41m";
    public static final String BG_GREEN = "\u001b[42m";
    public static final String BG_YELLOW = "\u001b[43m";
    public static final String BG_BLUE = "\u001b[44m";
    public static final String BG_MAGENTA = "\u001b[45m";
    public static final String BG_CYAN = "\u001b[46m";
    public static final String BG_WHITE = "\u001b[47m";

    // Screen buffer
    public static final String ENTER_ALTERNATE_SCREEN = "\u001b[?1049h";
    public static final String EXIT_ALTERNATE_SCREEN = "\u001b[?1049l";

    // Cursor visibility
    public static final String HIDE_CURSOR = "\u001b[?25l";
    public static final String SHOW_CURSOR = "\u001b[?25h";

    // Box drawing characters
    public static final String BOX_HORIZONTAL = "─";
    public static final String BOX_VERTICAL = "│";
    public static final String BOX_TOP_LEFT = "┌";
    public static final String BOX_TOP_RIGHT = "┐";
    public static final String BOX_BOTTOM_LEFT = "└";
    public static final String BOX_BOTTOM_RIGHT = "┘";
    public static final String BOX_VERTICAL_RIGHT = "├";
    public static final String BOX_VERTICAL_LEFT = "┤";
    public static final String BOX_HORIZONTAL_DOWN = "┬";
    public static final String BOX_HORIZONTAL_UP = "┴";
    public static final String BOX_CROSS = "┼";

    // Double box drawing
    public static final String BOX_DOUBLE_HORIZONTAL = "═";
    public static final String BOX_DOUBLE_VERTICAL = "║";

    // Selection indicator
    public static final String ARROW_RIGHT = "▶";
    public static final String BULLET = "•";

    /**
     * Move cursor to specified position (1-based).
     */
    public static String moveTo(int row, int col) {
        return "\u001b[" + row + ";" + col + "H";
    }

    /**
     * Clear the entire screen.
     */
    public static String clearScreen() {
        return "\u001b[2J";
    }

    /**
     * Clear from cursor to end of screen.
     */
    public static String clearToEnd() {
        return "\u001b[J";
    }

    /**
     * Clear the current line.
     */
    public static String clearLine() {
        return "\u001b[2K";
    }

    /**
     * Move cursor up N lines.
     */
    public static String moveUp(int n) {
        return "\u001b[" + n + "A";
    }

    /**
     * Move cursor down N lines.
     */
    public static String moveDown(int n) {
        return "\u001b[" + n + "B";
    }

    /**
     * Pad or truncate string to exact width.
     */
    public static String fixedWidth(String text, int width) {
        if (text == null) {
            text = "";
        }
        if (text.length() > width) {
            return text.substring(0, width - 1) + "…";
        }
        return String.format("%-" + width + "s", text);
    }

    /**
     * Create a horizontal line of specified width.
     */
    public static String horizontalLine(int width) {
        return BOX_HORIZONTAL.repeat(width);
    }

    /**
     * Render text with style and reset.
     */
    public static String styled(String style, String text) {
        return style + text + RESET;
    }

    /**
     * Render selected item (reverse video).
     */
    public static String selected(String text) {
        return REVERSE + text + RESET;
    }

    /**
     * Render header text (bold + color).
     */
    public static String header(String text) {
        return BOLD + FG_CYAN + text + RESET;
    }

    /**
     * Render label text (dim).
     */
    public static String label(String text) {
        return FG_BRIGHT_BLACK + text + RESET;
    }

    /**
     * Render value text.
     */
    public static String value(String text) {
        return FG_WHITE + text + RESET;
    }

    /**
     * Render success text (green).
     */
    public static String success(String text) {
        return FG_GREEN + text + RESET;
    }

    /**
     * Render error text (red).
     */
    public static String error(String text) {
        return FG_RED + text + RESET;
    }

    /**
     * Render warning text (yellow).
     */
    public static String warning(String text) {
        return FG_YELLOW + text + RESET;
    }
}
