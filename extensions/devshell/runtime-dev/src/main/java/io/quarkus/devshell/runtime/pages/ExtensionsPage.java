package io.quarkus.devshell.runtime.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.quarkus.devshell.runtime.DevShellRouter;

/**
 * Extensions page for Dev Shell.
 * Shows the list of active Quarkus extensions and available JSON-RPC methods.
 */
public class ExtensionsPage implements ShellPage {

    private List<MethodInfo> methods = new ArrayList<>();
    private boolean loading = true;
    private String errorMessage = null;

    @Override
    public String getTitle() {
        return "Extensions & Methods";
    }

    @Override
    public char getShortcutKey() {
        return '1';
    }

    @Override
    public void onEnter(DevShellRouter router) {
        loading = true;
        errorMessage = null;
        methods.clear();

        // Collect all available JSON-RPC methods
        try {
            Map<String, ?> runtimeMethods = router.getRuntimeMethods();
            Map<String, ?> deploymentMethods = router.getDeploymentMethods();

            if (runtimeMethods != null) {
                for (String methodName : runtimeMethods.keySet()) {
                    methods.add(new MethodInfo(methodName, "runtime"));
                }
            }

            if (deploymentMethods != null) {
                for (String methodName : deploymentMethods.keySet()) {
                    methods.add(new MethodInfo(methodName, "deployment"));
                }
            }

            // Sort by name
            methods.sort((a, b) -> a.name.compareTo(b.name));

            loading = false;
        } catch (Exception e) {
            loading = false;
            errorMessage = "Failed to load methods: " + e.getMessage();
        }
    }

    @Override
    public void onLeave() {
        // Nothing to clean up
    }

    @Override
    public void print() {
        System.out.println();
        System.out.println("=== " + getTitle() + " ===");
        System.out.println();

        if (loading) {
            System.out.println("Loading...");
            return;
        }

        if (errorMessage != null) {
            System.out.println("ERROR: " + errorMessage);
            return;
        }

        if (methods.isEmpty()) {
            System.out.println("No JSON-RPC methods available.");
            return;
        }

        System.out.printf("%-50s %s%n", "Method Name", "Type");
        System.out.println("-".repeat(65));

        for (MethodInfo method : methods) {
            System.out.printf("%-50s %s%n", truncate(method.name, 48), method.type);
        }

        System.out.println();
        System.out.println("Total: " + methods.size() + " methods");
    }

    @Override
    public boolean handleCommand(String command) {
        // This page doesn't have special commands yet
        return false;
    }

    private String truncate(String text, int maxLen) {
        if (text.length() <= maxLen) {
            return text;
        }
        return text.substring(0, maxLen - 3) + "...";
    }

    private record MethodInfo(String name, String type) {
    }
}
