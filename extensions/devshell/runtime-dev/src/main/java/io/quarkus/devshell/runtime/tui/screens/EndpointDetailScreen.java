package io.quarkus.devshell.runtime.tui.screens;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.Screen;

/**
 * Screen for viewing details of a single endpoint.
 */
public class EndpointDetailScreen implements Screen {

    private final EndpointsScreen.EndpointItem endpoint;

    public EndpointDetailScreen(EndpointsScreen.EndpointItem endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public String getTitle() {
        return "Endpoint: " + endpoint.uri;
    }

    @Override
    public void onEnter(AppContext ctx) {
        // Nothing to initialize
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        int width = ctx.getWidth();
        int height = ctx.getHeight();

        // Header
        renderHeader(buffer, width);

        int row = 3;

        // URI
        row++;

        row += 2;

        // Parse and display description components
        String description = endpoint.description;
        if (description != null && !description.isEmpty()) {
            // HTTP Method
            String httpMethod = extractHttpMethod(description);
            if (!httpMethod.isEmpty()) {
                row++;

                row += 2;
            }

            // Consumes
            String consumes = extractPart(description, "consumes:");
            if (!consumes.isEmpty()) {
                row++;

                row += 2;
            }

            // Produces
            String produces = extractPart(description, "produces:");
            if (!produces.isEmpty()) {
                row++;

                row += 2;
            }

            // Java Method
            String javaMethod = extractPart(description, "java:");
            if (!javaMethod.isEmpty()) {
                row++;

                row += 2;
            }

            // If no parts extracted, show raw description
            if (httpMethod.isEmpty() && consumes.isEmpty() && produces.isEmpty() && javaMethod.isEmpty()) {
                row++;

                row += 2;
            }
        }

        // Footer
        buffer.setString(1, height - 2, "[Esc] Back", Style.create().gray());
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Endpoint Detail ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);
    }

    private String extractHttpMethod(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        int spaceIdx = description.indexOf(' ');
        int parenIdx = description.indexOf('(');
        if (parenIdx > 0 && (spaceIdx < 0 || parenIdx < spaceIdx)) {
            return description.substring(0, parenIdx).trim();
        }
        if (spaceIdx > 0) {
            return description.substring(0, spaceIdx);
        }
        // Check if it's just an HTTP method
        String upper = description.toUpperCase();
        if (upper.equals("GET") || upper.equals("POST") || upper.equals("PUT") ||
                upper.equals("DELETE") || upper.equals("PATCH") || upper.equals("HEAD") ||
                upper.equals("OPTIONS") || upper.equals("SERVLET")) {
            return description;
        }
        return "";
    }

    private String extractPart(String description, String key) {
        int startIdx = description.indexOf("(" + key);
        if (startIdx < 0) {
            return "";
        }
        int valueStart = startIdx + key.length() + 2; // +2 for '(' and ':'
        int endIdx = description.indexOf(')', valueStart);
        if (endIdx > valueStart) {
            return description.substring(valueStart, endIdx).trim();
        }
        return "";
    }

    private String formatHttpMethod(String method) {
        return method.toUpperCase();
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        switch (key) {
            case KeyCode.ESCAPE:
                ctx.goBack();
                return true;

            default:
                return false;
        }
    }

    @Override
    public void onResize(int width, int height) {
        // Nothing to resize
    }
}
