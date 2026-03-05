package io.quarkus.devshell.runtime.tui.screens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.Screen;
import io.quarkus.devshell.runtime.tui.widgets.ListView;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Screen for viewing application dependencies.
 */
public class DependenciesScreen implements Screen {

    private boolean loading = true;
    private String error = null;

    // Dependency data
    private String rootId = null;
    private final List<DependencyNode> allNodes = new ArrayList<>();
    private final List<DependencyLink> allLinks = new ArrayList<>();

    // Filtered views
    private final List<DependencyNode> runtimeDeps = new ArrayList<>();
    private final List<DependencyNode> deploymentDeps = new ArrayList<>();
    private final List<DependencyNode> directDeps = new ArrayList<>();

    // Current tab: 0 = All, 1 = Direct, 2 = Runtime, 3 = Deployment
    private int currentTab = 0;
    private final ListView<DependencyNode> depList;

    // Search/filter
    private boolean searchMode = false;
    private StringBuilder searchQuery = new StringBuilder();
    private final List<DependencyNode> filteredNodes = new ArrayList<>();

    public DependenciesScreen() {
        this.depList = new ListView<>(node -> {
            String icon = getTypeIcon(node);
            return icon + " " + node.name + " (" + node.version + ")";
        });
    }

    private String getTypeIcon(DependencyNode node) {
        if (node.isRoot) {
            return "\u25C6"; // ◆
        } else if (node.isRuntime) {
            return "\u25CF"; // ●
        } else {
            return "\u25CB"; // ○
        }
    }

    @Override
    public String getTitle() {
        return "Dependencies";
    }

    @Override
    public void onEnter(AppContext ctx) {
        loadDependencies(ctx);
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    private void loadDependencies(AppContext ctx) {
        loading = true;
        error = null;
        ctx.requestRedraw();

        try {
            // Returns DependenciesProcessor.Root (deployment type) with public fields:
            // rootId (String), nodes (List<Node>), links (List<Link>)
            // Node has: id, name, value, description (all public)
            // Link has: source, target, type (String), direct (boolean)
            Object root = DevConsoleManager.invoke("devui-dependencies_pathToTarget", Map.of());
            loading = false;

            if (root != null) {
                parseDependencies(root);
            }
            categorizeDependencies();
            updateDepList();
            ctx.requestRedraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load dependencies: " + ex.getMessage();
            ctx.requestRedraw();
        }
    }

    private void parseDependencies(Object root) {
        allNodes.clear();
        allLinks.clear();

        try {
            JsonObject json = JsonObject.mapFrom(root);
            rootId = json.getString("rootId");

            JsonArray nodes = json.getJsonArray("nodes");
            if (nodes != null) {
                for (int i = 0; i < nodes.size(); i++) {
                    JsonObject n = nodes.getJsonObject(i);
                    DependencyNode node = new DependencyNode();
                    node.id = n.getString("id");
                    node.name = n.getString("name");
                    node.description = n.getString("description");

                    if (node.id != null) {
                        String[] parts = node.id.split(":");
                        if (parts.length >= 3) {
                            node.groupId = parts[0];
                            node.artifactId = parts[1];
                            node.version = parts[parts.length - 1];
                        }
                        node.isRoot = node.id.equals(rootId);
                    }

                    if (node.name != null) {
                        allNodes.add(node);
                    }
                }
            }

            JsonArray links = json.getJsonArray("links");
            if (links != null) {
                for (int i = 0; i < links.size(); i++) {
                    JsonObject l = links.getJsonObject(i);
                    DependencyLink link = new DependencyLink();
                    link.source = l.getString("source");
                    link.target = l.getString("target");
                    link.type = l.getString("type");
                    link.direct = l.getBoolean("direct", false);
                    allLinks.add(link);
                }
            }
        } catch (Exception e) {
            error = "Failed to parse dependencies: " + e.getMessage();
        }
    }

    private void categorizeDependencies() {
        runtimeDeps.clear();
        deploymentDeps.clear();
        directDeps.clear();

        // Build a map of node id to node
        Map<String, DependencyNode> nodeMap = new HashMap<>();
        for (DependencyNode node : allNodes) {
            nodeMap.put(node.id, node);
        }

        // Categorize based on links
        for (DependencyLink link : allLinks) {
            DependencyNode targetNode = nodeMap.get(link.target);
            if (targetNode != null) {
                if ("runtime".equals(link.type)) {
                    targetNode.isRuntime = true;
                    if (!runtimeDeps.contains(targetNode)) {
                        runtimeDeps.add(targetNode);
                    }
                } else if ("deployment".equals(link.type)) {
                    if (!deploymentDeps.contains(targetNode)) {
                        deploymentDeps.add(targetNode);
                    }
                }

                if (link.direct) {
                    targetNode.isDirect = true;
                    if (!directDeps.contains(targetNode)) {
                        directDeps.add(targetNode);
                    }
                }
            }
        }

        // Sort lists by name
        allNodes.sort((a, b) -> {
            if (a.isRoot)
                return -1;
            if (b.isRoot)
                return 1;
            return a.name.compareToIgnoreCase(b.name);
        });
        runtimeDeps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        deploymentDeps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        directDeps.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
    }

    private void updateDepList() {
        List<DependencyNode> sourceList;
        switch (currentTab) {
            case 1:
                sourceList = directDeps;
                break;
            case 2:
                sourceList = runtimeDeps;
                break;
            case 3:
                sourceList = deploymentDeps;
                break;
            default:
                sourceList = allNodes;
        }

        if (searchMode && searchQuery.length() > 0) {
            filteredNodes.clear();
            String query = searchQuery.toString().toLowerCase();
            for (DependencyNode node : sourceList) {
                if ((node.name != null && node.name.toLowerCase().contains(query)) ||
                        (node.groupId != null && node.groupId.toLowerCase().contains(query)) ||
                        (node.id != null && node.id.toLowerCase().contains(query))) {
                    filteredNodes.add(node);
                }
            }
            depList.setItems(filteredNodes);
        } else {
            depList.setItems(sourceList);
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

        // Search bar if active
        if (searchMode) {
            renderSearchBar(buffer, width);
        }

        // Tabs
        renderTabs(buffer, width);

        // Two-panel layout
        int panelWidth = Math.max(50, width / 2);

        // Left panel: Dependency list
        renderDepList(buffer, panelWidth, height);

        // Divider
        renderDivider(buffer, panelWidth, height);

        // Right panel: Dependency details
        renderDepDetails(buffer, panelWidth, width, height);

        // Footer
        renderFooter(buffer, width, height);
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Dependencies ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);

        // Legend
        int x = 1;
        x += buffer.setString(x, 2, "\u25C6", Style.create().cyan());
        x += buffer.setString(x, 2, " Root  ", Style.EMPTY);
        x += buffer.setString(x, 2, "\u25CF", Style.create().green());
        x += buffer.setString(x, 2, " Runtime  ", Style.EMPTY);
        x += buffer.setString(x, 2, "\u25CB", Style.create().gray());
        buffer.setString(x, 2, " Deployment", Style.EMPTY);
    }

    private void renderSearchBar(Buffer buffer, int width) {
        int x = 1;
        x += buffer.setString(x, 2, "Search: ", Style.create().yellow());
        x += buffer.setString(x, 2, searchQuery.toString(), Style.EMPTY);
        buffer.setString(x, 2, " ", Style.create().reversed());
    }

    private void renderTabs(Buffer buffer, int width) {
        int row = searchMode ? 4 : 3;
        int x = 1;

        String[] tabNames = {
                "All (" + allNodes.size() + ")",
                "Direct (" + directDeps.size() + ")",
                "Runtime (" + runtimeDeps.size() + ")",
                "Deployment (" + deploymentDeps.size() + ")"
        };

        for (int i = 0; i < tabNames.length; i++) {
            String label = " " + tabNames[i] + " ";
            if (i == currentTab) {
                x += buffer.setString(x, row, label, Style.create().bold().reversed());
            } else {
                x += buffer.setString(x, row, label, Style.create().gray());
            }
            x += buffer.setString(x, row, " ", Style.EMPTY);
        }
    }

    private void renderDepList(Buffer buffer, int panelWidth, int height) {
        int startRow = searchMode ? 6 : 5;
        List<DependencyNode> currentList = getCurrentList();

        if (currentList.isEmpty()) {
            buffer.setString(1, startRow, "No dependencies found", Style.create().gray());
            return;
        }

        depList.setVisibleRows(height - startRow - 3);
        depList.setWidth(panelWidth - 2);
        depList.render(buffer, startRow, 1);
    }

    private List<DependencyNode> getCurrentList() {
        if (searchMode && searchQuery.length() > 0) {
            return filteredNodes;
        }
        switch (currentTab) {
            case 1:
                return directDeps;
            case 2:
                return runtimeDeps;
            case 3:
                return deploymentDeps;
            default:
                return allNodes;
        }
    }

    private void renderDivider(Buffer buffer, int panelWidth, int height) {
        int startRow = searchMode ? 5 : 4;
        int col = panelWidth;
        for (int row = startRow; row < height - 1; row++) {
            buffer.setString(col, row, "\u2502", Style.create().gray());
        }
    }

    private void renderDepDetails(Buffer buffer, int panelWidth, int width, int height) {
        int startCol = panelWidth + 2;
        int contentWidth = width - panelWidth - 3;
        int startRow = searchMode ? 5 : 4;

        DependencyNode selected = depList.getSelectedItem();
        if (selected == null) {
            buffer.setString(startCol, startRow + 1, "Select a dependency to view details", Style.create().gray());
            return;
        }

        int row = startRow + 1;

        // Artifact name
        buffer.setString(startCol, row, selected.name, Style.create().cyan().bold());
        row += 2;

        // Group ID
        int x = startCol;
        x += buffer.setString(x, row, "Group: ", Style.create().cyan());
        buffer.setString(x, row, selected.groupId != null ? selected.groupId : "N/A", Style.EMPTY);
        row++;

        // Artifact ID
        x = startCol;
        x += buffer.setString(x, row, "Artifact: ", Style.create().cyan());
        buffer.setString(x, row, selected.artifactId != null ? selected.artifactId : "N/A", Style.EMPTY);
        row++;

        // Version
        x = startCol;
        x += buffer.setString(x, row, "Version: ", Style.create().cyan());
        buffer.setString(x, row, selected.version != null ? selected.version : "N/A", Style.create().green());
        row += 2;

        // Full GAV
        buffer.setString(startCol, row, "Full coordinates:", Style.create().cyan());
        row++;
        String gav = selected.id != null ? selected.id : "N/A";
        if (gav.length() > contentWidth - 2) {
            int pos = 0;
            while (pos < gav.length() && row < height - 4) {
                int end = Math.min(pos + contentWidth - 2, gav.length());
                buffer.setString(startCol, row, gav.substring(pos, end), Style.create().gray());
                pos = end;
                row++;
            }
        } else {
            buffer.setString(startCol, row, gav, Style.create().gray());
            row++;
        }
        row++;

        // Type indicators
        x = startCol;
        x += buffer.setString(x, row, "Type: ", Style.create().cyan());
        if (selected.isRoot) {
            x += buffer.setString(x, row, "Root", Style.create().red());
        } else if (selected.isRuntime) {
            x += buffer.setString(x, row, "Runtime", Style.create().green());
        } else {
            x += buffer.setString(x, row, "Deployment", Style.create().blue());
        }
        if (selected.isDirect) {
            x += buffer.setString(x, row, "  ", Style.EMPTY);
            buffer.setString(x, row, "[Direct]", Style.create().yellow());
        }
        row += 2;

        // Show dependencies (what this depends on)
        List<String> dependsOn = new ArrayList<>();
        for (DependencyLink link : allLinks) {
            if (link.source != null && link.source.equals(selected.id)) {
                dependsOn.add(link.target);
            }
        }
        if (!dependsOn.isEmpty()) {
            buffer.setString(startCol, row, "Depends on: " + dependsOn.size() + " dependencies", Style.create().cyan());
            row++;
        }

        // Show dependents (what depends on this)
        List<String> dependents = new ArrayList<>();
        for (DependencyLink link : allLinks) {
            if (link.target != null && link.target.equals(selected.id)) {
                dependents.add(link.source);
            }
        }
        if (!dependents.isEmpty()) {
            buffer.setString(startCol, row, "Used by: " + dependents.size() + " dependencies", Style.create().cyan());
        }
    }

    private void renderFooter(Buffer buffer, int width, int height) {
        if (searchMode) {
            buffer.setString(1, height - 2, "[Enter] Confirm  [Esc] Cancel search", Style.create().gray());
        } else {
            buffer.setString(1, height - 2, "[/] Search  [Tab] Switch tab  [R] Refresh  [Esc] Back", Style.create().gray());
        }
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        if (searchMode) {
            return handleSearchKey(key, ctx);
        }

        // Let list handle navigation
        if (depList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }

        switch (key) {
            case KeyCode.ESCAPE:
                ctx.goBack();
                return true;

            case KeyCode.TAB:
                currentTab = (currentTab + 1) % 4;
                updateDepList();
                ctx.requestRedraw();
                return true;

            case '/':
                searchMode = true;
                searchQuery.setLength(0);
                ctx.requestRedraw();
                return true;

            case 'r':
            case 'R':
                loadDependencies(ctx);
                return true;

            default:
                return false;
        }
    }

    private boolean handleSearchKey(int key, AppContext ctx) {
        switch (key) {
            case KeyCode.ESCAPE:
                searchMode = false;
                searchQuery.setLength(0);
                updateDepList();
                ctx.requestRedraw();
                return true;

            case KeyCode.ENTER:
                searchMode = false;
                ctx.requestRedraw();
                return true;

            case KeyCode.BACKSPACE:
                if (searchQuery.length() > 0) {
                    searchQuery.deleteCharAt(searchQuery.length() - 1);
                    updateDepList();
                    ctx.requestRedraw();
                }
                return true;

            default:
                if (key >= 32 && key < 127) {
                    searchQuery.append((char) key);
                    updateDepList();
                    ctx.requestRedraw();
                    return true;
                }
                return false;
        }
    }

    @Override
    public void onResize(int width, int height) {
        int panelWidth = Math.max(50, width / 2);
        int startRow = searchMode ? 6 : 5;
        depList.setWidth(panelWidth - 2);
        depList.setVisibleRows(height - startRow - 3);
    }

    /**
     * Data class for dependency nodes.
     */
    private static class DependencyNode {
        String id;
        String name;
        String description;
        String groupId;
        String artifactId;
        String version;
        boolean isRoot = false;
        boolean isRuntime = false;
        boolean isDirect = false;
    }

    /**
     * Data class for dependency links.
     */
    private static class DependencyLink {
        String source;
        String target;
        String type;
        boolean direct;
    }
}
