package io.quarkus.devshell.runtime.tui.screens;

import java.util.ArrayList;
import java.util.List;

import jakarta.inject.Inject;

import dev.tamboui.buffer.Buffer;
import dev.tamboui.style.Style;
import dev.tamboui.terminal.Frame;
import io.quarkus.dev.console.DevConsoleManager;
import io.quarkus.devshell.runtime.tui.AppContext;
import io.quarkus.devshell.runtime.tui.KeyCode;
import io.quarkus.devshell.runtime.tui.Screen;
import io.quarkus.devshell.runtime.tui.widgets.ListView;
import io.quarkus.devui.runtime.continuoustesting.ContinuousTestingJsonRPCService;
import io.quarkus.devui.runtime.continuoustesting.ContinuousTestingJsonRPCState;

/**
 * Screen for Continuous Testing management.
 */
public class ContinuousTestingScreen implements Screen {

    private boolean loading = true;
    private String error = null;

    // Status
    private boolean isRunning = false;
    private boolean inProgress = false;
    private boolean brokenOnly = false;

    // Counts
    private long passedCount = 0;
    private long failedCount = 0;
    private long skippedCount = 0;
    private long totalTime = 0;

    // Test results
    private final List<TestItem> passedTests = new ArrayList<>();
    private final List<TestItem> failedTests = new ArrayList<>();
    private final List<TestItem> skippedTests = new ArrayList<>();

    // Current tab: 0 = Passed, 1 = Failed, 2 = Skipped
    private int currentTab = 0;
    private final ListView<TestItem> testList;

    @Inject
    ContinuousTestingJsonRPCService service;

    public ContinuousTestingScreen() {
        this.testList = new ListView<>(item -> {
            String icon = getTestIcon(item);
            String time = formatTime(item.time);
            return icon + " " + item.displayName + " (" + time + ")";
        });
    }

    private String getTestIcon(TestItem item) {
        if (item.hasProblem) {
            return "\u2717"; // ✗
        }
        return "\u2713"; // ✓
    }

    private String formatTime(long ms) {
        if (ms < 1000) {
            return ms + "ms";
        }
        return String.format("%.2fs", ms / 1000.0);
    }

    @Override
    public String getTitle() {
        return "Continuous Testing";
    }

    @Override
    public void onEnter(AppContext ctx) {
        loadData(ctx);
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    private void loadData(AppContext ctx) {
        loading = true;
        error = null;
        passedTests.clear();
        failedTests.clear();
        skippedTests.clear();
        ctx.requestRedraw();

        try {
            ContinuousTestingJsonRPCState state = service.currentState();
            loading = false;
            applyState(state);
            updateTestList();
            ctx.requestRedraw();
        } catch (Exception ex) {
            loading = false;
            error = "Failed to load state: " + ex.getMessage();
            ctx.requestRedraw();
        }
    }

    private void applyState(ContinuousTestingJsonRPCState state) {
        if (state == null) {
            return;
        }

        inProgress = state.isInProgress();

        ContinuousTestingJsonRPCState.Config config = state.getConfig();
        if (config != null) {
            isRunning = config.isEnabled();
            brokenOnly = config.isBrokenOnly();
        }

        ContinuousTestingJsonRPCState.Result result = state.getResult();
        if (result != null) {
            ContinuousTestingJsonRPCState.Result.Counts counts = result.getCounts();
            if (counts != null) {
                passedCount = counts.getPassed();
                failedCount = counts.getFailed();
                skippedCount = counts.getSkipped();
            }

            totalTime = result.getTotalTime();

            addItems(result.getPassed(), passedTests, false);
            addItems(result.getFailed(), failedTests, true);
            addItems(result.getSkipped(), skippedTests, false);
        }
    }

    private void addItems(ContinuousTestingJsonRPCState.Result.Item[] items, List<TestItem> target, boolean hasProblem) {
        if (items == null) {
            return;
        }
        for (ContinuousTestingJsonRPCState.Result.Item item : items) {
            TestItem ti = new TestItem();
            ti.className = item.getClassName();
            ti.displayName = item.getDisplayName();
            ti.time = item.getTime();
            ti.hasProblem = hasProblem;

            if (hasProblem && item.getProblems() != null && item.getProblems().length > 0) {
                Throwable problem = item.getProblems()[0];
                ti.problemMessage = problem.getMessage() != null ? problem.getMessage() : "Test failed";
            }

            if (ti.displayName != null && !ti.displayName.isEmpty()) {
                target.add(ti);
            }
        }
    }

    private void updateTestList() {
        switch (currentTab) {
            case 0 -> testList.setItems(passedTests);
            case 1 -> testList.setItems(failedTests);
            case 2 -> testList.setItems(skippedTests);
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
            renderFooter(buffer, height);
            return;
        }

        // Status bar
        renderStatusBar(buffer, width);

        // Tabs
        renderTabs(buffer);

        // Two-panel layout: test list on left, details on right
        int panelWidth = Math.max(40, width / 2);

        // Left panel: Test list
        renderTestList(buffer, panelWidth, height);

        // Divider
        renderDivider(buffer, panelWidth, height);

        // Right panel: Test details
        renderTestDetails(buffer, panelWidth, width, height);

        // Footer
        renderFooter(buffer, height);
    }

    private void renderHeader(Buffer buffer, int width) {
        String title = " Continuous Testing ";
        int padding = (width - title.length()) / 2;

        Style headerStyle = Style.create().white().onBlue().bold();
        String headerLine = " ".repeat(Math.max(0, padding)) + title
                + " ".repeat(Math.max(0, width - padding - title.length()));
        buffer.setString(0, 0, headerLine, headerStyle);
    }

    private void renderStatusBar(Buffer buffer, int width) {
        int x = 1;

        // Running status
        if (isRunning) {
            x += buffer.setString(x, 2, "\u25CF", Style.create().green());
            x += buffer.setString(x, 2, " Running", Style.EMPTY);
        } else {
            x += buffer.setString(x, 2, "\u25CF", Style.create().red());
            x += buffer.setString(x, 2, " Stopped", Style.EMPTY);
        }

        x += buffer.setString(x, 2, "  ", Style.EMPTY);

        // Progress indicator
        if (inProgress) {
            x += buffer.setString(x, 2, "[Testing in progress...]", Style.create().yellow());
        }

        x += buffer.setString(x, 2, "  ", Style.EMPTY);

        // Broken only mode
        if (brokenOnly) {
            x += buffer.setString(x, 2, "[Broken Only]", Style.create().magenta());
        }

        // Test counts on right side
        int rx = width - 35;
        rx += buffer.setString(rx, 2, "\u2713 " + passedCount, Style.create().green());
        rx += buffer.setString(rx, 2, "  ", Style.EMPTY);
        rx += buffer.setString(rx, 2, "\u2717 " + failedCount, Style.create().red());
        rx += buffer.setString(rx, 2, "  ", Style.EMPTY);
        rx += buffer.setString(rx, 2, "\u25CB " + skippedCount, Style.create().yellow());
        if (totalTime > 0) {
            rx += buffer.setString(rx, 2, "  ", Style.EMPTY);
            buffer.setString(rx, 2, formatTime(totalTime), Style.create().gray());
        }
    }

    private void renderTabs(Buffer buffer) {
        int x = 1;
        String[] tabNames = { "Passed (" + passedCount + ")", "Failed (" + failedCount + ")",
                "Skipped (" + skippedCount + ")" };

        for (int i = 0; i < tabNames.length; i++) {
            String label = " " + tabNames[i] + " ";
            if (i == currentTab) {
                x += buffer.setString(x, 4, label, Style.create().bold().reversed());
            } else {
                x += buffer.setString(x, 4, label, Style.create().gray());
            }
            x += buffer.setString(x, 4, " ", Style.EMPTY);
        }
    }

    private void renderTestList(Buffer buffer, int panelWidth, int height) {
        List<TestItem> currentList = getCurrentList();

        if (currentList.isEmpty()) {
            buffer.setString(1, 6, "No tests in this category", Style.create().gray());
            return;
        }

        testList.setVisibleRows(height - 10);
        testList.setWidth(panelWidth - 2);
        testList.render(buffer, 7, 1);
    }

    private List<TestItem> getCurrentList() {
        return switch (currentTab) {
            case 0 -> passedTests;
            case 1 -> failedTests;
            case 2 -> skippedTests;
            default -> passedTests;
        };
    }

    private void renderDivider(Buffer buffer, int panelWidth, int height) {
        int col = panelWidth;
        for (int row = 6; row < height - 1; row++) {
            buffer.setString(col, row, "\u2502", Style.create().gray());
        }
    }

    private void renderTestDetails(Buffer buffer, int panelWidth, int width, int height) {
        int startCol = panelWidth + 2;
        int contentWidth = width - panelWidth - 3;

        TestItem selected = testList.getSelectedItem();
        if (selected == null) {
            buffer.setString(startCol, 6, "Select a test to view details", Style.create().gray());
            return;
        }

        int row = 7;

        // Test name
        buffer.setString(startCol, row, selected.displayName, Style.create().cyan().bold());
        row += 2;

        // Class name
        int x = startCol;
        x += buffer.setString(x, row, "Class: ", Style.create().cyan());
        String className = selected.className;
        if (className != null && className.length() > contentWidth - 10) {
            className = "..." + className.substring(className.length() - contentWidth + 13);
        }
        buffer.setString(x, row, className != null ? className : "N/A", Style.EMPTY);
        row++;

        // Time
        x = startCol;
        x += buffer.setString(x, row, "Time: ", Style.create().cyan());
        buffer.setString(x, row, formatTime(selected.time), Style.EMPTY);
        row += 2;

        // Problem/Error if failed
        if (selected.hasProblem && selected.problemMessage != null) {
            buffer.setString(startCol, row, "Error:", Style.create().red().bold());
            row++;

            // Wrap and display error message
            String[] lines = wrapText(selected.problemMessage, contentWidth - 2);
            int maxLines = height - row - 3;
            for (int i = 0; i < Math.min(lines.length, maxLines); i++) {
                buffer.setString(startCol, row, lines[i], Style.create().red());
                row++;
            }
            if (lines.length > maxLines) {
                buffer.setString(startCol, row, "... (" + (lines.length - maxLines) + " more lines)", Style.create().gray());
            }
        }
    }

    private String[] wrapText(String text, int maxWidth) {
        if (text == null || text.isEmpty()) {
            return new String[0];
        }

        List<String> lines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            if (paragraph.length() <= maxWidth) {
                lines.add(paragraph);
            } else {
                int start = 0;
                while (start < paragraph.length()) {
                    int end = Math.min(start + maxWidth, paragraph.length());
                    lines.add(paragraph.substring(start, end));
                    start = end;
                }
            }
        }

        return lines.toArray(new String[0]);
    }

    private void renderFooter(Buffer buffer, int height) {
        if (isRunning) {
            buffer.setString(1, height - 2,
                    "[S] Stop  [A] Run All  [F] Run Failed  [B] Toggle Broken  [Tab] Switch Tab  [R] Refresh  [Esc] Back",
                    Style.create().gray());
        } else {
            buffer.setString(1, height - 2, "[S] Start  [Tab] Switch Tab  [R] Refresh  [Esc] Back",
                    Style.create().gray());
        }
    }

    @Override
    public boolean handleKey(int key, AppContext ctx) {
        if (loading) {
            return true;
        }

        // Let list handle navigation
        if (testList.handleKey(key)) {
            ctx.requestRedraw();
            return true;
        }

        switch (key) {
            case KeyCode.ESCAPE:
                ctx.goBack();
                return true;

            case KeyCode.TAB:
                currentTab = (currentTab + 1) % 3;
                updateTestList();
                ctx.requestRedraw();
                return true;

            case 's':
            case 'S':
                if (isRunning) {
                    stopTesting(ctx);
                } else {
                    startTesting(ctx);
                }
                return true;

            case 'a':
            case 'A':
                if (isRunning) {
                    runAllTests(ctx);
                }
                return true;

            case 'f':
            case 'F':
                if (isRunning) {
                    runFailedTests(ctx);
                }
                return true;

            case 'b':
            case 'B':
                toggleBrokenOnly(ctx);
                return true;

            case 'r':
            case 'R':
                loadData(ctx);
                return true;

            default:
                return false;
        }
    }

    private void invokeAction(String method, String successMessage, AppContext ctx) {
        try {
            DevConsoleManager.invoke("devui-continuous-testing_" + method);
            ctx.setStatus(successMessage);
            loadData(ctx);
        } catch (Exception ex) {
            ctx.setStatus("Failed to " + method + ": " + ex.getMessage());
        }
    }

    private void startTesting(AppContext ctx) {
        invokeAction("start", "Continuous testing started", ctx);
    }

    private void stopTesting(AppContext ctx) {
        invokeAction("stop", "Continuous testing stopped", ctx);
    }

    private void runAllTests(AppContext ctx) {
        invokeAction("runAll", "Running all tests", ctx);
    }

    private void runFailedTests(AppContext ctx) {
        invokeAction("runFailed", "Running failed tests", ctx);
    }

    private void toggleBrokenOnly(AppContext ctx) {
        invokeAction("toggleBrokenOnly", "Broken only: " + (!brokenOnly ? "enabled" : "disabled"), ctx);
    }

    @Override
    public void onResize(int width, int height) {
        int panelWidth = Math.max(40, width / 2);
        testList.setWidth(panelWidth - 2);
        testList.setVisibleRows(height - 10);
    }

    /**
     * Data class for test items.
     */
    private static class TestItem {
        String className;
        String displayName;
        long time;
        boolean hasProblem;
        String problemMessage;
    }
}
