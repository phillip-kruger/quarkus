package io.quarkus.produi.spi.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.quarkus.produi.spi.AbstractProdUIBuildItem;

/**
 * Add a page (or section) to Prod UI.
 * This is the main way extensions contribute pages to the production UI.
 */
public final class ProdUICardPageBuildItem extends AbstractProdUIBuildItem {

    private final List<PageBuilder<?>> pageBuilders;

    public ProdUICardPageBuildItem() {
        super();
        this.pageBuilders = new ArrayList<>();
    }

    public ProdUICardPageBuildItem(PageBuilder<?>... pageBuilders) {
        super();
        this.pageBuilders = new ArrayList<>(Arrays.asList(pageBuilders));
    }

    public ProdUICardPageBuildItem(String customIdentifier) {
        super(customIdentifier);
        this.pageBuilders = new ArrayList<>();
    }

    public ProdUICardPageBuildItem(String customIdentifier, PageBuilder<?>... pageBuilders) {
        super(customIdentifier);
        this.pageBuilders = new ArrayList<>(Arrays.asList(pageBuilders));
    }

    public void addPage(PageBuilder<?> page) {
        this.pageBuilders.add(page);
    }

    public List<PageBuilder<?>> getPages() {
        return this.pageBuilders;
    }

    public boolean hasPages() {
        return !pageBuilders.isEmpty();
    }
}
