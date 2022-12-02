package io.quarkus.devui.deployment;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.mvnpm.importmap.Aggregator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.builder.Version;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.util.IoUtil;
import io.quarkus.devui.deployment.spi.DevUIContent;
import io.quarkus.devui.deployment.spi.buildtime.QuteTemplateBuildItem;
import io.quarkus.devui.deployment.spi.buildtime.StaticContentBuildItem;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;

/**
 * Process all Build Time static content. (Content generated at build time)
 */
public class BuildTimeStaticContentProcessor {

    private static final String BUILD_TIME_PATH = "dev-ui-templates" + File.separator + "build-time";
    final Config config = ConfigProvider.getConfig();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Here we create references to internal dev ui file so that they can be imported by ref.
     * This will be merged into the final importmap
     */
    @BuildStep(onlyIf = IsDevelopment.class)
    InternalImportMapBuildItem createKnownInternalImportMap() {
        InternalImportMapBuildItem internalImportMapBuildItem = new InternalImportMapBuildItem();

        // Also add some of our own
        // TODO: Test by adding this to an importMap.json ?
        internalImportMapBuildItem.add("qwc/", "./qwc/");
        internalImportMapBuildItem.add("icon/", "./icon/");
        internalImportMapBuildItem.add("font/", "./font/");
        internalImportMapBuildItem.add("controller/", "./controller/");
        internalImportMapBuildItem.add("jsonrpc", "./controller/jsonrpc.js");
        internalImportMapBuildItem.add("log-controller", "./controller/log-controller.js");
        internalImportMapBuildItem.add("router-controller", "./controller/router-controller.js");
        internalImportMapBuildItem.add("notification-controller", "./controller/notification-controller.js");

        return internalImportMapBuildItem;
    }

    /**
     * Here we map all the pages (as defined by the extensions) build time data
     *
     * @param pageBuildItems
     * @param buildTimeConstProducer
     */
    @BuildStep(onlyIf = IsDevelopment.class)
    void mapPageBuildTimeData(List<PageBuildItem> pageBuildItems,
            BuildProducer<BuildTimeConstBuildItem> buildTimeConstProducer) {

        for (PageBuildItem pageBuildItem : pageBuildItems) {
            if (pageBuildItem.hasBuildTimeData()) {
                buildTimeConstProducer.produce(
                        new BuildTimeConstBuildItem(pageBuildItem.getExtensionName(), pageBuildItem.getBuildTimeData()));
            }
        }
    }

    /**
     * Here we find all build time data and make then available via a const
     *
     * js components can import the const with "import {constName} from '{ext}-data';"
     *
     * @param pageBuildItems
     * @param quteTemplateProducer
     * @param internalImportMapProducer
     */
    @BuildStep(onlyIf = IsDevelopment.class)
    void createBuildTimeConstJsTemplate(
            List<BuildTimeConstBuildItem> buildTimeConstBuildItems,
            BuildProducer<QuteTemplateBuildItem> quteTemplateProducer,
            BuildProducer<InternalImportMapBuildItem> internalImportMapProducer) {

        QuteTemplateBuildItem quteTemplateBuildItem = new QuteTemplateBuildItem(
                QuteTemplateBuildItem.DEV_UI);

        InternalImportMapBuildItem internalImportMapBuildItem = new InternalImportMapBuildItem();

        for (BuildTimeConstBuildItem buildTimeConstBuildItem : buildTimeConstBuildItems) {
            Map<String, Object> data = new HashMap<>();
            if (buildTimeConstBuildItem.hasBuildTimeData()) {
                for (Map.Entry<String, Object> pageData : buildTimeConstBuildItem.getBuildTimeData().entrySet()) {
                    try {
                        String key = pageData.getKey();
                        String value = objectMapper.writerWithDefaultPrettyPrinter()
                                .writeValueAsString(pageData.getValue());
                        data.put(key, value);
                    } catch (JsonProcessingException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            if (!data.isEmpty()) {
                Map<String, Object> qutedata = new HashMap<>();
                qutedata.put("buildTimeData", data);
                String ref = buildTimeConstBuildItem.getExtensionPathName() + "-data";
                String file = ref + ".js";
                quteTemplateBuildItem.add("build-time-data.js", file, qutedata);
                internalImportMapBuildItem.add(ref, "./" + file);
            }
        }

        quteTemplateProducer.produce(quteTemplateBuildItem);
        internalImportMapProducer.produce(internalImportMapBuildItem);
    }

    /**
     * Here we create index.html
     * We aggregate all import maps into one
     * This includes import maps from 3rd party libs from mvnpm.org and internal ones defined above
     *
     * @return The QuteTemplate Build item that will create the end result
     */
    @BuildStep(onlyIf = IsDevelopment.class)
    QuteTemplateBuildItem createIndexHtmlTemplate(List<InternalImportMapBuildItem> internalImportMapBuildItems) {
        QuteTemplateBuildItem quteTemplateBuildItem = new QuteTemplateBuildItem(
                QuteTemplateBuildItem.DEV_UI);

        for (InternalImportMapBuildItem importMapBuildItem : internalImportMapBuildItems) {
            Map<String, String> importMap = importMapBuildItem.getImportMap();
            Aggregator.add(importMap);
        }

        String importmap = Aggregator.aggregateAsJson();

        // TODO: Move version and name to build time data

        Map<String, Object> data = Map.of(
                "importmap", importmap,
                "quarkusVersion", Version.getVersion(),
                "applicationName", config.getOptionalValue("quarkus.application.name", String.class).orElse(""),
                "applicationVersion", config.getOptionalValue("quarkus.application.version", String.class).orElse(""));

        quteTemplateBuildItem.add("index.html", data);

        return quteTemplateBuildItem;
    }

    // Here load all templates
    @BuildStep(onlyIf = IsDevelopment.class)
    void loadAllBuildTimeTemplates(BuildProducer<StaticContentBuildItem> buildTimeContentProducer,
            List<QuteTemplateBuildItem> templates) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        for (QuteTemplateBuildItem template : templates) {

            List<DevUIContent> contentPerExtension = new ArrayList<>();

            if (template.isInternal()) {
                List<QuteTemplateBuildItem.TemplateData> templatesWithData = template.getTemplateDatas();
                for (QuteTemplateBuildItem.TemplateData e : templatesWithData) {

                    String templateName = e.getTemplateName(); // Relative to BUILD_TIME_PATH
                    Map<String, Object> data = e.getData();
                    String resourceName = BUILD_TIME_PATH + File.separator + templateName;
                    String fileName = e.getFileName();
                    // TODO: What if we find more than one ?
                    try (InputStream templateStream = cl.getResourceAsStream(resourceName)) {
                        if (templateStream != null) {
                            byte[] templateContent = IoUtil.readBytes(templateStream);
                            // Internal runs on "naked" namespace
                            DevUIContent content = DevUIContent.builder()
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
                        StaticContentBuildItem.DEV_UI, contentPerExtension)); // TODO: This should not be internal ?
            } else {
                // TODO: Also handle case for extensions
            }
        }
    }
}
