package io.quarkus.devui.spi;

import java.util.Optional;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

public class ProdUI {
    boolean enabled = false;

    public ProdUI() {
        Config config = ConfigProvider.getConfig();
        Optional<Boolean> maybe = config.getOptionalValue("quarkus.dev-ui.prod.enabled", Boolean.class);
        this.enabled = maybe.orElse(false);
    }

    public static boolean isEnabled() {
        ProdUI prodUI = new ProdUI();
        return prodUI.enabled;
    }

}
