package io.quarkus.devui.deployment.spi.page;

public class RawDataPageBuilder extends AbstractBuilder<RawDataPageBuilder> {
    private static final String QWC_DATA_RAW_PAGE_JS = "qwc-data-raw-page.js";
    private static final String BUILD_TIME_DATA_KEY = "buildTimeDataKey";

    protected RawDataPageBuilder(String title) {
        super();
        super.title = title;
        super.componentLink = QWC_DATA_RAW_PAGE_JS;
        super.internalComponent = true;// As external page runs on "internal" namespace
    }

    public RawDataPageBuilder buildTimeDataKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("Invalid build time data key, can not be empty");
        }
        super.metadata.put(BUILD_TIME_DATA_KEY, key);
        return this;
    }
}