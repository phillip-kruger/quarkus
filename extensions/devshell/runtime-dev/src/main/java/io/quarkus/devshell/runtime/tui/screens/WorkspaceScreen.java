package io.quarkus.devshell.runtime.tui.screens;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.Screen;
import io.quarkus.devshell.runtime.tui.widgets.TreeView;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Screen for viewing and navigating the project workspace files.
 */
public class WorkspaceScreen implements Screen {

    private boolean loading = true;
    private String error = null;

    private final List<WorkspaceItem> items = new ArrayList<>();
    private final TreeView<WorkspaceItem> treeView;

    // File content preview
    private String selectedFilePath = null;
    private String selectedFileContent = null;
    private boolean loadingContent = false;
    private int contentScrollOffset = 0;

    // Edit mode
    private boolean editMode = false;
    private boolean modified = false;
    private boolean saving = false;
    private List<String> editLines = new ArrayList<>();
    private int cursorLine = 0;
    private int cursorCol = 0;
    private boolean isBinaryFile = false;

    public WorkspaceScreen() {
        this.treeView = new TreeView<>(
                item -> item.name,
                item -> item.name);
    }

    @Override
    public String getTitle() {
        return "Workspace";
    }

    @Override
    public void onEnter(AppContext ctx) {
        loadWorkspaceItems(ctx);
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    private void loadWorkspaceItems(AppContext ctx) {
        loading = true;
        error = null;
        items.clear();
        ctx.requestRedraw();

        try {
            Object workspaceItems = DevConsoleManager.invoke("devui-workspace_getWorkspaceItems");
            loading = false;

            if (workspaceItems != null) {
                JsonObject json = JsonObject.mapFrom(workspaceItems);
                JsonArray itemsArray = json.getJsonArray("items");
                if (itemsArray != null) {
                    for (int i = 0; i < itemsArray.size(); i++) {
                        JsonObject item = itemsArray.getJsonObject(i);
                        String name = item.getString("name");
                        // Jackson serializes Path as a URI string already
                        String pathStr = item.getString("path");
                        if (name != null && !name.isEmpty()) {
                            items.add(new WorkspaceItem(name, pathStr != null ? pathStr : name));
                        }
                    }
                }
            }

            treeView.setItems(items);
            treeView.expandAll();
            ctx.requestRedraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load workspace: " + ex.getMessage();
            ctx.requestRedraw();
        }
    }

    private void loadFileContent(AppContext ctx, String path) {
        if (path == null) {
            return;
        }

        loadingContent = true;
        selectedFilePath = path;
        selectedFileContent = null;
        contentScrollOffset = 0;
        ctx.requestRedraw();

        try {
            Object contentObj = DevConsoleManager.invoke("devui-workspace_getWorkspaceItemContent",
                    Map.of("path", path));
            loadingContent = false;

            if (contentObj != null) {
                JsonObject json = JsonObject.mapFrom(contentObj);
                String content = json.getString("content");
                String type = json.getString("type");
                boolean isBinary = json.getBoolean("isBinary", false);

                isBinaryFile = isBinary;
                if (isBinary) {
                    selectedFileContent = "[Binary file - " + type + "]";
                } else if (content != null) {
                    selectedFileContent = content;
                } else {
                    selectedFileContent = "(empty)";
                }
            } else {
                selectedFileContent = "(empty)";
                isBinaryFile = false;
            }

            ctx.requestRedraw();
        } catch (Exception ex) {
            loadingContent = false;
            selectedFileContent = "Error loading file: " + ex.getMessage();
            ctx.requestRedraw();
        }
    }

    private void enterEditMode() {
        if (selectedFileContent == null || isBinaryFile) {
            return;
        }
        editMode = true;
        modified = false;
        editLines.clear();
        String[] lines = selectedFileContent.split("\n", -1);
        for (String line : lines) {
            editLines.add(line);
        }
        if (editLines.isEmpty()) {
            editLines.add("");
        }
        cursorLine = 0;
        cursorCol = 0;
        contentScrollOffset = 0;
    }

    private void exitEditMode(boolean save, AppContext ctx) {
        if (save && modified) {
            saveContent(ctx);
        } else {
            editMode = false;
            modified = false;
        }
    }

    private void saveContent(AppContext ctx) {
        if (selectedFilePath == null) {
            return;
        }

        saving = true;
        ctx.requestRedraw();

        // Join lines back to content
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < editLines.size(); i++) {
            content.append(editLines.get(i));
            if (i < editLines.size() - 1) {
                content.append("\n");
            }
        }

        String contentStr = content.toString();

        try {
            DevConsoleManager.invoke("devui-workspace_saveWorkspaceItemContent",
                    Map.of("path", selectedFilePath, "content", contentStr));
            saving = false;
            // Update the preview content
            selectedFileContent = contentStr;
            editMode = false;
            modified = false;
            ctx.setStatus("File saved successfully");
            ctx.requestRedraw();
        } catch (Exception ex) {
            saving = false;
            ctx.setStatus("Failed to save: " + ex.getMessage());
            ctx.requestRedraw();
        }
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        int width = ctx.getWidth();
        int height = ctx.getHeight();

        // Header
        renderHeader(buffer, width);

        if (loading) {
            buffer.setString(width / 2 - 5, height / 2, "Loading...", Style.create().yellow());
            return;
        }

        if (error != null) {
            buffer.setString(1, 4, error, Style.create().red());
            renderFooter(buffer, width, height);
            return;
        }

        if (items.isEmpty()) {
            buffer.setString(1, 4, "No workspace items found.", Style.create().gray());
            renderFooter(buffer, width, height);
            return;
        }

        // Two-panel layout
        int treeWidth = Math.min(50, width / 2);
        int contentWidth = width - treeWidth - 3;

        // Left panel: Tree view
        renderTreePanel(buffer, treeWidth, height);

        // Divider
        renderDivider(buffer, treeWidth, height);

        // Right panel: File preview
        renderContentPanel(buffer, treeWidth + 2, contentWidth, height);

        // Footer
        renderFooter(buffer, width, height);
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Workspace ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);

        // File count
        buffer.setString(1, 2, items.size() + " items", Style.create().gray());
    }

    private void renderTreePanel(Buffer buffer, int treeWidth, int height) {
        buffer.setString(1, 3, " Files", Style.create().cyan().bold());

        treeView.setVisibleRows(height - 7);
        treeView.setWidth(treeWidth - 2);
        treeView.render(buffer, 5, 1);
    }

    private void renderDivider(Buffer buffer, int treeWidth, int height) {
        int col = treeWidth;
        for (int row = 4; row < height - 1; row++) {
            buffer.setString(col, row, "\u2502", Style.create().gray());
        }
    }

    private void renderContentPanel(Buffer buffer, int startCol, int contentWidth, int height) {

        TreeView.TreeNode<WorkspaceItem> selected = treeView.getSelectedNode();

        if (selected == null) {
            buffer.setString(startCol, 5, "Select a file to preview", Style.create().gray());
            return;
        }

        if (selected.isDirectory) {
            buffer.setString(startCol, 5, "Select a file to preview", Style.create().gray());
            return;
        }

        // Show file name with edit indicator
        if (editMode) {
            if (modified) {
                buffer.setString(startCol, 4, "\u270E " + selected.data.name + " [modified]", Style.create().yellow().bold());
            } else {
                buffer.setString(startCol, 4, "\u270E " + selected.data.name + " [editing]", Style.create().cyan().bold());
            }
        } else {
            buffer.setString(startCol, 4, selected.data.name, Style.create().cyan().bold());
        }

        if (saving) {
            buffer.setString(startCol, 6, "Saving...", Style.create().yellow());
            return;
        }

        if (loadingContent) {
            buffer.setString(startCol, 6, "Loading...", Style.create().yellow());
            return;
        }

        if (selectedFileContent == null) {
            buffer.setString(startCol, 6, "Press Enter to load file content", Style.create().gray());
            return;
        }

        // Render file content (edit mode or preview mode)
        if (editMode) {
            renderEditMode(buffer, startCol, contentWidth, height);
        } else {
            renderPreviewMode(buffer, startCol, contentWidth, height);
        }
    }

    private void renderPreviewMode(Buffer buffer, int startCol, int contentWidth, int height) {
        String[] lines = selectedFileContent.split("\n", -1);
        int contentHeight = height - 8;
        int endLine = Math.min(contentScrollOffset + contentHeight, lines.length);

        for (int i = contentScrollOffset; i < endLine; i++) {
            int row = 6 + (i - contentScrollOffset);

            // Line number
            String lineNum = String.format("%3d ", i + 1);
            buffer.setString(startCol, row, lineNum, Style.create().gray());

            String line = lines[i];
            // Truncate long lines
            if (line.length() > contentWidth - 6) {
                line = line.substring(0, contentWidth - 9) + "...";
            }
            buffer.setString(startCol + 4, row, line, Style.EMPTY);
        }

        // Scroll indicator
        if (lines.length > contentHeight) {
            int scrollPercent = (int) ((contentScrollOffset + contentHeight) * 100.0 / lines.length);
            scrollPercent = Math.min(scrollPercent, 100);
            buffer.setString(startCol + contentWidth - 6, height - 3, scrollPercent + "%", Style.create().gray());
        }
    }

    private void renderEditMode(Buffer buffer, int startCol, int contentWidth, int height) {
        int contentHeight = height - 8;

        // Ensure cursor is visible
        if (cursorLine < contentScrollOffset) {
            contentScrollOffset = cursorLine;
        } else if (cursorLine >= contentScrollOffset + contentHeight) {
            contentScrollOffset = cursorLine - contentHeight + 1;
        }

        int endLine = Math.min(contentScrollOffset + contentHeight, editLines.size());

        for (int i = contentScrollOffset; i < endLine; i++) {
            int row = 6 + (i - contentScrollOffset);

            // Line number
            String lineNum = String.format("%3d ", i + 1);
            buffer.setString(startCol, row, lineNum,
                    i == cursorLine ? Style.create().yellow() : Style.create().gray());

            String line = editLines.get(i);
            int lineStartCol = startCol + 4;
            int availableWidth = contentWidth - 6;

            // Render the line with cursor
            if (i == cursorLine) {
                // Current line with cursor
                int displayStart = 0;
                if (cursorCol > availableWidth - 5) {
                    displayStart = cursorCol - availableWidth + 5;
                }

                String displayLine = line.length() > displayStart
                        ? line.substring(displayStart, Math.min(line.length(), displayStart + availableWidth))
                        : "";

                int cursorPosInDisplay = cursorCol - displayStart;

                // Write text before cursor
                if (cursorPosInDisplay > 0 && cursorPosInDisplay <= displayLine.length()) {
                    buffer.setString(lineStartCol, row, displayLine.substring(0, cursorPosInDisplay), Style.EMPTY);
                } else if (cursorPosInDisplay <= 0) {
                    // Cursor is before display start - show full line
                    buffer.setString(lineStartCol, row, displayLine, Style.EMPTY);
                } else {
                    buffer.setString(lineStartCol, row, displayLine, Style.EMPTY);
                }

                // Write cursor
                if (cursorPosInDisplay >= 0 && cursorPosInDisplay < displayLine.length()) {
                    buffer.setString(lineStartCol + cursorPosInDisplay, row,
                            String.valueOf(displayLine.charAt(cursorPosInDisplay)), Style.create().reversed());
                } else {
                    buffer.setString(lineStartCol + Math.max(0, cursorPosInDisplay), row, " ",
                            Style.create().reversed());
                }

                // Write text after cursor
                if (cursorPosInDisplay >= 0 && cursorPosInDisplay + 1 < displayLine.length()) {
                    buffer.setString(lineStartCol + cursorPosInDisplay + 1, row,
                            displayLine.substring(cursorPosInDisplay + 1), Style.EMPTY);
                }
            } else {
                // Non-current line
                if (line.length() > availableWidth) {
                    buffer.setString(lineStartCol, row, line.substring(0, availableWidth - 3) + "...", Style.EMPTY);
                } else {
                    buffer.setString(lineStartCol, row, line, Style.EMPTY);
                }
            }
        }

        // Position info
        String posInfo = "Ln " + (cursorLine + 1) + ", Col " + (cursorCol + 1);
        buffer.setString(startCol + contentWidth - posInfo.length() - 1, height - 3, posInfo, Style.create().gray());

    }

    private void renderFooter(Buffer buffer, int width, int height) {
        if (editMode) {
            buffer.setString(1, height - 2, "[Ctrl+S] Save  [Esc] Cancel  [Arrows] Navigate  [Enter] New line",
                    Style.create().gray());
        } else if (selectedFileContent != null && !isBinaryFile) {
            buffer.setString(1, height - 2, "[Enter] Preview  [W] Edit  [E] Expand  [C] Collapse  [R] Refresh  [Esc] Back",
                    Style.create().gray());
        } else {
            buffer.setString(1, height - 2, "[Enter] Preview  [E] Expand all  [C] Collapse all  [R] Refresh  [Esc] Back",
                    Style.create().gray());
        }
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading || saving) {
            return true;
        }

        // Edit mode handling
        if (editMode) {
            return handleEditModeKey(key, ctx);
        }

        // Tree navigation
        if (treeView.handleKey(key)) {
            // Clear content when selection changes
            TreeView.TreeNode<WorkspaceItem> selected = treeView.getSelectedNode();
            if (selected != null && !selected.isDirectory) {
                if (selected.data != null && !selected.data.path.equals(selectedFilePath)) {
                    selectedFileContent = null;
                    contentScrollOffset = 0;
                }
            }
            ctx.requestRedraw();
            return true;
        }

        switch (key) {
            case KeyCode.ESCAPE:
                ctx.goBack();
                return true;

            case KeyCode.ENTER:
                TreeView.TreeNode<WorkspaceItem> selected = treeView.getSelectedNode();
                if (selected != null && !selected.isDirectory && selected.data != null) {
                    loadFileContent(ctx, selected.data.path);
                }
                return true;

            case 'w':
            case 'W':
                // Enter edit mode
                if (selectedFileContent != null && !isBinaryFile) {
                    enterEditMode();
                    ctx.requestRedraw();
                }
                return true;

            case 'e':
                // Only expand if no file loaded (otherwise 'e' might be for edit)
                if (selectedFileContent == null) {
                    treeView.expandAll();
                    ctx.requestRedraw();
                }
                return true;

            case 'E':
                treeView.expandAll();
                ctx.requestRedraw();
                return true;

            case 'c':
            case 'C':
                treeView.collapseAll();
                ctx.requestRedraw();
                return true;

            case 'r':
            case 'R':
                loadWorkspaceItems(ctx);
                return true;

            // Content scrolling with [ / ]
            case '[':
                if (selectedFileContent != null && contentScrollOffset > 0) {
                    contentScrollOffset--;
                    ctx.requestRedraw();
                }
                return true;

            case ']':
                if (selectedFileContent != null) {
                    contentScrollOffset++;
                    ctx.requestRedraw();
                }
                return true;

            default:
                return false;
        }
    }

    private boolean handleEditModeKey(int key, AppContext ctx) {
        switch (key) {
            case KeyCode.ESCAPE:
                // Cancel edit
                exitEditMode(false, ctx);
                ctx.requestRedraw();
                return true;

            case 19: // Ctrl+S
                // Save
                exitEditMode(true, ctx);
                return true;

            case KeyCode.UP:
                if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = Math.min(cursorCol, editLines.get(cursorLine).length());
                    ctx.requestRedraw();
                }
                return true;

            case KeyCode.DOWN:
                if (cursorLine < editLines.size() - 1) {
                    cursorLine++;
                    cursorCol = Math.min(cursorCol, editLines.get(cursorLine).length());
                    ctx.requestRedraw();
                }
                return true;

            case KeyCode.LEFT:
                if (cursorCol > 0) {
                    cursorCol--;
                } else if (cursorLine > 0) {
                    cursorLine--;
                    cursorCol = editLines.get(cursorLine).length();
                }
                ctx.requestRedraw();
                return true;

            case KeyCode.RIGHT:
                if (cursorCol < editLines.get(cursorLine).length()) {
                    cursorCol++;
                } else if (cursorLine < editLines.size() - 1) {
                    cursorLine++;
                    cursorCol = 0;
                }
                ctx.requestRedraw();
                return true;

            case KeyCode.HOME:
                cursorCol = 0;
                ctx.requestRedraw();
                return true;

            case KeyCode.END:
                cursorCol = editLines.get(cursorLine).length();
                ctx.requestRedraw();
                return true;

            case KeyCode.ENTER:
                // Insert new line
                String currentLine = editLines.get(cursorLine);
                String beforeCursor = currentLine.substring(0, cursorCol);
                String afterCursor = currentLine.substring(cursorCol);
                editLines.set(cursorLine, beforeCursor);
                editLines.add(cursorLine + 1, afterCursor);
                cursorLine++;
                cursorCol = 0;
                modified = true;
                ctx.requestRedraw();
                return true;

            case KeyCode.BACKSPACE: // 127
                if (cursorCol > 0) {
                    String line = editLines.get(cursorLine);
                    editLines.set(cursorLine, line.substring(0, cursorCol - 1) + line.substring(cursorCol));
                    cursorCol--;
                    modified = true;
                } else if (cursorLine > 0) {
                    // Merge with previous line
                    String prevLine = editLines.get(cursorLine - 1);
                    String currLine = editLines.get(cursorLine);
                    editLines.set(cursorLine - 1, prevLine + currLine);
                    editLines.remove(cursorLine);
                    cursorLine--;
                    cursorCol = prevLine.length();
                    modified = true;
                }
                ctx.requestRedraw();
                return true;

            case KeyCode.DELETE:
                String line = editLines.get(cursorLine);
                if (cursorCol < line.length()) {
                    editLines.set(cursorLine, line.substring(0, cursorCol) + line.substring(cursorCol + 1));
                    modified = true;
                } else if (cursorLine < editLines.size() - 1) {
                    // Merge with next line
                    String nextLine = editLines.get(cursorLine + 1);
                    editLines.set(cursorLine, line + nextLine);
                    editLines.remove(cursorLine + 1);
                    modified = true;
                }
                ctx.requestRedraw();
                return true;

            case KeyCode.TAB:
                // Insert spaces for tab
                insertChar(ctx, ' ');
                insertChar(ctx, ' ');
                insertChar(ctx, ' ');
                insertChar(ctx, ' ');
                return true;

            default:
                // Printable character
                if (key >= 32 && key < 127) {
                    insertChar(ctx, (char) key);
                    return true;
                }
                return false;
        }
    }

    private void insertChar(AppContext ctx, char c) {
        String line = editLines.get(cursorLine);
        editLines.set(cursorLine, line.substring(0, cursorCol) + c + line.substring(cursorCol));
        cursorCol++;
        modified = true;
        ctx.requestRedraw();
    }

    @Override
    public void onResize(int width, int height) {
        int treeWidth = Math.min(50, width / 2);
        treeView.setWidth(treeWidth - 2);
        treeView.setVisibleRows(height - 7);
    }

    /**
     * Data class for workspace items.
     */
    private static class WorkspaceItem {
        final String name;
        final String path;

        WorkspaceItem(String name, String path) {
            this.name = name;
            this.path = path;
        }
    }
}
