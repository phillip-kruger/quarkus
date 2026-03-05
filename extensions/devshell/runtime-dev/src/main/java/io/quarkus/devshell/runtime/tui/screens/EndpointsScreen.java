package io.quarkus.devshell.runtime.tui.screens;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.Screen;
import io.quarkus.devshell.runtime.tui.widgets.TableView;
import io.quarkus.vertx.http.runtime.devmode.ResourceNotFoundData;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Screen for viewing all endpoints exposed by the application.
 */
public class EndpointsScreen implements Screen {

    private enum Tab {
        REST("REST Endpoints"),
        STATIC("Static Resources"),
        ADDITIONAL("Additional Endpoints");

        final String label;

        Tab(String label) {
            this.label = label;
        }
    }

    private Tab currentTab = Tab.REST;
    private boolean loading = true;
    private String error = null;

    // Filter
    private boolean filterMode = false;
    private final StringBuilder filterInput = new StringBuilder();
    private String filterText = "";

    // Data storage
    private final List<EndpointItem> restEndpoints = new ArrayList<>();
    private final List<EndpointItem> staticResources = new ArrayList<>();
    private final List<EndpointItem> additionalEndpoints = new ArrayList<>();

    // Filtered lists
    private List<EndpointItem> filteredRestEndpoints = new ArrayList<>();
    private List<EndpointItem> filteredStaticResources = new ArrayList<>();
    private List<EndpointItem> filteredAdditionalEndpoints = new ArrayList<>();

    // Tables for each tab
    private final TableView<EndpointItem> restTable;
    private final TableView<EndpointItem> staticTable;
    private final TableView<EndpointItem> additionalTable;

    @Inject
    ResourceNotFoundData service;

    public EndpointsScreen() {
        // REST endpoints table with method, path, and description
        this.restTable = new TableView<EndpointItem>()
                .addColumn("Method", e -> getHttpMethod(e.description), 8)
                .addColumn("Path", e -> e.uri, 40)
                .addColumn("Details", e -> getDetails(e.description), 30);

        // Static resources table
        this.staticTable = new TableView<EndpointItem>()
                .addColumn("Path", e -> e.uri, 60)
                .addColumn("Type", e -> getFileType(e.uri), 10);

        // Additional endpoints table
        this.additionalTable = new TableView<EndpointItem>()
                .addColumn("Path", e -> e.uri, 40)
                .addColumn("Description", e -> e.description, 40);
    }

    @Override
    public String getTitle() {
        return "Endpoints";
    }

    @Override
    public void onEnter(AppContext ctx) {
        loadEndpoints(ctx);
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    private void loadEndpoints(AppContext ctx) {
        loading = true;
        error = null;
        ctx.requestRedraw();

        try {
            JsonObject json = service.getAllEndpoints();
            loading = false;
            parseEndpoints(json);
            applyFilter();
            ctx.requestRedraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load endpoints: " + ex.getMessage();
            ctx.requestRedraw();
        }
    }

    private void parseEndpoints(JsonObject json) {
        restEndpoints.clear();
        staticResources.clear();
        additionalEndpoints.clear();

        try {
            if (json == null) {
                error = "Empty response from server";
                return;
            }

            // Resource Endpoints
            JsonArray resourceEndpoints = json.getJsonArray("Resource Endpoints");
            if (resourceEndpoints != null) {
                for (int i = 0; i < resourceEndpoints.size(); i++) {
                    JsonObject endpoint = resourceEndpoints.getJsonObject(i);
                    restEndpoints.add(new EndpointItem(
                            endpoint.getString("uri", ""),
                            endpoint.getString("description", "")));
                }
            }

            // Servlet mappings (add to REST endpoints)
            JsonArray servletMappings = json.getJsonArray("Servlet mappings");
            if (servletMappings != null) {
                for (int i = 0; i < servletMappings.size(); i++) {
                    JsonObject servlet = servletMappings.getJsonObject(i);
                    restEndpoints.add(new EndpointItem(
                            servlet.getString("uri", ""),
                            "SERVLET"));
                }
            }

            // Static resources
            JsonArray staticRes = json.getJsonArray("Static resources");
            if (staticRes != null) {
                for (int i = 0; i < staticRes.size(); i++) {
                    JsonObject resource = staticRes.getJsonObject(i);
                    staticResources.add(new EndpointItem(
                            resource.getString("uri", ""),
                            resource.getString("description", "")));
                }
            }

            // Additional endpoints
            JsonArray additionalEp = json.getJsonArray("Additional endpoints");
            if (additionalEp != null) {
                for (int i = 0; i < additionalEp.size(); i++) {
                    JsonObject endpoint = additionalEp.getJsonObject(i);
                    additionalEndpoints.add(new EndpointItem(
                            endpoint.getString("uri", ""),
                            endpoint.getString("description", "")));
                }
            }
        } catch (Exception e) {
            error = "Failed to parse endpoints: " + e.getMessage();
        }
    }

    private void applyFilter() {
        String filter = filterText.toLowerCase();

        filteredRestEndpoints = restEndpoints.stream()
                .filter(e -> matchesFilter(e, filter))
                .toList();

        filteredStaticResources = staticResources.stream()
                .filter(e -> matchesFilter(e, filter))
                .toList();

        filteredAdditionalEndpoints = additionalEndpoints.stream()
                .filter(e -> matchesFilter(e, filter))
                .toList();

        restTable.setItems(filteredRestEndpoints);
        staticTable.setItems(filteredStaticResources);
        additionalTable.setItems(filteredAdditionalEndpoints);
    }

    private boolean matchesFilter(EndpointItem item, String filter) {
        if (filter.isEmpty()) {
            return true;
        }
        return item.uri.toLowerCase().contains(filter) ||
                item.description.toLowerCase().contains(filter);
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        int width = ctx.getWidth();
        int height = ctx.getHeight();

        // Header
        renderHeader(buffer, width);

        // Tabs
        renderTabs(buffer, width);

        if (loading) {
            buffer.setString(width / 2 - 5, height / 2, "Loading...", Style.create().yellow());
            return;
        }

        if (error != null) {
            buffer.setString(1, 4, error, Style.create().red());
            renderFooter(buffer, width, height);
            return;
        }

        // Content area
        int contentStartRow = 5;
        int contentHeight = height - 7;

        // Filter indicator
        if (filterMode) {
            String filterDisplay = filterInput.toString();
            if (filterDisplay.length() < 30) {
                filterDisplay = filterDisplay + " ".repeat(30 - filterDisplay.length());
            }
            contentStartRow++;
            contentHeight--;
        } else if (!filterText.isEmpty()) {
            contentStartRow++;
            contentHeight--;
        }

        // Render current tab's table
        TableView<EndpointItem> currentTable = getCurrentTable();
        currentTable.setVisibleRows(contentHeight);
        currentTable.setWidth(width - 4);
        currentTable.render(buffer, contentStartRow, 2);

        // Footer
        renderFooter(buffer, width, height);
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Endpoints ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);
    }

    private void renderTabs(Buffer buffer, int width) {
        int x = 1;
        for (Tab tab : Tab.values()) {
            String label = " " + tab.label + " (" + getTabCount(tab) + ") ";
            if (tab == currentTab) {
                x += buffer.setString(x, 2, label, Style.create().bold().reversed());
            } else {
                x += buffer.setString(x, 2, label, Style.create().gray());
            }
            x += buffer.setString(x, 2, " ", Style.EMPTY);
        }
    }

    private int getTabCount(Tab tab) {
        switch (tab) {
            case REST:
                return filterText.isEmpty() ? restEndpoints.size() : filteredRestEndpoints.size();
            case STATIC:
                return filterText.isEmpty() ? staticResources.size() : filteredStaticResources.size();
            case ADDITIONAL:
                return filterText.isEmpty() ? additionalEndpoints.size() : filteredAdditionalEndpoints.size();
            default:
                return 0;
        }
    }

    private int getCurrentFilteredCount() {
        switch (currentTab) {
            case REST:
                return filteredRestEndpoints.size();
            case STATIC:
                return filteredStaticResources.size();
            case ADDITIONAL:
                return filteredAdditionalEndpoints.size();
            default:
                return 0;
        }
    }

    private int getCurrentTotalCount() {
        switch (currentTab) {
            case REST:
                return restEndpoints.size();
            case STATIC:
                return staticResources.size();
            case ADDITIONAL:
                return additionalEndpoints.size();
            default:
                return 0;
        }
    }

    private TableView<EndpointItem> getCurrentTable() {
        switch (currentTab) {
            case REST:
                return restTable;
            case STATIC:
                return staticTable;
            case ADDITIONAL:
                return additionalTable;
            default:
                return restTable;
        }
    }

    private void renderFooter(Buffer buffer, int width, int height) {
        if (filterMode) {
            buffer.setString(1, height - 2, "[Enter] Apply  [Esc] Cancel", Style.create().gray());
        } else {
            buffer.setString(1, height - 2, "[Tab] Switch tab  [/] Filter  [Enter] Open  [R] Refresh  [Esc] Back",
                    Style.create().gray());
        }
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        if (filterMode) {
            return handleFilterKey(key, ctx);
        }

        switch (key) {
            case KeyCode.ESCAPE:
                if (!filterText.isEmpty()) {
                    filterText = "";
                    applyFilter();
                    ctx.requestRedraw();
                } else {
                    ctx.goBack();
                }
                return true;

            case KeyCode.TAB:
                // Switch to next tab
                Tab[] tabs = Tab.values();
                int current = currentTab.ordinal();
                currentTab = tabs[(current + 1) % tabs.length];
                ctx.requestRedraw();
                return true;

            case '/':
                filterMode = true;
                filterInput.setLength(0);
                filterInput.append(filterText);
                ctx.requestRedraw();
                return true;

            case 'r':
            case 'R':
                loadEndpoints(ctx);
                return true;

            case KeyCode.ENTER:
                EndpointItem selected = getCurrentTable().getSelectedItem();
                if (selected != null) {
                    ctx.navigateTo(new EndpointDetailScreen(selected));
                }
                return true;

            default:
                if (getCurrentTable().handleKey(key)) {
                    ctx.requestRedraw();
                    return true;
                }
                return false;
        }
    }

    private boolean handleFilterKey(int key, AppContext ctx) {
        switch (key) {
            case KeyCode.ESCAPE:
                filterMode = false;
                ctx.requestRedraw();
                return true;

            case KeyCode.ENTER:
                filterMode = false;
                filterText = filterInput.toString();
                applyFilter();
                ctx.requestRedraw();
                return true;

            case KeyCode.BACKSPACE:
                if (filterInput.length() > 0) {
                    filterInput.deleteCharAt(filterInput.length() - 1);
                    ctx.requestRedraw();
                }
                return true;

            default:
                if (key >= 32 && key < 127) {
                    filterInput.append((char) key);
                    ctx.requestRedraw();
                    return true;
                }
                return false;
        }
    }

    @Override
    public void onResize(int width, int height) {
        // Tables handle their own resizing
    }

    // Helper methods for parsing endpoint descriptions
    private String getHttpMethod(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        // Description format: "GET (consumes: ...) (produces: ...) (java: ...)"
        int spaceIdx = description.indexOf(' ');
        int parenIdx = description.indexOf('(');
        if (parenIdx > 0 && (spaceIdx < 0 || parenIdx < spaceIdx)) {
            return description.substring(0, parenIdx).trim();
        }
        if (spaceIdx > 0) {
            return description.substring(0, spaceIdx);
        }
        return description;
    }

    private String getDetails(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        // Extract java method if present
        int javaIdx = description.indexOf("(java:");
        if (javaIdx >= 0) {
            int endIdx = description.indexOf(')', javaIdx);
            if (endIdx > javaIdx) {
                return description.substring(javaIdx + 6, endIdx).trim();
            }
        }
        // Otherwise return consumes/produces
        int parenIdx = description.indexOf('(');
        if (parenIdx > 0) {
            return description.substring(parenIdx);
        }
        return "";
    }

    private String getFileType(String uri) {
        if (uri == null) {
            return "";
        }
        int dotIdx = uri.lastIndexOf('.');
        if (dotIdx > 0 && dotIdx < uri.length() - 1) {
            return uri.substring(dotIdx + 1).toUpperCase();
        }
        return "FILE";
    }

    /**
     * Data class for endpoint items.
     */
    public static class EndpointItem {
        public final String uri;
        public final String description;

        public EndpointItem(String uri, String description) {
            this.uri = uri;
            this.description = description != null ? description : "";
        }
    }
}
