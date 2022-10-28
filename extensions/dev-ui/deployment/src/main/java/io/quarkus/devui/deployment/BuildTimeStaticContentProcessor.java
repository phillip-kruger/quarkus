package io.quarkus.devui.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.mvnpm.importmap.Aggregator;

import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.util.IoUtil;
import io.quarkus.devui.deployment.spi.DevUIContent;
import io.quarkus.devui.deployment.spi.buildtime.QuteTemplateBuildItem;
import io.quarkus.devui.deployment.spi.buildtime.StaticContentBuildItem;

/**
 * Process all Build Time static content. (Content generated at build time)
 */
public class BuildTimeStaticContentProcessor {

    private static final String BUILD_TIME_PATH = "dev-ui-templates" + File.separator + "build-time";
    final Config config = ConfigProvider.getConfig();

    // Add our own internal files
    @BuildStep(onlyIf = IsDevelopment.class)
    QuteTemplateBuildItem createBuildTimeTemplate() {
        QuteTemplateBuildItem builtTimeTemplateBuildItem = new QuteTemplateBuildItem(
                QuteTemplateBuildItem.INTERNAL);

        // Also add some of our own
        // TODO: Test by adding this to an importMap.json ?
        Aggregator.add("qwc/", "./qwc/");
        Aggregator.add("icon/", "./icon/");
        Aggregator.add("controller/", "./controller/");
        Aggregator.add("jsonrpc-controller", "./controller/jsonrpc-controller.js");
        Aggregator.add("router-controller", "./controller/router-controller.js");
        Aggregator.add("notification-controller", "./controller/notification-controller.js");
        Aggregator.add("@qwc/app-info", "./qwc/qwc-app-info.js");
        Aggregator.add("@qwc/quarkus-version", "./qwc/qwc-quarkus-version.js");
        Aggregator.add("@qwc/extension", "./qwc/qwc-extension.js");
        Aggregator.add("@qwc/extension-link", "./qwc/qwc-extension-link.js");

        String importmap = Aggregator.aggregateAsJson();

        Map<String, Object> data = Map.of(
                "importmap", importmap,
                "quarkusVersion", Version.getVersion(),
                "applicationName", config.getOptionalValue("quarkus.application.name", String.class).orElse(""),
                "applicationVersion", config.getOptionalValue("quarkus.application.version", String.class).orElse(""));

        builtTimeTemplateBuildItem.add("index.html", data);

        return builtTimeTemplateBuildItem;
    }

    // Here load all templates
    @BuildStep(onlyIf = IsDevelopment.class)
    void loadAllBuildTimeTemplates(BuildProducer<StaticContentBuildItem> buildTimeContentProducer,
            List<QuteTemplateBuildItem> templates) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (QuteTemplateBuildItem template : templates) {

            List<DevUIContent> contentPerExtension = new ArrayList<>();

            if (template.isInternal()) {
                Map<String, Map<String, Object>> fileAndData = template.getFileAndData();
                Set<Map.Entry<String, Map<String, Object>>> entrySet = fileAndData.entrySet();
                for (Map.Entry<String, Map<String, Object>> e : entrySet) {
                    String fileName = e.getKey(); // Relative to BUILD_TIME_PATH
                    Map<String, Object> data = e.getValue();
                    String resourceName = BUILD_TIME_PATH + File.separator + fileName;
                    // TODO: What if we find more than one ?
                    try (InputStream templateStream = cl.getResourceAsStream(resourceName)) {
                        if (templateStream != null) {
                            byte[] templateContent = IoUtil.readBytes(templateStream);
                            // Internal runs on "naked" namespace
                            DevUIContent content = new DevUIContent.Builder()
                                    .fileName(fileName)
                                    .template(templateContent)
                                    .addData(data)
                                    .build();
                            contentPerExtension.add(content);
                        }
                    } catch (IOException ioe) {
                        throw new UncheckedIOException("An error occurred while processing " + resourceName, ioe);
                    }
                }
                buildTimeContentProducer.produce(new StaticContentBuildItem(
                        StaticContentBuildItem.INTERNAL, contentPerExtension));
            } else {
                // TODO: Also handle case for extensions
            }
        }
    }
}
