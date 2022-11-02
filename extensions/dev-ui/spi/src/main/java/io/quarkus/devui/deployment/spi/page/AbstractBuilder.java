package io.quarkus.devui.deployment.spi.page;

public abstract class AbstractBuilder<T> {
    protected static final String EMPTY = "";
    protected static final String SPACE = " ";
    protected static final String DASH = "-";
    protected static final String DOT = ".";
    protected static final String JS = "js";
    protected static final String QWC_DASH = "qwc-";
    protected static final String DOT_JS = DOT + JS;

    protected String icon = "font-awesome-solid:arrow-right";
    protected String title = null;
    protected String label = null;
    protected String componentName;
    protected String componentLink;
    protected boolean embed = true; // default

    @SuppressWarnings("unchecked")
    public T icon(String icon) {
        this.icon = icon;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T title(String title) {
        this.title = title;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T label(String label) {
        this.label = label;
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
                this.componentLink = QWC_DASH + this.title.toLowerCase().replaceAll(SPACE, DASH) + DOT_JS;
            }
        }

        // Guess the component name from the componentlink or title
        if (this.componentName == null) {
            this.componentName = this.componentLink.substring(0, this.componentLink.lastIndexOf(DOT)); // Remove the file extension (.js)
        }

        // Guess the title
        if (this.title == null) {
            String n = this.componentName.replaceAll(QWC_DASH, EMPTY); // Remove the qwc-
            n = n.substring(n.indexOf(DASH) + 1); // Remove the namespace-
            n = n.replaceAll(DASH, SPACE);
            this.title = n.substring(0, 1).toUpperCase() + n.substring(1); // Capitalize first letter
        }

        return new Page(icon, title, label, componentName, componentLink, embed);
    }
}
