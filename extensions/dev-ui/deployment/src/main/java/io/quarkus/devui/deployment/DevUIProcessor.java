package io.quarkus.devui.deployment;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.inject.Singleton;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.Type;
import org.jboss.logging.Logger;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.deployment.SyntheticBeanBuildItem;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.AdditionalIndexedClassesBuildItem;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.devui.deployment.spi.page.Page;
import io.quarkus.devui.deployment.spi.page.PageBuildItem;
import io.quarkus.devui.deployment.spi.page.PageBuilder;
import io.quarkus.devui.deployment.spi.page.QuteDataPageBuilder;
import io.quarkus.devui.deployment.spi.runtime.JsonRPCProvidersBuildItem;
import io.quarkus.devui.runtime.DevUIRecorder;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcMethodName;
import io.quarkus.devui.runtime.jsonrpc.JsonRpcRouter;
import io.quarkus.devui.runtime.service.extension.Codestart;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.maven.dependency.GACT;
import io.quarkus.maven.dependency.GACTV;
import io.quarkus.qute.Qute;
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
    private static final String SPACE = " ";
    private static final String DASH = "-";
    private static final String DOUBLE_POINT = ":";
    private static final String DASH_DEPLOYMENT = "-deployment";
    private static final String CONSTRUCTOR = "<init>";

    private final ClassLoader tccl = Thread.currentThread().getContextClassLoader();

    @BuildStep(onlyIf = IsDevelopment.class)
    void additionalBean(BuildProducer<AdditionalBeanBuildItem> additionalBeanProducer,
            BuildProducer<AdditionalIndexedClassesBuildItem> additionalIndexProducer,
            List<JsonRPCProvidersBuildItem> jsonRPCProvidersBuildItems) {
        additionalBeanProducer.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(JsonRpcRouter.class)
                .setUnremovable().build());

        // Make sure all JsonRPC Providers is in the index
        for (JsonRPCProvidersBuildItem jsonRPCProvidersBuildItem : jsonRPCProvidersBuildItems) {

            Class c = jsonRPCProvidersBuildItem.getJsonRPCMethodProviderClass();
            additionalIndexProducer.produce(new AdditionalIndexedClassesBuildItem(c.getName()));
        }
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void findAllJsonRPCMethods(BuildProducer<JsonRPCMethodsBuildItem> jsonRPCMethodsProvider,
            CombinedIndexBuildItem combinedIndexBuildItem,
            List<JsonRPCProvidersBuildItem> jsonRPCProvidersBuildItems) {

        IndexView index = combinedIndexBuildItem.getIndex();

        Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap = new HashMap<>();

        // Let's use the Jandex index to find all methods
        for (JsonRPCProvidersBuildItem jsonRPCProvidersBuildItem : jsonRPCProvidersBuildItems) {

            Class clazz = jsonRPCProvidersBuildItem.getJsonRPCMethodProviderClass();
            String extension = jsonRPCProvidersBuildItem.getExtensionName();

            Map<JsonRpcMethodName, JsonRpcMethod> jsonRpcMethods = null;
            if (extensionMethodsMap.containsKey(extension)) {
                jsonRpcMethods = extensionMethodsMap.get(extension);
            } else {
                jsonRpcMethods = new HashMap<>();
            }

            ClassInfo classInfo = index.getClassByName(DotName.createSimple(clazz.getName()));

            List<MethodInfo> methods = classInfo.methods();

            for (MethodInfo method : methods) {
                if (!method.name().equals(CONSTRUCTOR)) { // Ignore constructor
                    if (Modifier.isPublic(method.flags())) { // Only allow public methods

                        JsonRpcMethodName jsonRpcMethodName = new JsonRpcMethodName(method.name());
                        if (method.parametersCount() > 0) {
                            Map<String, Class> params = new LinkedHashMap<>(); // Keep the order
                            for (int i = 0; i < method.parametersCount(); i++) {
                                Type parameterType = method.parameterType(i);
                                Class parameterClass = toClass(parameterType);
                                String parameterName = method.parameterName(i);
                                params.put(parameterName, parameterClass);
                            }
                            JsonRpcMethod jsonRpcMethod = new JsonRpcMethod(clazz, method.name(), params);
                            jsonRpcMethods.put(jsonRpcMethodName, jsonRpcMethod);
                        } else {
                            JsonRpcMethod jsonRpcMethod = new JsonRpcMethod(clazz, method.name(), null);
                            jsonRpcMethods.put(jsonRpcMethodName, jsonRpcMethod);
                        }
                    }
                }
            }

            if (!jsonRpcMethods.isEmpty()) {
                extensionMethodsMap.put(extension, jsonRpcMethods);
            }
        }

        if (!extensionMethodsMap.isEmpty()) {
            jsonRPCMethodsProvider.produce(new JsonRPCMethodsBuildItem(extensionMethodsMap));
        }
    }

    private Class toClass(Type type) {
        try {
            return tccl.loadClass(type.name().toString());
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.STATIC_INIT)
    void scopeJsonRPCProviders(DevUIRecorder recorder,
            List<JsonRPCProvidersBuildItem> jsonRPCProvidersBuildItems,
            BuildProducer<SyntheticBeanBuildItem> syntheticBeanProducer) {

        for (JsonRPCProvidersBuildItem jsonRPCProvidersBuildItem : jsonRPCProvidersBuildItems) {
            Class c = jsonRPCProvidersBuildItem.getJsonRPCMethodProviderClass();
            SyntheticBeanBuildItem sbbi = SyntheticBeanBuildItem.configure(c)
                    .scope(Singleton.class)
                    .runtimeValue(recorder.createJsonRpcProvider(c))
                    .unremovable()
                    .done();
            syntheticBeanProducer.produce(sbbi);

        }
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.STATIC_INIT)
    void createJsonRpcRouter(DevUIRecorder recorder,
            BeanContainerBuildItem beanContainer,
            JsonRPCMethodsBuildItem jsonRPCMethodsBuildItem) {

        Map<String, Map<JsonRpcMethodName, JsonRpcMethod>> extensionMethodsMap = jsonRPCMethodsBuildItem
                .getExtensionMethodsMap();

        // TODO: Can JsonRPCRouter be in development ?
        recorder.createJsonRpcRouter(beanContainer.getValue(), extensionMethodsMap);

    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @SuppressWarnings("unchecked")
    void getAllExtensions(List<PageBuildItem> pageBuildItems,
            BuildProducer<ExtensionsBuildItem> extensionsProducer,
            BuildProducer<WebJarBuildItem> webJarBuildProducer,
            BuildProducer<DevUIWebJarBuildItem> devUIWebJarProducer) {

        // First create the static resources for our own internal components
        webJarBuildProducer.produce(WebJarBuildItem.builder()
                .artifactKey(UI_JAR)
                .root(DEVUI + SLASH).build());

        devUIWebJarProducer.produce(new DevUIWebJarBuildItem(UI_JAR, DEVUI));

        // Now go through all extensions and check them for active components
        Map<String, PageBuildItem> pagesMap = getPagesMap(pageBuildItems);

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
                                log.warn("Could not set Guide URL [" + guide + "] for exception [" + name + "]");
                            }
                        }

                        extension.setCategories((List<String>) metaData.getOrDefault(CATEGORIES, null));
                        extension.setStatus((String) metaData.getOrDefault(STATUS, null));
                        extension.setBuiltWith((String) metaData.getOrDefault(BUILT_WITH, null));
                        extension.setConfigFilter((List<String>) metaData.getOrDefault(CONFIG, null));
                        extension.setExtensionDependencies((List<String>) metaData.getOrDefault(EXTENSION_DEPENDENCIES, null));
                        String sboolean = String.valueOf(metaData.getOrDefault(UNLISTED, "false")); // Some yaml entries are strings :(
                        extension.setUnlisted(Boolean.valueOf(sboolean));

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

                        String nameKey = name.toLowerCase().replaceAll(SPACE, DASH);
                        // Inactive card
                        if (!pagesMap.containsKey(nameKey)) {
                            inactiveExtensions.add(extension);
                        } else {
                            PageBuildItem pageBuildItem = pagesMap.get(nameKey);
                            List<PageBuilder> pageBuilders = pageBuildItem.getPages();

                            for (PageBuilder pageBuilder : pageBuilders) {
                                Map<String, Object> buildTimeData = pageBuildItem.getBuildTimeData();
                                Page page = buildFinalPage(pageBuilder, extension, buildTimeData);
                                extension.addPage(page);
                            }

                            // Also make sure the static resources for that static resource is available
                            GACT gact = getGACT(artifactId);
                            webJarBuildProducer.produce(WebJarBuildItem.builder()
                                    .artifactKey(gact)
                                    .root(DEVUI + SLASH + pageBuildItem.getExtensionPathName() + SLASH).build());

                            devUIWebJarProducer.produce(
                                    new DevUIWebJarBuildItem(gact,
                                            DEVUI + SLASH + pageBuildItem.getExtensionPathName()));

                            activeExtensions.add(extension);
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

    private Page buildFinalPage(PageBuilder pageBuilder, Extension extension, Map<String, Object> buildTimeData) {
        pageBuilder.namespace(extension.getPathName());
        pageBuilder.extension(extension.getName());

        // TODO: Have a nice factory way to load this...
        // Some preprocessing for certain builds
        if (pageBuilder.getClass().equals(QuteDataPageBuilder.class)) {
            return buildQutePage(pageBuilder, extension, buildTimeData);
        }

        return pageBuilder.build();
    }

    private Page buildQutePage(PageBuilder pageBuilder, Extension extension, Map<String, Object> buildTimeData) {
        try {
            QuteDataPageBuilder quteDataPageBuilder = (QuteDataPageBuilder) pageBuilder;
            String templatePath = quteDataPageBuilder.getTemplatePath();
            ClassPathUtils.consumeAsPaths(templatePath, p -> {
                try {
                    String template = Files.readString(p);
                    String fragment = Qute.fmt(template, buildTimeData);
                    pageBuilder.metadata("htmlFragment", fragment);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return pageBuilder.build();
    }

    private GACT getGACT(String artifactKey) {
        String[] split = artifactKey.split(DOUBLE_POINT);
        return new GACT(split[0], split[1] + DASH_DEPLOYMENT, null, JAR);
    }

    private Map<String, PageBuildItem> getPagesMap(List<PageBuildItem> pages) {
        Map<String, PageBuildItem> m = new HashMap<>();
        for (PageBuildItem pageBuildItem : pages) {
            m.put(pageBuildItem.getExtensionPathName(), pageBuildItem);
        }
        return m;
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
