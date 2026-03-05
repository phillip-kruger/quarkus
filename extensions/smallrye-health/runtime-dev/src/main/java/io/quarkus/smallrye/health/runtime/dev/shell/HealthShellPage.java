package io.quarkus.smallrye.health.runtime.dev.shell;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.terminal.Frame;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.ShellExtension;
import io.quarkus.devshell.runtime.tui.pages.BaseExtensionPage;
import io.quarkus.devshell.runtime.tui.widgets.ListView;
import io.quarkus.smallrye.health.runtime.dev.ui.HealthJsonRPCService;
import io.smallrye.health.SmallRyeHealth;

public class HealthShellPage extends BaseExtensionPage {

    @Inject
    HealthJsonRPCService healthService;

    private final ListView<HealthCheck> checkList;
    private List<HealthCheck> allChecks = new ArrayList<>();
    private String overallStatus = "UNKNOWN";

    public HealthShellPage() {
        this.checkList = new ListView<>(check -> {
            String icon = check.status.equals("UP") ? "+" : "x";
            return icon + " " + check.name;
        });
    }

    public HealthShellPage(ShellExtension extension) {
        this();
        setExtension(extension);
    }

    @Override
    public void loadData() {
        loading = true;
        error = null;
        allChecks.clear();
        redraw();

        try {
            SmallRyeHealth health = healthService.getHealth().await().indefinitely();
            loading = false;

            if (health != null) {
                overallStatus = health.isDown() ? "DOWN" : "UP";

                jakarta.json.JsonObject payload = health.getPayload();
                if (payload != null && payload.containsKey("checks")) {
                    jakarta.json.JsonArray checks = payload.getJsonArray("checks");
                    for (int i = 0; i < checks.size(); i++) {
                        jakarta.json.JsonObject check = checks.getJsonObject(i);
                        String name = check.getString("name", null);
                        String status = check.getString("status", "UNKNOWN");
                        jakarta.json.JsonObject data = check.containsKey("data") ? check.getJsonObject("data") : null;
                        if (name != null) {
                            allChecks.add(new HealthCheck(name, status, data != null ? data.toString() : null));
                        }
                    }
                }
            }

            checkList.setItems(allChecks);
            redraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load health: " + ex.getMessage();
            redraw();
        }
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        renderHeader(buffer, width);

        int row = 3;

        // Overall status
        if (overallStatus.equals("UP")) {
        } else {
        }
        row += 2;

        // Health checks list
        row++;

        if (loading) {
            renderLoading(buffer, row);
        } else if (checkList.isEmpty()) {
        } else {
            checkList.setVisibleRows(height - row - 4);
            checkList.setWidth(width - 4);
            checkList.render(buffer, row, 2);
        }

        if (error != null) {
            renderError(buffer, height - 3);
        }

        renderFooter(buffer, "");
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (checkList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }
        return super.handleKey(key, ctx);
    }

    @Override
    public void onResize(int width, int height) {
        super.onResize(width, height);
        checkList.setVisibleRows(height - 10);
        checkList.setWidth(width - 4);
    }

    private static class HealthCheck {
        final String name;
        final String status;
        final String data;

        HealthCheck(String name, String status, String data) {
            this.name = name;
            this.status = status;
            this.data = data;
        }
    }
}
