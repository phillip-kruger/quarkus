package io.quarkus.devui.deployment.spi.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TableDataPageBuilder extends AbstractBuilder<TableDataPageBuilder> {
    private static final String QWC_DATA_TABLE_PAGE_JS = "qwc-data-table-page.js";
    private static final String BUILD_TIME_DATA_KEY = "buildTimeDataKey";
    private static final String COLS = "cols";
    private static final String COMMA = ",";

    protected TableDataPageBuilder(String title) {
        super();
        super.title = title;
        super.componentLink = QWC_DATA_TABLE_PAGE_JS;
        super.internalComponent = true;// As external page runs on "internal" namespace
    }

    public TableDataPageBuilder buildTimeDataKey(String key) {
        if (key == null || key.isEmpty()) {
            throw new RuntimeException("Invalid build time data key, can not be empty");
        }
        super.metadata.put(BUILD_TIME_DATA_KEY, key);
        return this;
    }

    public TableDataPageBuilder showColumn(String path) {
        List<String> headerPaths = new ArrayList<>();
        if (super.metadata.containsKey(COLS)) {
            String csl = super.metadata.get(COLS);
            headerPaths = new ArrayList<>(Arrays.asList(csl.split(COMMA)));
        }
        headerPaths.add(path);
        String csl = String.join(COMMA, headerPaths);
        super.metadata(COLS, csl);
        return this;
    }
}