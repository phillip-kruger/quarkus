package io.quarkus.devui.runtime.jsonrpc.handler;

public class MenuItem {

    private String webcomponent;
    private String icon;
    private boolean defaultSelection;

    public MenuItem() {
    }

    public MenuItem(String webcomponent, String icon) {
        this(webcomponent, icon, false);

    }

    public MenuItem(String webcomponent, String icon, boolean defaultSelection) {
        this.webcomponent = webcomponent;
        this.icon = icon;
        this.defaultSelection = defaultSelection;
    }

    public String getWebcomponent() {
        return webcomponent;
    }

    public void setWebcomponent(String webcomponent) {
        this.webcomponent = webcomponent;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isDefaultSelection() {
        return defaultSelection;
    }

    public void setDefaultSelection(boolean defaultSelection) {
        this.defaultSelection = defaultSelection;
    }
}
