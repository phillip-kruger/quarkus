package io.quarkus.devui.runtime.service.extension;

/**
 * Card Link on an extension card in Dev UI
 */
public class Link {

    private String iconName;
    private String displayName;
    private String label;
    private String component;
    private String path;

    public Link() {
    }

    public Link(String iconName, String displayName, String label, String component, String path) {
        this.iconName = iconName;
        this.displayName = displayName;
        this.label = label;
        this.component = component;
        this.path = path;
    }

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
