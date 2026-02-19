package io.quarkus.produi.spi.page;

import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.Logger;

/**
 * Base builder for Prod UI pages.
 */
public abstract class PageBuilder<T> {
    private static final Logger log = Logger.getLogger(PageBuilder.class);

    protected static final String EMPTY = "";
    protected static final String SPACE = " ";
    protected static final String DASH = "-";
    protected static final String DOT = ".";
    protected static final String JS = "js";
    protected static final String PUI_DASH = "pui-";
    protected static final String DOT_JS = DOT + JS;

    protected String icon = null;
    protected String title = null;
    protected String componentName;
    protected String componentLink;
    protected Map<String, String> metadata = new HashMap<>();
    protected boolean includeInMenu = true;
    protected boolean internalComponent = false;
    protected String namespace = null;
    protected String extensionId = null;

    @SuppressWarnings("unchecked")
    public T icon(String icon) {
        if (this.icon == null) {
            this.icon = icon;
        } else {
            log.warn("Icon already set to [" + this.icon + "], ignoring [" + icon + "]");
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T title(String title) {
        this.title = title;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T metadata(String key, String value) {
        this.metadata.put(key, value);
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T namespace(String namespace) {
        if (this.namespace == null) {
            this.namespace = namespace;
        }
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T internal() {
        this.internalComponent = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T excludeFromMenu() {
        this.includeInMenu = false;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T extension(String extension) {
        this.extensionId = extension.toLowerCase().replaceAll(SPACE, DASH);
        this.metadata.put("extensionName", extension);
        this.metadata.put("extensionId", extensionId);
        return (T) this;
    }

    public Page build() {
        if (this.componentName == null && this.componentLink == null && this.title == null) {
            throw new RuntimeException(
                    "Not enough information to build the page. Set at least one of componentLink and/or componentName and/or title");
        }

        // Guess the component link from the component name or title
        if (this.componentLink == null) {
            if (this.componentName != null) {
                this.componentLink = this.componentName + DOT_JS;
            } else if (this.title != null) {
                this.componentLink = PUI_DASH + this.title.toLowerCase().replaceAll(SPACE, DASH) + DOT_JS;
            }
        }

        // Guess the component name from the component link or title
        if (this.componentName == null) {
            this.componentName = this.componentLink.substring(0, this.componentLink.lastIndexOf(DOT));
        }

        // Guess the title
        if (this.title == null) {
            String n = this.componentName.replaceAll(PUI_DASH, EMPTY);
            n = n.substring(n.indexOf(DASH) + 1);
            n = n.replaceAll(DASH, SPACE);
            this.title = n.substring(0, 1).toUpperCase() + n.substring(1);
        }

        return new Page(icon,
                title,
                componentName,
                componentLink,
                metadata,
                includeInMenu,
                internalComponent,
                namespace,
                extensionId);
    }
}
