package io.quarkus.datasource.runtime.dev.shell;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.terminal.Frame;
import io.quarkus.datasource.runtime.dev.ui.DatasourceJsonRpcService;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.ShellExtension;
import io.quarkus.devshell.runtime.tui.pages.BaseExtensionPage;
import io.quarkus.devshell.runtime.tui.widgets.ListView;

/**
 * Shell page for the Datasource extension.
 * Lists configured datasources and allows resetting their schema.
 */
public class DatasourceShellPage extends BaseExtensionPage {

    private ListView<String> datasourceList;
    private List<String> datasources = new ArrayList<>();
    private String lastActionResult = null;

    @Inject
    DatasourceJsonRpcService datasourceService;

    // No-arg constructor for CDI
    public DatasourceShellPage() {
        this.datasourceList = new ListView<>(ds -> ds);
    }

    public DatasourceShellPage(ShellExtension extension) {
        this();
        setExtension(extension);
    }

    @Override
    public void loadData() {
        loading = true;
        error = null;
        datasources.clear();
        lastActionResult = null;
        redraw();

        try {
            String json = getBuildTimeData("quarkus-datasource", "datasources");
            loading = false;
            if (json != null && !json.isEmpty()) {
                // Simple JSON array of strings: ["ds1","ds2"]
                String stripped = json.trim();
                if (stripped.startsWith("[") && stripped.endsWith("]")) {
                    stripped = stripped.substring(1, stripped.length() - 1).trim();
                    if (!stripped.isEmpty()) {
                        for (String part : stripped.split(",")) {
                            String name = part.trim().replace("\"", "");
                            if (!name.isEmpty()) {
                                datasources.add(name);
                            }
                        }
                    }
                }
            }
            datasourceList.setItems(datasources);
            redraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load datasources: " + ex.getMessage();
            redraw();
        }
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        renderHeader(buffer, width);

        int row = 3;

        row++;

        row++;

        if (loading) {
            renderLoading(buffer, row);
        } else if (datasources.isEmpty()) {
        } else {
            datasourceList.setVisibleRows(height - row - 6);
            datasourceList.setWidth(width - 4);
            datasourceList.render(buffer, row, 2);

        }

        // Show action result
        if (lastActionResult != null && !loading) {
        }

        if (error != null) {
            renderError(buffer, height - 3);
        }

        renderFooter(buffer, "");
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        if (datasourceList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }

        if (key == 'r' || key == 'R') {
            resetSelectedDatasource();
            return true;
        }

        return super.handleKey(key, ctx);
    }

    private void resetSelectedDatasource() {
        String selected = datasourceList.getSelectedItem();
        if (selected == null)
            return;

        loading = true;
        lastActionResult = null;
        redraw();

        try {
            datasourceService.reset(selected);
            loading = false;
            lastActionResult = "Schema reset: " + selected;
            loadData(); // refresh after reset
        } catch (Exception ex) {
            loading = false;
            lastActionResult = "Reset failed: " + ex.getMessage();
            redraw();
        }
    }

    @Override
    protected void renderPanelContent(Buffer buffer, int startRow, int startCol, int panelWidth, int panelHeight) {
        int row = startRow;

        if (loading) {
            renderPanelLoading(buffer, row, startCol);
            return;
        }

        if (error != null) {
            renderPanelError(buffer, row, startCol, panelWidth);
            return;
        }

        row++;

        if (datasources.isEmpty()) {
            return;
        }

        int max = Math.min(datasources.size(), panelHeight - 3);
        for (int i = 0; i < max; i++) {
            row++;
        }

        if (datasources.size() > max) {
        }
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        datasourceList.setVisibleRows(height - 12);
        datasourceList.setWidth(width - 4);
    }
}
