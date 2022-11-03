package io.quarkus.devui.deployment.spi.page;

import java.util.Map;

/**
 * Define a page in Dev UI.
 * This not a full web page, but rather the section in the middle where extensions can display data.
 * All pages (fragments) are rendered using Web components, but different builders exist to make it easy to define a page
 *
 * Navigation to this page is also defined here.
 */
public class Page {
    private final String icon; // Any font awesome icon
    private final String title; // This is the display name and link title for the page
    private final String label; // This is optional extra info that might be displayed next to the link

    private final String componentName; // This is name of the component
    private final String componentLink; // This is a link to the component, excluding namespace
    private final Map<String, String> metadata; // Key value Metadata

    private boolean embed = true; // default

    private String namespace = null; // The namespace can be the extension path or, if internal, qwc

    protected Page(String icon,
            String title,
            String label,
            String componentName,
            String componentLink,
            Map<String, String> metadata,
            boolean embed,
            String namespace) {

        this.icon = icon;
        this.title = title;
        this.label = label;
        this.componentName = componentName;
        this.componentLink = componentLink;
        this.metadata = metadata;
        this.embed = embed;
        this.namespace = namespace;
    }

    public String getId() {
        String id = this.title.toLowerCase().replaceAll(SPACE, DASH);
        if (this.namespace != null) {
            id = this.namespace + SLASH + id;
        }
        return id;
    }

    public String getComponentRef() {
        String group = "qwc";
        if (this.namespace != null) {
            group = this.namespace;
        }
        return DOT + SLASH + DOT + DOT + SLASH + group + SLASH + this.componentLink;
    }

    public boolean hasNamespace() {
        return this.namespace != null;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public void setExtension(String extension) {
        this.metadata.put("extensionName", extension);
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

    public String getLabel() {
        return label;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getComponentLink() {
        return componentLink;
    }

    public boolean isEmbed() {
        return embed;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return "Page {\n\ticon=" + icon
                + ", \n\ttitle=" + title
                + ", \n\tlabel=" + label
                + ", \n\tcomponentName=" + componentName
                + ", \n\tcomponentLink=" + componentLink
                + ", \n\tembed=" + embed + "\n}";
    }

    public static WebComponentPageBuilder webComponentPageBuilder() {
        return new WebComponentPageBuilder();
    }

    public static ExternalPageBuilder externalPageBuilder(String name) {
        return new ExternalPageBuilder(name);
    }

    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final String SLASH = "/";
    private static final String DOT = ".";

}
