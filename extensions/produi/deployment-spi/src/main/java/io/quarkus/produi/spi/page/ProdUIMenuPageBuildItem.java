package io.quarkus.produi.spi.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.quarkus.produi.spi.AbstractProdUIBuildItem;

/**
 * Add a menu item to Prod UI sidebar.
 */
public final class ProdUIMenuPageBuildItem extends AbstractProdUIBuildItem {

    private final List<PageBuilder<?>> pageBuilders;

    public ProdUIMenuPageBuildItem() {
        super();
        this.pageBuilders = new ArrayList<>();
    }

    public ProdUIMenuPageBuildItem(PageBuilder<?>... pageBuilders) {
        super();
        this.pageBuilders = new ArrayList<>(Arrays.asList(pageBuilders));
    }

    public ProdUIMenuPageBuildItem(String customIdentifier, PageBuilder<?>... pageBuilders) {
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
