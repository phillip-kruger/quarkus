package io.quarkus.devui.deployment.spi.page;

// TODO: Add content type
public class ExternalPageBuilder extends AbstractBuilder<ExternalPageBuilder> {
    private static final String QWC_EXTERNAL_PAGE_JS = "qwc-external-page.js";
    private static final String EXTERNAL_URL = "externalUrl";

    protected ExternalPageBuilder(String title) {
        super();
        super.title = title;
    }

    public ExternalPageBuilder url(String url) {
        if (url.isEmpty()) {
            throw new RuntimeException("Invalid external URL, can not be empty");
        }
        super.componentLink = QWC_EXTERNAL_PAGE_JS;
        super.metadata.put(EXTERNAL_URL, url);
        super.namespace = "qwc"; // As external page runs on "internal" namespace
        return this;
    }

    public ExternalPageBuilder doNotEmbed() {
        super.embed = false;
        return this;
    }
}