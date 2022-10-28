package io.quarkus.devui.deployment.spi.page;

import java.util.HashMap;
import java.util.Map;

/**
 * Define a "page" in Dev UI. This page will be rendered using a provided Web Component
 *
 * Data can be provided at build time or runtime.
 * For build time:
 */
public class WebComponentPage extends AbstractPage {

    private final String webComponent; // This is name of the component, relative to the namespace

    private final Map<String, Object> buildTimeData; // Optional (if data is known at build-time)

    private WebComponentPage(Builder builder) {
        super(builder.iconName,
                builder.displayName,
                builder.label);//,
        //                builder.showLinkOnCard,
        //                builder.showLinkOnSideMenu,
        //                builder.showLinkOnNavigationBar);

        this.webComponent = builder.webComponent;
        this.buildTimeData = builder.buildTimeData; // Optional ?
    }

    public String getWebComponent() {
        return webComponent;
    }

    public boolean hasWebComponentLink() {
        return this.webComponent != null;
    }

    public boolean hasBuildTimeData() {
        return this.buildTimeData != null;
    }

    public Map<String, Object> getBuildTimeData() {
        return this.buildTimeData;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private final String namespace;

        private String webComponent;
        private Map<String, Object> buildTimeData = new HashMap<>(); // Method name and response data

        public Builder(String namespace) {
            this.namespace = namespace;
        }

        public Builder webComponent(String webComponent) {
            if (webComponent.isEmpty() || !webComponent.endsWith(DOT_JS)) {
                throw new RuntimeException("Invalid component [" + webComponent + "] - expecting a .js link");
            }

            this.webComponent = webComponent;
            return this;
        }

        public <T> Builder buildTimeData(String methodName, T methodResponse) {
            this.buildTimeData.put(methodName, methodResponse);
            return this;
        }

        public WebComponentPage build() {
            if (webComponent == null && displayName == null) {
                throw new RuntimeException(
                        "Not enough information to create a link. Set at least one of path, component or displayName");
            }

            if (webComponent == null) {
                // create default component file
                this.webComponent = "qwc" + DASH + this.namespace.toLowerCase().replaceAll(" ", DASH) + DASH
                        + this.displayName.toLowerCase().replaceAll(" ", DASH) + DOT_JS;
            }
            if (this.displayName == null && this.webComponent != null) {
                // create default display name
                try {
                    String n = this.webComponent.substring(this.webComponent.indexOf("-") + 1);// Remove the component prefix (qwc-)
                    n = n.substring(n.indexOf(DASH) + 1);// Remove the component prefix (namespace-)
                    n = n.substring(0, n.lastIndexOf(".")); // Remove the file extension (.js)
                    n = n.replace(DASH, " "); // replace dash with space
                    this.displayName = n.substring(0, 1).toUpperCase() + n.substring(1); // Capitalize first letter
                } catch (IndexOutOfBoundsException ioobe) {
                    throw new RuntimeException(
                            "Wrong naming used for component JavaScript file. Use [qwc-yournamespace-yourcomponent]", ioobe);
                }
            }

            return new WebComponentPage(this);
        }
    }

    private static final String DASH = "-";
    private static final String DOT = ".";
    private static final String JS = "js";
    private static final String DOT_JS = DOT + JS;

}
