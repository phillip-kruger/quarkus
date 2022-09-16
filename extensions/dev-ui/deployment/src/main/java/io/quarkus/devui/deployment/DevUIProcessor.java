package io.quarkus.devui.deployment;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.devui.deployment.spi.CardLink;
import io.quarkus.devui.deployment.spi.DevUICardLinksBuildItem;
import io.quarkus.devui.runtime.DevUIRecorder;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRouter;
import io.quarkus.devui.runtime.jsonrpc.handler.DevUIPlatformHandler;
import io.quarkus.devui.runtime.service.extension.Codestart;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionsService;
import io.quarkus.devui.runtime.service.extension.Link;
import io.quarkus.maven.dependency.GACT;
import io.quarkus.maven.dependency.GACTV;
import io.quarkus.webjar.deployment.WebJarBuildItem;
import io.quarkus.webjar.deployment.WebJarResultsBuildItem;
import io.smallrye.common.classloader.ClassPathUtils;

/**
 * Processor for Dev UI
 */
public class DevUIProcessor {

    private static final Logger log = Logger.getLogger(DevUIProcessor.class);
    private static final String JAR = "jar";
    private static final GACT UI_JAR = new GACT("io.quarkus", "quarkus-dev-ui-deployment", null, JAR);
    private static final String DEVUI = "dev-ui";
    private static final String SLASH = "/";
    private static final String DOUBLE_POINT = ":";
    private static final String DASH_DEPLOYMENT = "-deployment";

    @BuildStep(onlyIf = IsDevelopment.class)
    void additionalBean(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer) {
        additionalBeanProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(JsonRpcRouter.class)
                .setUnremovable().build());
        additionalBeanProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(DevUIPlatformHandler.class)
                .setUnremovable().build());

        additionalBeanProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(ExtensionsService.class)
                .setUnremovable().build());
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.STATIC_INIT)
    void createRuntimeExtensions(DevUIRecorder recorder,
            BeanContainerBuildItem beanContainer,
            ExtensionsBuildItem extensionsProducer) {

        recorder.createExtensionService(beanContainer.getValue(), extensionsProducer.getActiveExtensions(),
                extensionsProducer.getInactiveExtensions());

    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @SuppressWarnings("unchecked")
    void getAllExtensions(List<DevUICardLinksBuildItem> activeCards,
            BuildProducer<ExtensionsBuildItem> extensionsProducer,
            BuildProducer<WebJarBuildItem> webJarBuildProducer,
            BuildProducer<DevUIWebJarBuildItem> devUIWebJarProducer) {

        // First create the static resources for our own internal components
        webJarBuildProducer.produce(WebJarBuildItem.builder()
                .artifactKey(UI_JAR)
                .root(DEVUI + SLASH).build());

        devUIWebJarProducer.produce(new DevUIWebJarBuildItem(UI_JAR, DEVUI));

        // Now go through all extensions and check them
        Map<String, DevUICardLinksBuildItem> activeExtensionsMap = getActiveExtensionsMap(activeCards);

        try {
            final Yaml yaml = new Yaml(new SafeConstructor());
            List<Extension> activeExtensions = new ArrayList<>();
            List<Extension> inactiveExtensions = new ArrayList<>();
            ClassPathUtils.consumeAsPaths(YAML_FILE, p -> {
                try {
                    Extension extension = new Extension();
                    final String extensionYaml;
                    try (Scanner scanner = new Scanner(Files.newBufferedReader(p, StandardCharsets.UTF_8))) {
                        scanner.useDelimiter("\\A");
                        extensionYaml = scanner.hasNext() ? scanner.next() : null;
                    }
                    if (extensionYaml == null) {
                        // This is a internal extension (like this one, Dev UI)
                        return;
                    }

                    final Map<String, Object> extensionMap = yaml.load(extensionYaml);

                    if (extensionMap.containsKey(NAME)) {
                        String name = (String) extensionMap.get(NAME);
                        extension.setNamespace(getExtensionNamespace(extensionMap));
                        extension.setName(name);
                        extension.setDescription((String) extensionMap.getOrDefault(DESCRIPTION, null));
                        String artifactId = (String) extensionMap.getOrDefault(ARTIFACT, null);
                        extension.setArtifact(artifactId);

                        Map<String, Object> metaData = (Map<String, Object>) extensionMap.getOrDefault(METADATA, null);
                        extension.setKeywords((List<String>) metaData.getOrDefault(KEYWORDS, null));
                        extension.setShortName((String) metaData.getOrDefault(SHORT_NAME, null));

                        if (metaData.containsKey(GUIDE)) {
                            String guide = (String) metaData.get(GUIDE);
                            try {
                                extension.setGuide(new URL(guide));
                            } catch (MalformedURLException mue) {
                                log.error("Could not set Guide URL [" + guide + "]", mue);
                            }
                        }

                        extension.setCategories((List<String>) metaData.getOrDefault(CATEGORIES, null));
                        extension.setStatus((String) metaData.getOrDefault(STATUS, null));
                        extension.setBuiltWith((String) metaData.getOrDefault(BUILT_WITH, null));
                        extension.setConfigFilter((List<String>) metaData.getOrDefault(CONFIG, null));
                        extension.setExtensionDependencies((List<String>) metaData.getOrDefault(EXTENSION_DEPENDENCIES, null));
                        extension.setUnlisted((boolean) metaData.getOrDefault(UNLISTED, false));

                        if (metaData.containsKey(CAPABILITIES)) {
                            Map<String, Object> capabilities = (Map<String, Object>) metaData.get(CAPABILITIES);
                            extension.setConfigFilter((List<String>) capabilities.getOrDefault(PROVIDES, null));
                        }

                        if (metaData.containsKey(CODESTART)) {
                            Map<String, Object> codestartMap = (Map<String, Object>) metaData.get(metaData);
                            if (codestartMap != null) {
                                Codestart codestart = new Codestart();
                                codestart.setName((String) codestartMap.getOrDefault(NAME, null));
                                codestart.setLanguages((List<String>) codestartMap.getOrDefault(LANGUAGES, null));
                                codestart.setArtifact((String) codestartMap.getOrDefault(ARTIFACT, null));
                                extension.setCodestart(codestart);
                            }
                        }

                        // Check for active cards
                        if (activeExtensionsMap.containsKey(name.toLowerCase())) {
                            DevUICardLinksBuildItem cardLinksBuildItem = activeExtensionsMap.get(name.toLowerCase());
                            List<CardLink> cardLinks = cardLinksBuildItem.getLinks();

                            List<Link> links = new ArrayList<>();
                            for (CardLink cardLink : cardLinks) {
                                links.add(new Link(cardLink.getIconName(),
                                        cardLink.getDisplayName(),
                                        cardLink.getLabel(),
                                        cardLink.getComponent(),
                                        cardLink.getPath()));
                            }
                            extension.setLinks(links);
                            activeExtensions.add(extension);

                            // Also make sure the static resources for that static resource is available
                            GACT gact = getGACT(artifactId);
                            webJarBuildProducer.produce(WebJarBuildItem.builder()
                                    .artifactKey(gact)
                                    .root(DEVUI + SLASH + cardLinksBuildItem.getExtensionPathName() + SLASH).build());

                            devUIWebJarProducer.produce(
                                    new DevUIWebJarBuildItem(gact, DEVUI + SLASH + cardLinksBuildItem.getExtensionPathName()));
                        } else {
                            inactiveExtensions.add(extension);
                        }
                    }

                    Collections.sort(activeExtensions, sortingComparator);
                    Collections.sort(inactiveExtensions, sortingComparator);
                } catch (IOException | RuntimeException e) {
                    // don't abort, just log, to prevent a single extension from breaking entire dev ui
                    log.error("Failed to process extension descriptor " + p.toUri(), e);
                }
            });
            extensionsProducer.produce(new ExtensionsBuildItem(activeExtensions, inactiveExtensions));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void createAllRoutes(WebJarResultsBuildItem webJarResultsBuildItem,
            List<DevUIWebJarBuildItem> devUIWebJarBuiltItems,
            BuildProducer<DevUIRoutesBuildItem> devUIRoutesProducer) {

        for (DevUIWebJarBuildItem devUIWebJarBuiltItem : devUIWebJarBuiltItems) {
            WebJarResultsBuildItem.WebJarResult result = webJarResultsBuildItem
                    .byArtifactKey(devUIWebJarBuiltItem.getArtifactKey());
            if (result != null) {
                devUIRoutesProducer.produce(new DevUIRoutesBuildItem(devUIWebJarBuiltItem.getPath(),
                        result.getFinalDestination(), result.getWebRootConfigurations()));
            }
        }
    }

    private GACT getGACT(String artifactKey) {
        String[] split = artifactKey.split(DOUBLE_POINT);
        return new GACT(split[0], split[1] + DASH_DEPLOYMENT, null, JAR);
    }

    private Map<String, DevUICardLinksBuildItem> getActiveExtensionsMap(List<DevUICardLinksBuildItem> activeCards) {
        Map<String, DevUICardLinksBuildItem> activeExtensionsMap = new HashMap<>();
        for (DevUICardLinksBuildItem activeCard : activeCards) {
            activeExtensionsMap.put(activeCard.getExtensionName().toLowerCase(), activeCard);
        }
        return activeExtensionsMap;
    }

    private String getExtensionNamespace(Map<String, Object> extensionMap) {
        final String groupId;
        final String artifactId;
        final String artifact = (String) extensionMap.get("artifact");
        if (artifact == null) {
            // trying quarkus 1.x format
            groupId = (String) extensionMap.get("group-id");
            artifactId = (String) extensionMap.get("artifact-id");
            if (artifactId == null || groupId == null) {
                throw new RuntimeException(
                        "Failed to locate 'artifact' or 'group-id' and 'artifact-id' among metadata keys "
                                + extensionMap.keySet());
            }
        } else {
            final GACTV coords = GACTV.fromString(artifact);
            groupId = coords.getGroupId();
            artifactId = coords.getArtifactId();
        }
        return groupId + "." + artifactId;
    }

    // Sort extensions with Guide first and then alphabetical
    private final Comparator sortingComparator = new Comparator<Extension>() {
        @Override
        public int compare(Extension t, Extension t1) {
            if (t.getGuide() != null && t1.getGuide() != null) {
                return t.getName().compareTo(t1.getName());
            } else if (t.getGuide() == null) {
                return 1;
            } else {
                return -1;
            }
        }
    };

    private static final String NAME = "name";
    private static final String DESCRIPTION = "description";
    private static final String ARTIFACT = "artifact";
    private static final String METADATA = "metadata";
    private static final String KEYWORDS = "keywords";
    private static final String SHORT_NAME = "short-name";
    private static final String GUIDE = "guide";
    private static final String CATEGORIES = "categories";
    private static final String STATUS = "status";
    private static final String BUILT_WITH = "built-with-quarkus-core";
    private static final String CONFIG = "config";
    private static final String EXTENSION_DEPENDENCIES = "extension-dependencies";
    private static final String CAPABILITIES = "capabilities";
    private static final String PROVIDES = "provides";
    private static final String UNLISTED = "unlisted";
    private static final String CODESTART = "codestart";
    private static final String LANGUAGES = "languages";

    private static final String YAML_FILE = "/META-INF/quarkus-extension.yaml";

}
