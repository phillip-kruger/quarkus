package io.quarkus.devui.runtime.jsonrpc.handler;

public class VersionInfo {
    private String quarkusVersion;
    private String applicationName;
    private String applicationVersion;

    public VersionInfo() {
    }

    public VersionInfo(String quarkusVersion, String applicationName, String applicationVersion) {
        this.quarkusVersion = quarkusVersion;
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    public String getQuarkusVersion() {
        return quarkusVersion;
    }

    public void setQuarkusVersion(String quarkusVersion) {
        this.quarkusVersion = quarkusVersion;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(String applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    @Override
    public String toString() {
        return "VersionInfo [applicationName=" + applicationName + ", applicationVersion=" + applicationVersion
                + ", quarkusVersion=" + quarkusVersion + "]";
    }
}
