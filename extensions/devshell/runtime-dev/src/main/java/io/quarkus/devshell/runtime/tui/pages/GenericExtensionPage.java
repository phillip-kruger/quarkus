package io.quarkus.devshell.runtime.tui.pages;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.terminal.Frame;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.ShellExtension;

/**
 * Generic fallback page for extensions without custom shell pages.
 * Shows basic extension information.
 */
public class GenericExtensionPage extends BaseExtensionPage {

    public GenericExtensionPage(ShellExtension extension) {
        super(extension);
    }

    @Override
    public void loadData() {
        // Nothing to load for generic page
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        renderHeader(buffer, width);

        int row = 3;

        // Extension info section
        row++;

        row += 2;

        // Name
        row++;

        // Namespace
        if (extension.namespace() != null && !extension.namespace().isEmpty()) {
            row++;
        }

        // Status
        if (extension.isActive()) {
        } else {
        }
        row += 2;

        // Description
        if (extension.description() != null && !extension.description().isEmpty()) {
            row++;

            row += 2;

            // Wrap description text
            String desc = extension.description();
            int maxWidth = width - 6;
            while (!desc.isEmpty() && row < height - 5) {
                int lineLen = Math.min(desc.length(), maxWidth);
                // Find word boundary
                if (lineLen < desc.length()) {
                    int lastSpace = desc.lastIndexOf(' ', lineLen);
                    if (lastSpace > 0) {
                        lineLen = lastSpace;
                    }
                }
                desc = desc.substring(lineLen).trim();
                row++;
            }
        }

        // Note about shell page
        row = height - 4;

        // Footer
        renderFooter(buffer, "");
    }

    @Override
    protected void renderPanelContent(Buffer buffer, int startRow, int startCol, int panelWidth, int panelHeight) {
        int row = startRow;

        // Show basic info in panel mode
        if (extension.isActive()) {
        } else {
        }
        row++;

        if (extension.description() != null && !extension.description().isEmpty()) {
            row++;
        }
    }

    @Override
    public boolean handlePanelKey(int key, AppContext ctx) {
        return super.handlePanelKey(key, ctx);
    }
}
