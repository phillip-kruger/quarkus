package io.quarkus.devshell.runtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.jboss.logging.Logger;

import io.quarkus.devshell.runtime.navigation.PageNavigator;
import io.quarkus.devshell.runtime.pages.ShellPage;

/**
 * Main terminal UI application for Quarkus Dev Shell.
 *
 * This is a minimal text-based implementation that can be enhanced
 * with full Tamboui TUI support later.
 */
public class DevShellApp implements Runnable {

    private static final Logger LOG = Logger.getLogger(DevShellApp.class);

    private final DevShellRouter router;
    private final PageNavigator navigator;
    private volatile boolean running = false;
    private volatile boolean shouldQuit = false;

    public DevShellApp(DevShellRouter router, PageNavigator navigator) {
        this.router = router;
        this.navigator = navigator;
        this.navigator.setRouter(router);
    }

    @Override
    public void run() {
        running = true;
        shouldQuit = false;

        try {
            // Initialize the first page
            ShellPage currentPage = navigator.getCurrentPage();
            if (currentPage != null) {
                currentPage.onEnter(router);
            }

            // Simple text-based UI loop
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            printHeader();
            printMenu();
            printMethods();
            printFooter();

            while (!shouldQuit) {
                System.out.print("\ndev-shell> ");
                String input = reader.readLine();

                if (input == null || input.equalsIgnoreCase("q") || input.equalsIgnoreCase("quit")) {
                    shouldQuit = true;
                } else if (input.equalsIgnoreCase("h") || input.equalsIgnoreCase("help")) {
                    printHelp();
                } else if (input.equalsIgnoreCase("m") || input.equalsIgnoreCase("methods")) {
                    printMethods();
                } else if (input.equalsIgnoreCase("r") || input.equalsIgnoreCase("refresh")) {
                    if (currentPage != null) {
                        currentPage.onEnter(router);
                    }
                    printMethods();
                } else if (!input.isEmpty()) {
                    System.out.println("Unknown command: " + input + " (type 'h' for help)");
                }
            }

        } catch (IOException e) {
            LOG.error("Dev Shell error", e);
        } finally {
            running = false;
            System.out.println("\nExiting Dev Shell...\n");
        }
    }

    private void printHeader() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                     QUARKUS DEV SHELL                                    ║");
        System.out.println("║                Terminal-based Dev Mode Access                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════════════╝");
    }

    private void printMenu() {
        System.out.println();
        System.out.println("Available commands:");
        System.out.println("  [m] methods  - List available JSON-RPC methods");
        System.out.println("  [r] refresh  - Refresh method list");
        System.out.println("  [h] help     - Show this help");
        System.out.println("  [q] quit     - Exit Dev Shell");
    }

    private void printHelp() {
        System.out.println();
        System.out.println("Dev Shell Help");
        System.out.println("==============");
        System.out.println();
        System.out.println("Dev Shell provides terminal access to Quarkus Dev Mode features.");
        System.out.println("It uses the same JSON-RPC backend as the browser-based Dev UI.");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  methods (m)  - Display all available JSON-RPC methods");
        System.out.println("  refresh (r)  - Refresh the method list");
        System.out.println("  help (h)     - Show this help message");
        System.out.println("  quit (q)     - Exit Dev Shell and return to normal console");
        System.out.println();
    }

    private void printMethods() {
        System.out.println();
        System.out.println("Available JSON-RPC Methods");
        System.out.println("==========================");
        System.out.println();

        Map<String, ?> runtimeMethods = router.getRuntimeMethods();
        Map<String, ?> deploymentMethods = router.getDeploymentMethods();

        int count = 0;

        if (runtimeMethods != null && !runtimeMethods.isEmpty()) {
            System.out.println("Runtime Methods:");
            for (String methodName : runtimeMethods.keySet()) {
                System.out.println("  - " + methodName);
                count++;
            }
            System.out.println();
        }

        if (deploymentMethods != null && !deploymentMethods.isEmpty()) {
            System.out.println("Deployment Methods:");
            for (String methodName : deploymentMethods.keySet()) {
                System.out.println("  - " + methodName);
                count++;
            }
            System.out.println();
        }

        System.out.println("Total: " + count + " methods available");
    }

    private void printFooter() {
        System.out.println();
        System.out.println("Type 'h' for help, 'q' to quit");
    }

    /**
     * Called when the Quarkus application is hot-reloaded.
     */
    public void onHotReload() {
        System.out.println("\n[Dev Shell] Application reloaded - type 'r' to refresh");
        navigator.onHotReload();
    }

    /**
     * Stop the UI and return to normal console.
     */
    public void stop() {
        shouldQuit = true;
    }

    /**
     * Check if the UI is currently running.
     */
    public boolean isRunning() {
        return running;
    }
}
