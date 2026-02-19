package io.quarkus.produi.runtime.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.common.annotation.NonBlocking;

/**
 * JSON-RPC service providing configuration information for Prod UI.
 * Configuration values are sanitized to hide sensitive data.
 */
@Singleton
public class ConfigJsonRpcService {

    private static final List<String> SENSITIVE_PATTERNS = List.of(
            "password", "secret", "credential", "key", "token", "auth",
            "apikey", "api-key", "api_key", "private", "jwt");

    /**
     * Get all configuration properties (sanitized).
     */
    @NonBlocking
    public List<Map<String, Object>> getAll() {
        List<Map<String, Object>> configs = new ArrayList<>();
        Config config = ConfigProvider.getConfig();

        for (String name : config.getPropertyNames()) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("name", name);

            String value = config.getOptionalValue(name, String.class).orElse("");
            entry.put("value", sanitizeValue(name, value));
            entry.put("sensitive", isSensitive(name));

            configs.add(entry);
        }

        // Sort by name
        configs.sort(Comparator.comparing(e -> (String) e.get("name")));

        return configs;
    }

    /**
     * Get Quarkus-specific configuration properties (sanitized).
     */
    @NonBlocking
    public List<Map<String, Object>> getQuarkusConfig() {
        List<Map<String, Object>> configs = new ArrayList<>();
        Config config = ConfigProvider.getConfig();

        for (String name : config.getPropertyNames()) {
            if (name.startsWith("quarkus.")) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("name", name);

                String value = config.getOptionalValue(name, String.class).orElse("");
                entry.put("value", sanitizeValue(name, value));
                entry.put("sensitive", isSensitive(name));

                configs.add(entry);
            }
        }

        // Sort by name
        configs.sort(Comparator.comparing(e -> (String) e.get("name")));

        return configs;
    }

    /**
     * Get a specific configuration value (sanitized).
     */
    @NonBlocking
    public Map<String, Object> get(String name) {
        Map<String, Object> result = new LinkedHashMap<>();
        Config config = ConfigProvider.getConfig();

        result.put("name", name);

        String value = config.getOptionalValue(name, String.class).orElse(null);
        if (value != null) {
            result.put("value", sanitizeValue(name, value));
            result.put("sensitive", isSensitive(name));
            result.put("found", true);
        } else {
            result.put("found", false);
        }

        return result;
    }

    private String sanitizeValue(String name, String value) {
        if (isSensitive(name)) {
            return "********";
        }
        return value;
    }

    private boolean isSensitive(String name) {
        String lowerName = name.toLowerCase();
        for (String pattern : SENSITIVE_PATTERNS) {
            if (lowerName.contains(pattern)) {
                return true;
            }
        }
        return false;
    }
}
