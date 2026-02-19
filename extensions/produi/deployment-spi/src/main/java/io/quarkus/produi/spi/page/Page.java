package io.quarkus.produi.spi.page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Define a page in Prod UI.
 * Pages are rendered using Web components.
 * Navigation to this page is also defined here.
 */
public class Page {
    private final String icon;
    private final String title;
    private final String componentName;
    private final String componentLink;
    private final Map<String, String> metadata;
    private final boolean includeInMenu;
    private final boolean internalComponent;

    private String namespace = null;
    private String extensionId = null;

    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final String SLASH = "/";
    private static final String DOT = ".";

    protected Page(String icon,
            String title,
            String componentName,
            String componentLink,
            Map<String, String> metadata,
            boolean includeInMenu,
            boolean internalComponent,
            String namespace,
            String extensionId) {

        this.icon = icon != null ? icon : "font-awesome-solid:arrow-right";
        this.title = title;
        this.componentName = componentName;
        this.componentLink = componentLink;
        this.metadata = metadata;
        this.includeInMenu = includeInMenu;
        this.internalComponent = internalComponent;
        this.namespace = namespace;
        this.extensionId = extensionId;
    }

    public String getId() {
        String id = this.title.toLowerCase().replaceAll(SPACE, DASH);
        try {
            id = URLEncoder.encode(id, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }

        if (!this.isInternal() && this.namespace != null) {
            id = this.namespace.toLowerCase() + SLASH + id;
        } else if (this.isInternal() && this.namespace != null) {
            String d = "produi-" + id;
            if (d.equals(this.namespace)) {
                return id;
            } else {
                int i = this.namespace.indexOf(DASH) + 1;
                String stripProdui = this.namespace.substring(i);
                return stripProdui + DASH + id;
            }
        }
        return id;
    }

    public String getComponentRef() {
        if (internalComponent) {
            return DOT + SLASH + DOT + DOT + SLASH + "pui" + SLASH + this.componentLink;
        } else if (this.namespace != null) {
            return DOT + SLASH + DOT + DOT + SLASH + this.namespace + SLASH + this.componentLink;
        }
        throw new RuntimeException("Could not find component reference");
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getIcon() {
        return icon;
    }

    public String getTitle() {
        return title;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public boolean isIncludeInMenu() {
        return includeInMenu;
    }

    public boolean isInternal() {
        return this.internalComponent && this.extensionId == null;
    }

    public String getExtensionId() {
        return extensionId;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    /**
     * Create a builder for a Web Component page
     */
    public static WebComponentPageBuilder webComponentPageBuilder() {
        return new WebComponentPageBuilder();
    }

    @Override
    public String toString() {
        return "Page{" +
                "icon='" + icon + '\'' +
                ", title='" + title + '\'' +
                ", componentName='" + componentName + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}
