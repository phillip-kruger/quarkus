package io.quarkus.devui.deployment.spi;

import java.util.List;

import io.quarkus.builder.item.MultiBuildItem;

/**
 * Add links to the Dev UI Card
 */
public final class DevUICardLinksBuildItem extends MultiBuildItem {

    private static final String SPACE = " ";
    private static final String DASH = "-";
    private final String extensionName;
    private final List<CardLink> links;

    public DevUICardLinksBuildItem(String extensionName, List<CardLink> links) {
        this.extensionName = extensionName;
        this.links = links;
    }

    public String getExtensionName() {
        return extensionName;
    }

    public String getExtensionPathName() {
        return extensionName.toLowerCase().replaceAll(SPACE, DASH);
    }

    public List<CardLink> getLinks() {
        return links;
    }

    public boolean hasBuildTimeData() {
        if (this.links != null && !this.links.isEmpty()) {
            for (CardLink link : this.links) {
                if (link.hasBuildTimeData()) {
                    return true;
                }
            }
        }
        return false;
    }
}
