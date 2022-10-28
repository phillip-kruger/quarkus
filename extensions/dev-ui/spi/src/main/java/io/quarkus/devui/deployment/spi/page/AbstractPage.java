package io.quarkus.devui.deployment.spi.page;

/**
 * Define a "page" in Dev UI. This not a full web page, but rather the section in the middle where extensions can display data
 *
 * Navigation to this page is also defined here.
 */
public abstract class AbstractPage {

    private final String iconName;
    private final String displayName;
    private final String label;

    //    private final boolean showLinkOnCard; // default true
    //    private final boolean showLinkOnSideMenu; // default false
    //    private final boolean showLinkOnNavigationBar; // default true

    public AbstractPage(String iconName,
            String displayName,
            String label) {//,
        //            boolean showLinkOnCard,
        //            boolean showLinkOnSideMenu,
        //            boolean showLinkOnNavigationBar) {

        if (iconName == null) {
            this.iconName = "font-awesome-solid:arrow-right";
        } else {
            this.iconName = iconName;
        }
        this.displayName = displayName;
        this.label = label;
        //        this.showLinkOnCard = showLinkOnCard;
        //        this.showLinkOnSideMenu = showLinkOnSideMenu;
        //        this.showLinkOnNavigationBar = showLinkOnNavigationBar;
    }

    public String getIconName() {
        return iconName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLabel() {
        return label;
    }

    //    public boolean isShowLinkOnCard() {
    //        return showLinkOnCard;
    //    }
    //
    //    public boolean isShowLinkOnSideMenu() {
    //        return showLinkOnSideMenu;
    //    }
    //
    //    public boolean isShowLinkOnNavigationBar() {
    //        return showLinkOnNavigationBar;
    //    }

    protected static abstract class AbstractBuilder<T> {
        protected String iconName = null;
        protected String displayName = null;
        protected String label = null;

        // Navigation
        //        protected boolean showLinkOnCard = true;
        //        protected boolean showLinkOnSideMenu = false;
        //        protected boolean showLinkOnNavigationBar = true;

        @SuppressWarnings("unchecked")
        public T iconName(String iconName) {
            this.iconName = iconName;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T displayName(String displayName) {
            this.displayName = displayName;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T label(String label) {
            this.label = label;
            return (T) this;
        }

        //        @SuppressWarnings("unchecked")
        //        public T hideLinkOnCard() {
        //            this.showLinkOnCard = false;
        //            return (T) this;
        //        }
        //
        //        @SuppressWarnings("unchecked")
        //        public T hideLinkOnNavigationBar() {
        //            this.showLinkOnNavigationBar = false;
        //            return (T) this;
        //        }
        //
        //        @SuppressWarnings("unchecked")
        //        public T showLinkOnSideMenu() {
        //            this.showLinkOnNavigationBar = true;
        //            return (T) this;
        //        }
    }
}
