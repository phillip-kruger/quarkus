package io.quarkus.produi.runtime.overview;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import org.eclipse.microprofile.config.ConfigProvider;

import io.smallrye.common.annotation.NonBlocking;

/**
 * JSON-RPC service providing application overview information for Prod UI.
 * All methods are read-only and safe for production use.
 */
@Singleton
public class OverviewJsonRpcService {

    @Inject
    io.quarkus.runtime.ApplicationConfig applicationConfig;

    /**
     * Get basic application information.
     */
    @NonBlocking
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new LinkedHashMap<>();

        // Application info
        info.put("name", applicationConfig.name().orElse("Unknown"));
        info.put("version", applicationConfig.version().orElse("Unknown"));

        // Quarkus version
        String quarkusVersion = ConfigProvider.getConfig()
                .getOptionalValue("quarkus.application.quarkus-version", String.class)
                .orElse(getQuarkusVersion());
        info.put("quarkusVersion", quarkusVersion);

        // JVM info
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        info.put("jvmName", runtimeBean.getVmName());
        info.put("jvmVersion", runtimeBean.getVmVersion());
        info.put("jvmVendor", runtimeBean.getVmVendor());

        // Uptime
        long uptimeMs = runtimeBean.getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);
        info.put("uptimeMillis", uptimeMs);
        info.put("uptimeFormatted", formatDuration(uptime));

        // Start time
        info.put("startTime", runtimeBean.getStartTime());

        return info;
    }

    /**
     * Get memory statistics.
     */
    @NonBlocking
    public Map<String, Object> getMemory() {
        Map<String, Object> memory = new LinkedHashMap<>();

        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        memory.put("totalBytes", totalMemory);
        memory.put("freeBytes", freeMemory);
        memory.put("maxBytes", maxMemory);
        memory.put("usedBytes", usedMemory);
        memory.put("usedPercentage", Math.round((double) usedMemory / maxMemory * 100));

        memory.put("totalFormatted", formatBytes(totalMemory));
        memory.put("freeFormatted", formatBytes(freeMemory));
        memory.put("maxFormatted", formatBytes(maxMemory));
        memory.put("usedFormatted", formatBytes(usedMemory));

        return memory;
    }

    /**
     * Get thread statistics.
     */
    @NonBlocking
    public Map<String, Object> getThreads() {
        Map<String, Object> threads = new LinkedHashMap<>();

        java.lang.management.ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

        threads.put("threadCount", threadBean.getThreadCount());
        threads.put("peakThreadCount", threadBean.getPeakThreadCount());
        threads.put("daemonThreadCount", threadBean.getDaemonThreadCount());
        threads.put("totalStartedThreadCount", threadBean.getTotalStartedThreadCount());

        return threads;
    }

    /**
     * Get system properties (sanitized to hide sensitive values).
     */
    @NonBlocking
    public Map<String, String> getSystemProperties() {
        Map<String, String> props = new LinkedHashMap<>();

        System.getProperties().forEach((key, value) -> {
            String keyStr = key.toString();
            String valueStr = value.toString();

            // Sanitize sensitive properties
            if (isSensitive(keyStr)) {
                valueStr = "********";
            }

            props.put(keyStr, valueStr);
        });

        return props;
    }

    private String getQuarkusVersion() {
        Package pkg = io.quarkus.runtime.Quarkus.class.getPackage();
        if (pkg != null && pkg.getImplementationVersion() != null) {
            return pkg.getImplementationVersion();
        }
        return "Unknown";
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;

        if (days > 0) {
            return String.format("%dd %dh %dm %ds", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds);
        } else {
            return String.format("%ds", seconds);
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        double kb = bytes / 1024.0;
        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }
        double mb = kb / 1024.0;
        if (mb < 1024) {
            return String.format("%.1f MB", mb);
        }
        double gb = mb / 1024.0;
        return String.format("%.2f GB", gb);
    }

    private boolean isSensitive(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("password")
                || lowerKey.contains("secret")
                || lowerKey.contains("credential")
                || lowerKey.contains("key")
                || lowerKey.contains("token")
                || lowerKey.contains("auth");
    }
}
