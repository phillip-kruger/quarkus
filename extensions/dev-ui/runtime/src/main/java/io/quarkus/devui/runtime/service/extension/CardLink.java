package io.quarkus.devui.runtime.service.extension;

/**
 * Card Link on an extension card in Dev UI
 */
@Deprecated
public class CardLink {

    private String iconName;
    private String displayName;
    private String label;
    private String component;
    private String componentRef;
    private String path;
    boolean addPathParam = false;

    public CardLink() {
    }

    public CardLink(String iconName, String displayName, String label, String component, String componentRef, String path) {
        this(iconName, displayName, label, component, componentRef, path, false);
    }

    public CardLink(String iconName, String displayName, String label, String component, String componentRef, String path,
            boolean addPathParam) {
        this.iconName = iconName;
        this.displayName = displayName;
        this.label = label;
        this.component = component;
        this.componentRef = componentRef;
        this.path = path;
        this.addPathParam = addPathParam;
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

    public String getComponentRef() {
        return this.componentRef;
    }

    public void setComponentRef(String componentRef) {
        this.componentRef = componentRef;
    }

    public boolean isAddPathParam() {
        return this.addPathParam;
    }

    public void setAddPathParam(boolean addPathParam) {
        this.addPathParam = addPathParam;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
