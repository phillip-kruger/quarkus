package io.quarkus.devui.deployment.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.BuildTimeConstBuildItem;
import io.quarkus.devui.deployment.ExtensionsBuildItem;
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
            ExtensionsBuildItem extensionsBuildItem) {

        BuildTimeConstBuildItem internalBuildTimeData = new BuildTimeConstBuildItem(PageBuildItem.DEV_UI);

        // Theme details TODO: Allow configuration
        Map<String, Map<String, Color>> themes = new HashMap<>();
        Map<String, Color> dark = new HashMap<>();
        Map<String, Color> light = new HashMap<>();

        // Main background
        dark.put("background1", Color.from(210, 10, 23));
        light.put("background1", Color.from(0, 0, 100));

        // Main color
        dark.put("color1", Color.from(0, 0, 90));
        light.put("color1", Color.from(210, 10, 23));

        // Grey colors
        dark.put("mute1", Color.from(218, 25, 71));
        light.put("mute1", Color.from(212, 21, 41));

        // Quarkus logo colors
        dark.put("logo1", Color.from(211, 63, 54)); //blue
        light.put("logo1", Color.from(211, 63, 54));

        dark.put("logo2", Color.from(343, 100, 50)); // red
        light.put("logo2", Color.from(343, 100, 50));

        dark.put("logo3", Color.from(180, 36, 5)); // black/white
        light.put("logo3", Color.from(0, 0, 90));

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
    }

    static class Color {
        private int hue; // Defines a degree on the color wheel (from 0 to 360) - 0 (or 360) is red, 120 is green, 240 is blue
        private int saturation; // Defines the saturation; 0% is a shade of gray and 100% is the full color (full saturation)
        private int lightness; // Defines the lightness; 0% is black, 50% is normal, and 100% is white

        private Color(int hue, int saturation, int lightness) {
            this.hue = hue;
            this.saturation = saturation;
            this.lightness = lightness;
        }

        public String getColor() {
            return this.toString();
        }

        public String getDarker() {
            int l = 0;
            if (this.lightness >= 10) {
                l = this.lightness - 10;
            }

            return new Color(this.hue, this.saturation, l).toString();
        }

        public String getLighter() {
            int l = 100;
            if (this.lightness <= 90) {
                l = this.lightness + 10;
            }

            return new Color(this.hue, this.saturation, l).toString();
        }

        @Override
        public String toString() {
            return "hsl(" + hue + ", " + saturation + "%, " + lightness + "%)";
        }

        static Color from(int hue, int saturation, int lightness) {
            if (saturation < 0 || saturation > 360) {
                throw new RuntimeException(
                        "Invalid hue, number needs to be between 0 and 360. Defines a degree on the color wheel");
            }

            if (saturation < 0 || saturation > 100) {
                throw new RuntimeException(
                        "Invalid saturation, number needs to be between 0 and 100. 0% is a shade of gray and 100% is the full color (full saturation)");
            }
            if (lightness < 0 || lightness > 100) {
                throw new RuntimeException(
                        "Invalid lightness, number needs to be between 0 and 100. 0% is black, 50% is normal, and 100% is white");
            }
            return new Color(hue, saturation, lightness);
        }
    }

}
