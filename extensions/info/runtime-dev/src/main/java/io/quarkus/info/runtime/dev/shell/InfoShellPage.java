package io.quarkus.info.runtime.dev.shell;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.layout.Constraint;
import dev.tamboui.layout.Layout;
import dev.tamboui.layout.Rect;
import dev.tamboui.style.Color;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import dev.tamboui.text.Line;
import dev.tamboui.text.Span;
import dev.tamboui.widgets.block.Block;
import dev.tamboui.widgets.block.Borders;
import dev.tamboui.widgets.paragraph.Paragraph;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.ShellExtension;
import io.quarkus.devshell.runtime.tui.pages.BaseExtensionPage;
import io.quarkus.info.BuildInfo;
import io.quarkus.info.GitInfo;
import io.quarkus.info.JavaInfo;
import io.quarkus.info.OsInfo;

/**
 * Shell page for the Info extension showing application and environment information.
 * Uses TamboUI widgets for declarative rendering.
 */
public class InfoShellPage extends BaseExtensionPage {

    @Inject
    OsInfo osInfo;

    @Inject
    JavaInfo javaInfo;

    @Inject
    Instance<BuildInfo> buildInfoInstance;

    @Inject
    Instance<GitInfo> gitInfoInstance;

    private boolean dataLoaded = false;

    // No-arg constructor for CDI
    public InfoShellPage() {
    }

    public InfoShellPage(ShellExtension extension) {
        this();
        setExtension(extension);
    }

    @Override
    public void loadData() {
        loading = true;
        error = null;
        dataLoaded = false;
        redraw();

        try {
            dataLoaded = true;
            loading = false;
            redraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load info: " + ex.getMessage();
            redraw();
        }
    }

    @Override
    public void render(Frame frame, AppContext ctx) {
        Buffer buffer = frame.buffer();
        renderHeader(buffer, width);

        if (loading) {
            renderLoading(buffer, 3);
            renderFooter(buffer, "");
            return;
        }

        if (!dataLoaded) {
            renderFooter(buffer, "");
            return;
        }

        // Content area: below header (row 2), above footer (row height-1)
        Rect content = new Rect(0, 2, width, height - 3);

        // Two columns side by side
        List<Rect> cols = Layout.horizontal()
                .constraints(Constraint.percentage(50), Constraint.percentage(50))
                .split(content);

        // Left column: OS + Java
        List<Rect> leftPanels = Layout.vertical()
                .constraints(
                        Constraint.length(osEntryCount() + 2),
                        Constraint.length(javaEntryCount() + 2),
                        Constraint.fill())
                .split(cols.get(0));

        renderKVBlock(buffer, leftPanels.get(0), "Operating System", buildOsLines());
        renderKVBlock(buffer, leftPanels.get(1), "Java", buildJavaLines());

        // Right column: Build + Git
        List<Rect> rightPanels = Layout.vertical()
                .constraints(
                        Constraint.length(buildEntryCount() + 2),
                        Constraint.length(gitEntryCount() + 2),
                        Constraint.fill())
                .split(cols.get(1));

        renderKVBlock(buffer, rightPanels.get(0), "Build", buildBuildLines());
        renderKVBlock(buffer, rightPanels.get(1), "Git", buildGitLines());

        if (error != null) {
            renderError(buffer, height - 4);
        }

        renderFooter(buffer, "");
    }

    /**
     * Render a bordered block with key-value lines inside using TamboUI widgets.
     */
    private void renderKVBlock(Buffer buffer, Rect area, String title, List<Line> lines) {
        Block block = Block.builder()
                .title(title)
                .borders(Borders.ALL)
                .borderColor(Color.GRAY)
                .build();

        Paragraph paragraph = Paragraph.builder()
                .text(new dev.tamboui.text.Text(lines, null))
                .build();

        // Render block border + paragraph content directly to buffer
        block.render(area, buffer);
        paragraph.render(block.inner(area), buffer);
    }

    // --- Entry builders ---

    private List<Line> buildOsLines() {
        List<Line> lines = new ArrayList<>();
        if (osInfo != null) {
            addKVLine(lines, "Name", osInfo.name());
            addKVLine(lines, "Version", osInfo.version());
            addKVLine(lines, "Arch", osInfo.architecture());
        }
        return lines;
    }

    private int osEntryCount() {
        return osInfo != null ? 3 : 0;
    }

    private List<Line> buildJavaLines() {
        List<Line> lines = new ArrayList<>();
        if (javaInfo != null) {
            addKVLine(lines, "Version", javaInfo.version());
            addKVLine(lines, "Vendor", javaInfo.vendor());
            addKVLine(lines, "Vendor Ver", javaInfo.vendorVersion());
        }
        return lines;
    }

    private int javaEntryCount() {
        return javaInfo != null ? 3 : 0;
    }

    private List<Line> buildBuildLines() {
        List<Line> lines = new ArrayList<>();
        if (buildInfoInstance.isResolvable()) {
            BuildInfo buildInfo = buildInfoInstance.get();
            addKVLine(lines, "Quarkus", buildInfo.quarkusVersion());
            addKVLine(lines, "Group", buildInfo.group());
            addKVLine(lines, "Artifact", buildInfo.artifact());
            addKVLine(lines, "Version", buildInfo.version());
            if (buildInfo.time() != null) {
                addKVLine(lines, "Time", buildInfo.time().toString());
            }
        }
        return lines;
    }

    private int buildEntryCount() {
        if (!buildInfoInstance.isResolvable())
            return 0;
        BuildInfo bi = buildInfoInstance.get();
        return 4 + (bi.time() != null ? 1 : 0);
    }

    private List<Line> buildGitLines() {
        List<Line> lines = new ArrayList<>();
        if (gitInfoInstance.isResolvable()) {
            GitInfo gitInfo = gitInfoInstance.get();
            addKVLine(lines, "Branch", gitInfo.branch());
            String commitId = gitInfo.latestCommitId();
            if (commitId != null && commitId.length() > 12) {
                commitId = commitId.substring(0, 12) + "...";
            }
            addKVLine(lines, "Commit", commitId);
            if (gitInfo.commitTime() != null) {
                addKVLine(lines, "Time", gitInfo.commitTime().toString());
            }
        }
        return lines;
    }

    private int gitEntryCount() {
        if (!gitInfoInstance.isResolvable())
            return 0;
        GitInfo gi = gitInfoInstance.get();
        return 2 + (gi.commitTime() != null ? 1 : 0);
    }

    private void addKVLine(List<Line> lines, String key, String value) {
        if (value == null)
            return;
        lines.add(Line.from(
                Span.styled(String.format("%-12s", key + ":"), Style.create().cyan()),
                Span.raw(" " + value)));
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }
        return super.handleKey(key, ctx);
    }

    @Override
    protected void renderPanelContent(Buffer buffer, int startRow, int startCol, int panelWidth, int panelHeight) {
        if (loading) {
            renderPanelLoading(buffer, startRow, startCol);
            return;
        }

        if (error != null) {
            renderPanelError(buffer, startRow, startCol, panelWidth);
            return;
        }

        if (!dataLoaded) {
            return;
        }

        // Compact panel view — use a small TamboUI block
        Rect area = new Rect(startCol - 1, startRow - 1, panelWidth, panelHeight);
        List<Line> lines = new ArrayList<>();
        if (buildInfoInstance.isResolvable()) {
            BuildInfo bi = buildInfoInstance.get();
            addKVLine(lines, "App", bi.artifact() + " v" + bi.version());
            addKVLine(lines, "Quarkus", bi.quarkusVersion());
        }
        if (javaInfo != null) {
            addKVLine(lines, "Java", javaInfo.version());
        }

        Paragraph paragraph = Paragraph.from(new dev.tamboui.text.Text(lines, null));
        paragraph.render(area, buffer);
    }
}
