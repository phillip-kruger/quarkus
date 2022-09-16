package io.quarkus.devui.deployment.spi;

/**
 * Card Link on an extension card in Dev UI
 */
public class CardLink {
    private final String iconName;
    private final String displayName;
    private final String label;
    private final String component;
    private final String path;

    private CardLink(Builder builder) {
        this.iconName = builder.iconName;
        this.displayName = builder.displayName;
        this.label = builder.label;
        this.path = builder.path;
        this.component = builder.component;
    }

    public String getIconName() {
        return iconName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public String getComponent() {
        return component;
    }

    public boolean hasComponentLink() {
        return this.component != null;
    }

    public static class Builder {
        private final String namespace;
        private String iconName;
        private String displayName;
        private String label;
        private String component;
        private String path;

        public Builder(String namespace) {
            this.namespace = namespace;
        }

        public Builder iconName(String iconName) {
            this.iconName = iconName;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder component(String component) {
            if (component.isEmpty() || !component.endsWith(DOT_JS)) {
                throw new RuntimeException("Invalid component [" + component + "] - expecting a .js link");
            }
            if (this.path != null) {
                throw new RuntimeException(
                        "Invalid use of CardLink. Set either path or component, not both. Path for external links, component for internal web component.");
            }

            this.component = component;
            return this;
        }

        public Builder path(String path) {
            if (path.isEmpty()) {
                throw new RuntimeException("Invalid path, can not be empty");
            }
            if (this.component != null) {
                throw new RuntimeException(
                        "Invalid use of CardLink. Set either path or component, not both. Path for external links, component for internal web component.");
            }

            this.path = path;
            return this;
        }

        public CardLink build() {
            if (path == null && component == null && displayName == null) {
                throw new RuntimeException(
                        "Not enough information to create a card link. Set at least one of path, component or displayName");
            }

            if (path == null && component == null) {
                // create default component file
                this.component = "qwc" + DASH + this.namespace.toLowerCase().replaceAll(" ", DASH) + DASH
                        + this.displayName.toLowerCase().replaceAll(" ", DASH) + DOT_JS;
            }
            if (this.displayName == null && this.component != null) {
                // create default display name
                try {
                    String n = this.component.substring(this.component.indexOf("-") + 1);// Remove the component prefix (qwc-)
                    n = n.substring(n.indexOf(DASH) + 1);// Remove the component prefix (namespace-)
                    n = n.substring(0, n.lastIndexOf(".")); // Remove the file extension (.js)
                    n = n.replace(DASH, " "); // replace dash with space
                    this.displayName = n.substring(0, 1).toUpperCase() + n.substring(1); // Capitalize first letter
                } catch (IndexOutOfBoundsException ioobe) {
                    throw new InvalidNamingException(
                            "Wrong naming used for component JavaScript file. Use [qwc-yournamespace-yourcomponent]", ioobe);
                }
            }

            if (this.iconName == null) {
                this.iconName = "font-awesome-solid:arrow-right";
            }

            CardLink cardLink = new CardLink(this);

            return cardLink;
        }
    }

    private static final String DASH = "-";
    private static final String DOT = ".";
    private static final String JS = "js";
    private static final String DOT_JS = DOT + JS;
}
