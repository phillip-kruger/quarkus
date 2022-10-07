package io.quarkus.devui.runtime.jsonrpc.handler;

public class AppInfo {
    private String name;
    private String version;

    public AppInfo() {
    }

    public AppInfo(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setversion(String version) {
        this.version = version;
    }

}
