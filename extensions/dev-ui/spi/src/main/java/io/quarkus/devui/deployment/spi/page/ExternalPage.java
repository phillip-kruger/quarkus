package io.quarkus.devui.deployment.spi.page;

/**
 * Define a "page" in Dev UI. This page links to an external UI.
 *
 * TODO: Allow rendering in Dev UI
 * TODO: Validate URL
 */
public class ExternalPage extends AbstractPage {

    private final String externalURL; // The outside link. This can be relative.
    private final boolean embed;

    private ExternalPage(Builder builder) {
        super(builder.iconName,
                builder.displayName,
                builder.label);//,
        //                builder.showLinkOnCard,
        //                builder.showLinkOnSideMenu,
        //                builder.showLinkOnNavigationBar);

        this.externalURL = builder.externalURL;
        this.embed = builder.embed;
    }

    public String getExternalURL() {
        return externalURL;
    }

    public boolean shouldEmbed() {
        return this.embed;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private String externalURL;
        private boolean embed = true; // default

        public Builder() {
        }

        public Builder(String externalURL) {
            this.externalURL = externalURL;
        }

        public Builder externalURL(String externalURL) {
            if (externalURL.isEmpty()) {
                throw new RuntimeException("Invalid external URL, can not be empty");
            }
            // TODO: Validate that this is a URL. Full or relative

            this.externalURL = externalURL;
            return this;
        }

        public Builder doNotEmbed() {
            this.embed = false;
            return this;
        }

        public ExternalPage build() {
            if (externalURL == null) {
                throw new RuntimeException(
                        "Not enough information to create a link. Set at least the external URL");
            }
            return new ExternalPage(this);
        }
    }

}
