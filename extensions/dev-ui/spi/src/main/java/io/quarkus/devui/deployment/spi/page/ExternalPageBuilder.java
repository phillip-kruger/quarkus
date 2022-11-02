package io.quarkus.devui.deployment.spi.page;

public class ExternalPageBuilder extends AbstractBuilder<ExternalPageBuilder> {
    private static final String QWC_EXTERNAL_PAGE_JS = "qwc-external-page.js";

    // TODO: Content type ?

    protected ExternalPageBuilder(String title) {
        super();
        super.title = title;
    }

    public ExternalPageBuilder url(String url) {
        if (url.isEmpty()) {
            throw new RuntimeException("Invalid external URL, can not be empty");
        }

        super.componentLink = QWC_EXTERNAL_PAGE_JS + "?externalUrl=" + "here the url encoded";
        return this;
    }

    public ExternalPageBuilder doNotEmbed() {
        super.embed = false;
        return this;
    }
}