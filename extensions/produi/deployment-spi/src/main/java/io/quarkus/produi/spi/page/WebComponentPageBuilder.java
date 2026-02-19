package io.quarkus.produi.spi.page;

/**
 * Builder for Web Component pages in Prod UI.
 */
public class WebComponentPageBuilder extends PageBuilder<WebComponentPageBuilder> {

    protected static final String DOT_JS = ".js";

    protected WebComponentPageBuilder() {
        super();
    }

    public WebComponentPageBuilder componentName(String componentName) {
        if (componentName == null || componentName.isEmpty()) {
            throw new RuntimeException("Invalid component [" + componentName + "]");
        }

        super.componentName = componentName;
        return this;
    }

    public WebComponentPageBuilder componentLink(String componentLink) {
        if (componentLink == null || componentLink.isEmpty() || !componentLink.endsWith(DOT_JS)) {
            throw new RuntimeException(
                    "Invalid component link [" + componentLink + "] - Expecting a link that ends with .js");
        }

        super.componentLink = componentLink;
        return this;
    }
}
