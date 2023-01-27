package io.quarkus.devui.deployment.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.BuildTimeConstBuildItem;
import io.quarkus.devui.deployment.ExtensionsBuildItem;
import io.quarkus.devui.deployment.ThemeVarsBuildItem;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;
import io.quarkus.devui.runtime.jsonrpc.handler.MenuItem;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionGroup;

/**
 * Processor that creates the parts needed for the Dev UI Internal components
 */
public class InternalProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createBuildTimeData(BuildProducer<BuildTimeConstBuildItem> buildTimeConstProducer,
            BuildProducer<ThemeVarsBuildItem> themeVarsProducer,
            ExtensionsBuildItem extensionsBuildItem) {

        BuildTimeConstBuildItem internalBuildTimeData = new BuildTimeConstBuildItem(PageBuildItem.DEV_UI);

        // Theme details TODO: Allow configuration
        Map<String, Map<String, String>> themes = new HashMap<>();
        Map<String, String> dark = new HashMap<>();
        Map<String, String> light = new HashMap<>();

        // Quarkus logo colors
        light.put("--quarkus-blue", QUARKUS_BLUE.toString());
        dark.put("--quarkus-blue", QUARKUS_BLUE.toString());

        light.put("--quarkus-red", QUARKUS_RED.toString());
        dark.put("--quarkus-red", QUARKUS_RED.toString());

        light.put("--quarkus-center", QUARKUS_DARK.toString());
        dark.put("--quarkus-center", QUARKUS_LIGHT.toString());

        // Vaadin's Lumo (see https://vaadin.com/docs/latest/styling/lumo/design-tokens/color)

        // Base
        light.put("--lumo-base-color", Color.from(0, 100, 100).toString());
        dark.put("--lumo-base-color", Color.from(210, 10, 23).toString());

        // Grayscale
        light.put("--lumo-contrast-5pct", Color.from(214, 61, 25, 0.05).toString());
        dark.put("--lumo-contrast-5pct", Color.from(214, 65, 85, 0.06).toString());
        light.put("--lumo-contrast-10pct", Color.from(214, 57, 24, 0.1).toString());
        dark.put("--lumo-contrast-10pct", Color.from(214, 60, 80, 0.14).toString());
        light.put("--lumo-contrast-20pct", Color.from(214, 53, 23, 0.16).toString());
        dark.put("--lumo-contrast-20pct", Color.from(214, 64, 82, 0.23).toString());
        light.put("--lumo-contrast-30pct", Color.from(214, 50, 22, 0.26).toString());
        dark.put("--lumo-contrast-30pct", Color.from(214, 69, 84, 0.32).toString());
        light.put("--lumo-contrast-40pct", Color.from(214, 47, 21, 0.38).toString());
        dark.put("--lumo-contrast-40pct", Color.from(214, 73, 86, 0.41).toString());
        light.put("--lumo-contrast-50pct", Color.from(214, 45, 20, 0.52).toString());
        dark.put("--lumo-contrast-50pct", Color.from(214, 78, 88, 0.50).toString());
        light.put("--lumo-contrast-60pct", Color.from(214, 43, 19, 0.6).toString());
        dark.put("--lumo-contrast-60pct", Color.from(214, 82, 90, 0.6).toString());
        light.put("--lumo-contrast-70pct", Color.from(214, 42, 18, 0.69).toString());
        dark.put("--lumo-contrast-70pct", Color.from(214, 87, 92, 0.7).toString());
        light.put("--lumo-contrast-80pct", Color.from(214, 41, 17, 0.83).toString());
        dark.put("--lumo-contrast-80pct", Color.from(214, 91, 94, 0.8).toString());
        light.put("--lumo-contrast-90pct", Color.from(214, 40, 16, 0.94).toString());
        dark.put("--lumo-contrast-90pct", Color.from(214, 96, 96, 0.9).toString());
        light.put("--lumo-contrast", Color.from(214, 35, 15).toString());
        dark.put("--lumo-contrast", Color.from(214, 100, 98).toString());

        // Primary
        light.put("--lumo-primary-color-10pct", Color.from(214, 100, 60, 0.13).toString());
        dark.put("--lumo-primary-color-10pct", Color.from(214, 90, 63, 0.1).toString());
        light.put("--lumo-primary-color-50pct", Color.from(QUARKUS_BLUE, 0.76).toString());
        dark.put("--lumo-primary-color-50pct", Color.from(QUARKUS_BLUE, 0.5).toString());
        light.put("--lumo-primary-color", QUARKUS_BLUE.toString());
        dark.put("--lumo-primary-color", QUARKUS_BLUE.toString());
        light.put("--lumo-primary-text-color", QUARKUS_BLUE.toString());
        dark.put("--lumo-primary-text-color", QUARKUS_BLUE.toString());
        light.put("--lumo-primary-contrast-color", Color.from(0, 100, 100).toString());
        dark.put("--lumo-primary-contrast-color", Color.from(0, 100, 100).toString());

        // Error
        light.put("--lumo-error-color-10pct", Color.from(3, 85, 49, 0.1).toString());
        dark.put("--lumo-error-color-10pct", Color.from(3, 90, 63, 0.1).toString());
        light.put("--lumo-error-color-50pct", Color.from(3, 85, 49, 0.5).toString());
        dark.put("--lumo-error-color-50pct", Color.from(3, 90, 63, 0.5).toString());
        light.put("--lumo-error-color", Color.from(3, 85, 48).toString());
        dark.put("--lumo-error-color", Color.from(3, 90, 63).toString());
        light.put("--lumo-error-text-color", Color.from(3, 89, 42).toString());
        dark.put("--lumo-error-text-color", Color.from(3, 100, 67).toString());
        light.put("--lumo-error-contrast-color", Color.from(0, 100, 100).toString());
        dark.put("--lumo-error-contrast-color", Color.from(0, 100, 100).toString());

        // Success
        light.put("--lumo-success-color-10pct", Color.from(145, 72, 31, 0.1).toString());
        dark.put("--lumo-success-color-10pct", Color.from(145, 65, 42, 0.1).toString());
        light.put("--lumo-success-color-50pct", Color.from(145, 72, 31, 0.5).toString());
        dark.put("--lumo-success-color-50pct", Color.from(145, 65, 42, 0.5).toString());
        light.put("--lumo-success-color", Color.from(145, 72, 30).toString());
        dark.put("--lumo-success-color", Color.from(145, 65, 42).toString());
        light.put("--lumo-success-text-color", Color.from(145, 85, 25).toString());
        dark.put("--lumo-success-text-color", Color.from(145, 85, 47).toString());
        light.put("--lumo-success-contrast-color", Color.from(0, 100, 100).toString());
        dark.put("--lumo-success-contrast-color", Color.from(0, 100, 100).toString());

        // Text
        light.put("--lumo-header-text-color", Color.from(214, 35, 15).toString());
        dark.put("--lumo-header-text-color", Color.from(214, 100, 98).toString());
        light.put("--lumo-body-text-color", Color.from(214, 40, 16, 0.94).toString());
        dark.put("--lumo-body-text-color", Color.from(214, 96, 96, 0.9).toString());
        light.put("--lumo-secondary-text-color", Color.from(214, 42, 18, 0.69).toString());
        dark.put("--lumo-secondary-text-color", Color.from(214, 87, 92, 0.7).toString());
        light.put("--lumo-tertiary-text-color", Color.from(214, 45, 20, 0.52).toString());
        dark.put("--lumo-tertiary-text-color", Color.from(214, 78, 88, 0.5).toString());
        light.put("--lumo-disabled-text-color", Color.from(214, 50, 22, 0.26).toString());
        dark.put("--lumo-disabled-text-color", Color.from(214, 69, 84, 0.32).toString());

        themes.put("dark", dark);
        themes.put("light", light);

        internalBuildTimeData.addBuildTimeData("themes", themes);

        // Extensions
        Map<ExtensionGroup, List<Extension>> response = Map.of(
                ExtensionGroup.active, extensionsBuildItem.getActiveExtensions(),
                ExtensionGroup.inactive, extensionsBuildItem.getInactiveExtensions());

        internalBuildTimeData.addBuildTimeData("extensions", response);

        // Sections Menu
        // TODO: Get this from PageBuildItem
        List<MenuItem> menuItems = List.of(new MenuItem("qwc-extensions", "puzzle-piece", true),
                new MenuItem("qwc-configuration", "sliders"),
                new MenuItem("qwc-continuous-testing", "flask-vial"),
                new MenuItem("qwc-dev-services", "wand-magic-sparkles"),
                new MenuItem("qwc-build-steps", "hammer"));

        internalBuildTimeData.addBuildTimeData("menuItems", menuItems);

        // TODO: Implement below
        internalBuildTimeData.addBuildTimeData("allConfiguration", "Loading Configuration");
        internalBuildTimeData.addBuildTimeData("continuousTesting", "Loading Continuous Testing");
        internalBuildTimeData.addBuildTimeData("devServices", "Loading Dev Services");
        internalBuildTimeData.addBuildTimeData("buildSteps", "Loading Build Steps");

        buildTimeConstProducer.produce(internalBuildTimeData);

        themeVarsProducer.produce(new ThemeVarsBuildItem(light.keySet(), QUARKUS_BLUE.toString()));
    }

    private static final Color QUARKUS_BLUE = Color.from(211, 63, 54);
    private static final Color QUARKUS_RED = Color.from(343, 100, 50);
    private static final Color QUARKUS_DARK = Color.from(180, 36, 5);
    private static final Color QUARKUS_LIGHT = Color.from(0, 0, 90);

    /**
     * This represents a HSLA color
     * see https://www.w3schools.com/html/html_colors_hsl.asp
     */
    static class Color {
        private int hue; // Defines a degree on the color wheel (from 0 to 360) - 0 (or 360) is red, 120 is green, 240 is blue
        private int saturation; // Defines the saturation; 0% is a shade of gray and 100% is the full color (full saturation)
        private int lightness; // Defines the lightness; 0% is black, 50% is normal, and 100% is white
        private double alpha; // Defines the opacity; 0 is fully transparent, 100 is not transparent at all

        private Color(int hue, int saturation, int lightness, double alpha) {
            if (hue < 0 || hue > 360) {
                throw new RuntimeException(
                        "Invalid hue, number needs to be between 0 and 360. Defines a degree on the color wheel");
            }
            this.hue = hue;

            if (saturation < 0 || saturation > 100) {
                throw new RuntimeException(
                        "Invalid saturation, number needs to be between 0 and 100. 0% is a shade of gray and 100% is the full color (full saturation)");
            }
            this.saturation = saturation;

            if (lightness < 0 || lightness > 100) {
                throw new RuntimeException(
                        "Invalid lightness, number needs to be between 0 and 100. 0% is black, 50% is normal, and 100% is white");
            }
            this.lightness = lightness;

            if (alpha < 0 || alpha > 1) {
                throw new RuntimeException(
                        "Invalid alpha, number needs to be between 0 and 1. 0 is fully transparent, 1 is not transparent at all");
            }
            this.alpha = alpha;
        }

        @Override
        public String toString() {
            return "hsla(" + this.hue + ", " + this.saturation + "%, " + this.lightness + "%, " + this.alpha + ")";
        }

        static Color from(Color color, double alpha) {
            return new Color(color.hue, color.saturation, color.lightness, alpha);
        }

        static Color from(int hue, int saturation, int lightness) {
            return new Color(hue, saturation, lightness, 1);
        }

        static Color from(int hue, int saturation, int lightness, double alpha) {
            return new Color(hue, saturation, lightness, alpha);
        }
    }

}
