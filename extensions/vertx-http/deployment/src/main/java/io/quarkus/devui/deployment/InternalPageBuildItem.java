package io.quarkus.devui.deployment;


import io.quarkus.builder.item.MultiBuildItem;
import io.quarkus.devui.spi.page.Page;
import java.util.ArrayList;
import java.util.List;

/**
 * Used internally to define some of our own pages
 */
public final class InternalPageBuildItem extends MultiBuildItem {

    private final String namespaceLabel;
    private final List<Page> pages = new ArrayList<>();
    
    
    public InternalPageBuildItem(String namespaceLabel) {
        this.namespaceLabel = namespaceLabel;
    }

    public void addPage(Page page) {
        this.pages.add(page);
    }

//    public void addBuildTimeData(String key, String path) {
//        this.importMap.put(key, path);
//    }

    public List<Page> getPages() {
        return pages;
    }
}
