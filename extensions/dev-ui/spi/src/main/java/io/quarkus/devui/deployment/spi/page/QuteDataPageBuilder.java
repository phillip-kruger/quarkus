package io.quarkus.devui.deployment.spi.page;

import java.util.HashMap;
import java.util.Map;

public class QuteDataPageBuilder extends PageBuilder<QuteDataPageBuilder> {
    private static final String DOT_HTML = ".html";
    private static final String QWC_DATA_QUTE_PAGE_JS = "qwc-data-qute-page.js";
    private final Map<String, Object> data;
    private String templateLink;

    protected QuteDataPageBuilder(String title) {
        super();
        super.title = title;
        super.internalComponent = true;// As external page runs on "internal" namespace
        super.componentLink = QWC_DATA_QUTE_PAGE_JS;
        this.data = new HashMap<>();
    }

    public QuteDataPageBuilder data(Map<String, Object> data) {
        this.data.putAll(data);
        return this;
    }

    public QuteDataPageBuilder data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    public QuteDataPageBuilder templateLink(String templateLink) {
        if (templateLink == null || templateLink.isEmpty() || !templateLink.endsWith(DOT_HTML)) {
            throw new RuntimeException(
                    "Invalid template link [" + templateLink + "] - Expeting a link that ends with .html");
        }

        this.templateLink = templateLink;
        return this;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getTemplateLink() {
        return templateLink;
    }

}