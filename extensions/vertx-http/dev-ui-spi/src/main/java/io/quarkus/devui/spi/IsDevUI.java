package io.quarkus.devui.spi;

import java.util.function.BooleanSupplier;

import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.dev.spi.DevModeType;
import io.quarkus.runtime.LaunchMode;

/**
 * Boolean supplier that returns true if the application is running in development, or if config allow prod mode
 * mode. Intended for use with {@link BuildStep#onlyIf()}
 */
public class IsDevUI implements BooleanSupplier {

    private final LaunchMode launchMode;
    private final DevModeType devModeType;
    private final ProdUI prodUI;

    public IsDevUI(LaunchMode launchMode,
            DevModeType devModeType) {
        this.launchMode = launchMode;
        this.devModeType = devModeType;
        this.prodUI = new ProdUI();
    }

    @Override
    public boolean getAsBoolean() {
        if (launchMode.equals(LaunchMode.DEVELOPMENT) && devModeType.equals(DevModeType.LOCAL)) {
            return true;
        } else {
            return prodUI.enabled;
        }
    }
}
