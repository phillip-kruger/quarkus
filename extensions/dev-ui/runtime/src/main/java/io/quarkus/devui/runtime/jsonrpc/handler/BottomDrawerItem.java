package io.quarkus.devui.runtime.jsonrpc.handler;

public class BottomDrawerItem {
    private String webcomponent;

    public BottomDrawerItem() {
    }

    public BottomDrawerItem(String webcomponent) {
        this.webcomponent = webcomponent;
    }

    public String getWebcomponent() {
        return webcomponent;
    }

    public void setWebcomponent(String webcomponent) {
        this.webcomponent = webcomponent;
    }

}
