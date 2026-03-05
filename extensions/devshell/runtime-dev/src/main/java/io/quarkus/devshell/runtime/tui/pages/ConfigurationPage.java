package io.quarkus.devshell.runtime.tui.pages;

import java.util.ArrayList;
import java.util.List;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.terminal.Frame;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.ShellExtension;
import io.quarkus.devshell.runtime.tui.widgets.ListView;

/**
 * Page for the Configuration extension showing all config properties.
 */
public class ConfigurationPage extends BaseExtensionPage {

    private final ListView<ConfigProperty> propertyList;
    private String filter = "";
    private boolean filterMode = false;

    public ConfigurationPage(ShellExtension extension) {
        super(extension);
        this.propertyList = new ListView<>(prop -> {
            String source = prop.source != null ? " [" + prop.source + "]" : "";
            String value = prop.value != null ? prop.value : "<not set>";
            return prop.name + " = " + value + source;
        });
    }

    @Override
    public void loadData() {
        loading = true;
        error = null;
        redraw();

        callMethod("devui-configuration_getAllValues")
                .thenAccept(response -> {
                    loading = false;
                    parseConfiguration(response);
                    redraw();
                })
                .exceptionally(ex -> {
                    loading = false;
                    error = "Failed to load configuration: " + ex.getMessage();
                    redraw();
                    return null;
                });
    }

    private void parseConfiguration(String json) {
        List<ConfigProperty> properties = new ArrayList<>();
        try {
            if (json != null && json.contains("name")) {
                int idx = 0;
                while ((idx = json.indexOf("{", idx)) != -1) {
                    int endIdx = findMatchingBrace(json, idx);
                    if (endIdx == -1)
                        break;

                    String obj = json.substring(idx, endIdx + 1);
                    String name = extractJsonString(obj, "name");
                    String value = extractJsonString(obj, "value");
                    String configSource = extractJsonString(obj, "configSourceName");

                    if (name != null) {
                        properties.add(new ConfigProperty(name, value, configSource));
                    }
                    idx = endIdx + 1;
                }
            }
        } catch (Exception e) {
            error = "Failed to parse configuration: " + e.getMessage();
        }

        // Sort by name
        properties.sort((a, b) -> a.name.compareToIgnoreCase(b.name));

        applyFilter(properties);
    }

    private void applyFilter(List<ConfigProperty> allProperties) {
        if (filter.isEmpty()) {
            propertyList.setItems(allProperties);
        } else {
            String lowerFilter = filter.toLowerCase();
            List<ConfigProperty> filtered = new ArrayList<>();
            for (ConfigProperty prop : allProperties) {
                if (prop.name.toLowerCase().contains(lowerFilter) ||
                        (prop.value != null && prop.value.toLowerCase().contains(lowerFilter))) {
                    filtered.add(prop);
                }
            }
            propertyList.setItems(filtered);
        }
    }

    private String extractJsonString(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int idx = json.indexOf(searchKey);
        if (idx == -1)
            return null;

        int valueStart = idx + searchKey.length();
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }

        if (valueStart >= json.length())
            return null;

        char startChar = json.charAt(valueStart);
        if (startChar == '"') {
            int valueEnd = json.indexOf('"', valueStart + 1);
            if (valueEnd == -1)
                return null;
            return json.substring(valueStart + 1, valueEnd);
        } else if (startChar == 'n') {
            // null value
            return null;
        }
        return null;
    }

    private int findMatchingBrace(String json, int startIdx) {
        int depth = 0;
        for (int i = startIdx; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{')
                depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0)
                    return i;
            }
        }
        return -1;
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        renderHeader(buffer, width);

        int row = 3;

        // Filter indicator
        if (filterMode) {
        } else if (!filter.isEmpty()) {
        } else {
        }
        row += 2;

        // Properties header
        row++;

        row++;

        if (loading) {
            renderLoading(buffer, row);
        } else if (propertyList.isEmpty()) {
        } else {
            propertyList.setVisibleRows(height - row - 4);
            propertyList.setWidth(width - 4);
            propertyList.render(buffer, row, 2);

            // Show count
        }

        if (error != null) {
            renderError(buffer, height - 4);
        }

        renderFooter(buffer, filterMode ? "[Enter] Apply filter  [Esc] Cancel" : "[/] Filter  [Enter] View details");
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        if (filterMode) {
            return handleFilterKey(key, ctx);
        }

        if (propertyList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }

        switch (key) {
            case '/':
                filterMode = true;
                redraw();
                return true;

            case KeyCode.ENTER:
                // Could show property details in future
                return true;

            default:
                return super.handleKey(key, ctx);
        }
    }

    private boolean handleFilterKey(int key, AppContext ctx) {
        switch (key) {
            case KeyCode.ENTER:
                filterMode = false;
                loadData(); // Reload with filter
                return true;

            case KeyCode.ESCAPE:
                filterMode = false;
                filter = "";
                loadData();
                return true;

            case KeyCode.BACKSPACE:
                if (!filter.isEmpty()) {
                    filter = filter.substring(0, filter.length() - 1);
                    redraw();
                }
                return true;

            default:
                if (key >= 32 && key < 127) {
                    filter += (char) key;
                    redraw();
                }
                return true;
        }
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        propertyList.setVisibleRows(height - 10);
        propertyList.setWidth(width - 4);
    }

    @Override
    protected void renderPanelContent(Buffer buffer, int startRow, int startCol, int panelWidth, int panelHeight) {
        int row = startRow;

        if (loading) {
            return;
        }

        if (error != null) {
            return;
        }

        row++;

        if (propertyList.isEmpty()) {
            return;
        }

        int maxProps = Math.min(propertyList.size(), panelHeight - 3);
        propertyList.setVisibleRows(maxProps);
        propertyList.setWidth(panelWidth - 2);
        propertyList.render(buffer, row, startCol);

        row += maxProps;
        if (propertyList.size() > maxProps) {
        }
    }

    @Override
    public boolean handlePanelKey(int key, AppContext ctx) {
        if (propertyList.handleKey(key)) {
            return true;
        }
        return super.handlePanelKey(key, ctx);
    }

    private static class ConfigProperty {
        final String name;
        final String value;
        final String source;

        ConfigProperty(String name, String value, String source) {
            this.name = name;
            this.value = value;
            this.source = source;
        }
    }
}
