package io.quarkus.devshell.runtime.tui.screens;

import java.util.ArrayList;
import java.util.Collection;
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
 * Screen for viewing Dev Services started by the application.
 */
public class DevServicesScreen implements Screen {

    private boolean loading = true;
    private String error = null;

    private final List<DevService> services = new ArrayList<>();
    private final ListView<DevService> serviceList;

    public DevServicesScreen() {
        this.serviceList = new ListView<>(service -> {
            String icon = service.hasContainer ? "\u25A0 " : "\u25CB ";
            return icon + service.name;
        });
    }

    @Override
    public String getTitle() {
        return "Dev Services";
    }

    @Override
    public void onEnter(AppContext ctx) {
        loadDevServices(ctx);
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    private void loadDevServices(AppContext ctx) {
        loading = true;
        error = null;
        services.clear();
        ctx.requestRedraw();

        try {
            Object result = DevConsoleManager.invoke("devui-dev-services_getDevServices");
            // The action returns CompletableFuture<Collection<...>>
            if (result instanceof java.util.concurrent.CompletableFuture<?>) {
                result = ((java.util.concurrent.CompletableFuture<?>) result).join();
            }
            loading = false;

            if (result == null) {
                serviceList.setItems(services);
                ctx.requestRedraw();
                return;
            }

            Collection<?> devServices = (Collection<?>) result;
            for (Object ds : devServices) {
                JsonObject json = JsonObject.mapFrom(ds);
                DevService service = new DevService();

                service.name = json.getString("name", "Unknown");
                service.description = json.getString("description", "");

                // Container info (may be null)
                JsonObject containerInfo = json.getJsonObject("containerInfo");
                if (containerInfo != null) {
                    service.hasContainer = true;

                    String id = containerInfo.getString("id");
                    service.containerId = id != null ? id : "";

                    String imageName = containerInfo.getString("imageName");
                    service.imageName = imageName != null ? imageName : "";

                    String status = containerInfo.getString("status");
                    service.status = status != null ? status : "";

                    // Names (JsonArray of strings)
                    JsonArray namesArr = containerInfo.getJsonArray("names");
                    if (namesArr != null) {
                        service.containerNames = new ArrayList<>();
                        for (int i = 0; i < namesArr.size(); i++) {
                            service.containerNames.add(namesArr.getString(i));
                        }
                    }

                    // Exposed ports (JsonArray of objects: ip, privatePort, publicPort, type)
                    JsonArray portsArr = containerInfo.getJsonArray("exposedPorts");
                    if (portsArr != null) {
                        service.exposedPorts = new ArrayList<>();
                        for (int i = 0; i < portsArr.size(); i++) {
                            JsonObject port = portsArr.getJsonObject(i);
                            Object publicPort = port.getValue("publicPort");
                            if (publicPort != null) {
                                Object privatePort = port.getValue("privatePort");
                                String ip = port.getString("ip");
                                String type = port.getString("type");
                                String portStr = (ip != null ? ip : "0.0.0.0") + ":" +
                                        publicPort + "->" +
                                        privatePort + "/" +
                                        (type != null ? type : "tcp");
                                service.exposedPorts.add(portStr);
                            }
                        }
                    }

                    // Networks (JsonObject with string keys)
                    JsonObject networksObj = containerInfo.getJsonObject("networks");
                    if (networksObj != null) {
                        service.networks = new ArrayList<>(networksObj.fieldNames());
                    }
                }

                // Configs
                JsonObject configs = json.getJsonObject("configs");
                if (configs != null) {
                    service.configs = new HashMap<>();
                    for (String key : configs.fieldNames()) {
                        service.configs.put(key, configs.getString(key));
                    }
                }

                services.add(service);
            }

            serviceList.setItems(services);
            ctx.requestRedraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load dev services: " + ex.getMessage();
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

        if (services.isEmpty()) {
            buffer.setString(1, 4, "No dev services are currently running.", Style.create().gray());
            buffer.setString(1, 6, "Dev services start automatically when you use extensions that require them,",
                    Style.create().gray());
            buffer.setString(1, 7, "such as databases, message brokers, or other external services.", Style.create().gray());
            renderFooter(buffer, width, height);
            return;
        }

        // Two-panel layout: list on left, details on right
        int panelWidth = Math.max(30, width / 3);

        // Left panel: Service list
        renderServiceList(buffer, panelWidth, height);

        // Divider
        renderDivider(buffer, panelWidth, height);

        // Right panel: Selected service details
        renderServiceDetails(buffer, panelWidth, width, height);

        // Footer
        renderFooter(buffer, width, height);
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Dev Services ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);

        // Legend
        int x = 1;
        x += buffer.setString(x, 2, "\u25A0", Style.create().green());
        x += buffer.setString(x, 2, " Container  ", Style.EMPTY);
        x += buffer.setString(x, 2, "\u25CB", Style.create().yellow());
        buffer.setString(x, 2, " Non-container", Style.EMPTY);
    }

    private void renderServiceList(Buffer buffer, int panelWidth, int height) {
        buffer.setString(0, 3, " Services (" + services.size() + ")", Style.create().cyan().bold());

        serviceList.setVisibleRows(height - 7);
        serviceList.setWidth(panelWidth - 2);
        serviceList.render(buffer, 5, 1);
    }

    private void renderDivider(Buffer buffer, int panelWidth, int height) {
        int col = panelWidth;
        for (int row = 4; row < height - 1; row++) {
            buffer.setString(col, row, "\u2502", Style.create().gray());
        }
    }

    private void renderServiceDetails(Buffer buffer, int panelWidth, int width, int height) {
        DevService selected = serviceList.getSelectedItem();
        if (selected == null) {
            return;
        }

        int rightPanelStart = panelWidth + 2;
        int row = 4;

        // Service name
        buffer.setString(rightPanelStart, row, selected.name, Style.create().cyan().bold());
        row++;

        buffer.setString(rightPanelStart, row, "\u2550".repeat(Math.min(selected.name.length(), width - rightPanelStart - 2)),
                Style.create().gray());
        row += 2;

        // Description
        if (selected.description != null && !selected.description.isEmpty()) {
            buffer.setString(rightPanelStart, row, selected.description, Style.EMPTY);
            row += 2;
        }

        // Container info
        if (selected.hasContainer) {
            buffer.setString(rightPanelStart, row, "\u25A0 Container", Style.create().green());
            row++;

            if (selected.containerId != null && !selected.containerId.isEmpty()) {
                int x = rightPanelStart;
                x += buffer.setString(x, row, "ID: ", Style.create().cyan());
                buffer.setString(x, row,
                        selected.containerId.length() > 12 ? selected.containerId.substring(0, 12) : selected.containerId,
                        Style.EMPTY);
                row++;
            }

            if (selected.imageName != null && !selected.imageName.isEmpty()) {
                int x = rightPanelStart;
                x += buffer.setString(x, row, "Image: ", Style.create().cyan());
                String image = selected.imageName;
                int maxLen = width - rightPanelStart - 10;
                if (image.length() > maxLen) {
                    image = "..." + image.substring(image.length() - maxLen + 3);
                }
                buffer.setString(x, row, image, Style.EMPTY);
                row++;
            }

            if (selected.status != null && !selected.status.isEmpty()) {
                int x = rightPanelStart;
                x += buffer.setString(x, row, "Status: ", Style.create().cyan());
                buffer.setString(x, row, selected.status, Style.create().green());
                row++;
            }

            if (selected.exposedPorts != null && !selected.exposedPorts.isEmpty()) {
                int x = rightPanelStart;
                x += buffer.setString(x, row, "Ports: ", Style.create().cyan());
                buffer.setString(x, row, String.join(", ", selected.exposedPorts), Style.EMPTY);
                row++;
            }

            if (selected.networks != null && !selected.networks.isEmpty()) {
                int x = rightPanelStart;
                x += buffer.setString(x, row, "Networks: ", Style.create().cyan());
                buffer.setString(x, row, String.join(", ", selected.networks), Style.EMPTY);
                row++;
            }

            row++;
        } else {
            buffer.setString(rightPanelStart, row, "\u25CB Non-container service", Style.create().yellow());
            row += 2;
        }

        // Config properties
        if (selected.configs != null && !selected.configs.isEmpty()) {
            buffer.setString(rightPanelStart, row, "Configuration:", Style.create().cyan().bold());
            row++;

            int maxRows = height - row - 3;
            int count = 0;
            for (Map.Entry<String, String> entry : selected.configs.entrySet()) {
                if (count >= maxRows) {
                    break;
                }
                String key = entry.getKey();
                String value = entry.getValue();
                int maxKeyLen = 30;
                int maxValLen = width - rightPanelStart - maxKeyLen - 5;
                if (key.length() > maxKeyLen) {
                    key = "..." + key.substring(key.length() - maxKeyLen + 3);
                }
                if (maxValLen > 3 && value.length() > maxValLen) {
                    value = value.substring(0, maxValLen - 3) + "...";
                }
                int x = rightPanelStart;
                x += buffer.setString(x, row, key, Style.create().cyan());
                x += buffer.setString(x, row, " = ", Style.create().gray());
                buffer.setString(x, row, value, Style.EMPTY);
                row++;
                count++;
            }
        }
    }

    private void renderFooter(Buffer buffer, int width, int height) {
        buffer.setString(1, height - 2, "[R] Refresh  [Esc] Back", Style.create().gray());
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        // Let list handle navigation
        if (serviceList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }

        switch (key) {
            case KeyCode.ESCAPE:
                ctx.goBack();
                return true;

            case 'r':
            case 'R':
                loadDevServices(ctx);
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onResize(int width, int height) {
        int panelWidth = Math.max(30, width / 3);
        serviceList.setWidth(panelWidth - 2);
        serviceList.setVisibleRows(height - 7);
    }

    /**
     * Data class for dev service information.
     */
    private static class DevService {
        String name;
        String description;
        boolean hasContainer;
        String containerId;
        String imageName;
        String status;
        List<String> containerNames;
        List<String> exposedPorts;
        List<String> networks;
        Map<String, String> configs;
    }
}
