package io.quarkus.produi.deployment;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.jboss.jandex.MethodInfo;
import org.jboss.jandex.MethodParameterInfo;
import org.jboss.jandex.Type;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.deployment.BeanContainerBuildItem;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.produi.runtime.ProdUIRecorder;
import io.quarkus.produi.runtime.comms.ProdUIJsonRpcRouter;
import io.quarkus.produi.runtime.config.ConfigJsonRpcService;
import io.quarkus.produi.runtime.jsonrpc.JsonRpcMethod;
import io.quarkus.produi.runtime.overview.OverviewJsonRpcService;
import io.quarkus.produi.spi.ProdUIJsonRpcProvidersBuildItem;
import io.quarkus.produi.spi.page.Page;
import io.quarkus.produi.spi.page.PageBuilder;
import io.quarkus.produi.spi.page.ProdUICardPageBuildItem;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.smallrye.mutiny.Multi;

/**
 * Deployment processor for Prod UI.
 */
public class ProdUIProcessor {

    private static final String CONFIG_KEY_PROD_UI_MANAGEMENT_ENABLED = "quarkus.prod-ui.management-enabled";
    private static final DotName MULTI = DotName.createSimple(Multi.class.getName());

    private static final String FEATURE_NAME = "prod-ui";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE_NAME);
    }

    @BuildStep
    void registerBeans(BuildProducer<AdditionalBeanBuildItem> additionalBeans) {
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(ProdUIJsonRpcRouter.class)
                .setUnremovable()
                .build());

        // Register built-in JSON-RPC services
        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(OverviewJsonRpcService.class)
                .setUnremovable()
                .build());

        additionalBeans.produce(AdditionalBeanBuildItem.builder()
                .addBeanClass(ConfigJsonRpcService.class)
                .setUnremovable()
                .build());
    }

    @BuildStep
    void registerBuiltInJsonRpcProviders(BuildProducer<ProdUIJsonRpcProvidersBuildItem> producer) {
        producer.produce(new ProdUIJsonRpcProvidersBuildItem("produi", OverviewJsonRpcService.class));
        producer.produce(new ProdUIJsonRpcProvidersBuildItem("produi", ConfigJsonRpcService.class));
    }

    @BuildStep
    void registerJsonRpcProviderBeans(
            List<ProdUIJsonRpcProvidersBuildItem> jsonRpcProviders,
            BuildProducer<AdditionalBeanBuildItem> additionalBeans) {

        for (ProdUIJsonRpcProvidersBuildItem provider : jsonRpcProviders) {
            DotName scope = provider.getDefaultBeanScope();
            if (scope == null) {
                scope = BuiltinScope.SINGLETON.getName();
            }
            additionalBeans.produce(AdditionalBeanBuildItem.builder()
                    .addBeanClass(provider.getJsonRpcMethodProviderClass())
                    .setDefaultScope(scope)
                    .setUnremovable()
                    .build());
        }
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerRoutes(
            ProdUIBuildTimeConfig buildTimeConfig,
            ProdUIRecorder recorder,
            BeanContainerBuildItem beanContainer,
            CombinedIndexBuildItem combinedIndex,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem,
            List<ProdUIJsonRpcProvidersBuildItem> jsonRpcProviders,
            List<ProdUICardPageBuildItem> cardPages,
            BuildProducer<RouteBuildItem> routeProducer) {

        if (!buildTimeConfig.enabled()) {
            return;
        }

        String rootPath = buildTimeConfig.rootPath();
        if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }
        if (!rootPath.endsWith("/")) {
            rootPath = rootPath + "/";
        }

        // Scan JSON-RPC methods
        IndexView index = combinedIndex.getIndex();
        Map<String, JsonRpcMethod> runtimeMethods = new HashMap<>();
        Map<String, JsonRpcMethod> runtimeSubscriptions = new HashMap<>();

        for (ProdUIJsonRpcProvidersBuildItem provider : jsonRpcProviders) {
            Class<?> providerClass = provider.getJsonRpcMethodProviderClass();
            ClassInfo classInfo = index.getClassByName(DotName.createSimple(providerClass.getName()));
            if (classInfo != null) {
                scanJsonRpcMethods(classInfo, providerClass, runtimeMethods, runtimeSubscriptions);
            }
        }

        // Initialize the JSON-RPC router
        recorder.createJsonRpcRouter(beanContainer.getValue(), runtimeMethods, runtimeSubscriptions);

        // Generate the index.html content
        String indexContent = generateIndexHtml(rootPath, cardPages);

        // Register WebSocket route for JSON-RPC
        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .management(CONFIG_KEY_PROD_UI_MANAGEMENT_ENABLED)
                .route(buildTimeConfig.rootPath() + "/json-rpc-ws")
                .handler(recorder.prodUIWebSocketHandler())
                .build());

        // Register static content route
        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .management(CONFIG_KEY_PROD_UI_MANAGEMENT_ENABLED)
                .route(buildTimeConfig.rootPath())
                .displayOnNotFoundPage("Prod UI")
                .handler(recorder.staticHandler(rootPath, indexContent))
                .build());

        // Also handle trailing slash
        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .management(CONFIG_KEY_PROD_UI_MANAGEMENT_ENABLED)
                .route(buildTimeConfig.rootPath() + "/")
                .handler(recorder.staticHandler(rootPath, indexContent))
                .build());

        // Handle index.html explicitly
        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .management(CONFIG_KEY_PROD_UI_MANAGEMENT_ENABLED)
                .route(buildTimeConfig.rootPath() + "/index.html")
                .handler(recorder.staticHandler(rootPath, indexContent))
                .build());
    }

    private void scanJsonRpcMethods(ClassInfo classInfo, Class<?> providerClass,
            Map<String, JsonRpcMethod> runtimeMethods,
            Map<String, JsonRpcMethod> runtimeSubscriptions) {

        String namespace = classInfo.simpleName();
        if (namespace.endsWith("JsonRpcService")) {
            namespace = namespace.substring(0, namespace.length() - "JsonRpcService".length());
        }
        namespace = namespace.toLowerCase();

        // Skip inherited methods from Object
        List<String> objectMethods = List.of("equals", "hashCode", "toString", "getClass", "notify", "notifyAll", "wait");

        for (MethodInfo method : classInfo.methods()) {
            String methodJavaName = method.name();
            // Skip constructor, static methods, non-public, and Object methods
            if (methodJavaName.equals("<init>") || objectMethods.contains(methodJavaName)) {
                continue;
            }
            if (Modifier.isPublic(method.flags()) && !Modifier.isStatic(method.flags())) {
                String methodName = namespace + "_" + method.name();

                JsonRpcMethod jsonRpcMethod = new JsonRpcMethod();
                jsonRpcMethod.setBean(providerClass);
                jsonRpcMethod.setMethodName(methodName);

                // Check for NonBlocking/Blocking annotations
                boolean isNonBlocking = method.hasAnnotation(DotName.createSimple("io.smallrye.common.annotation.NonBlocking"));
                boolean isBlocking = method.hasAnnotation(DotName.createSimple("io.smallrye.common.annotation.Blocking"));
                jsonRpcMethod.setExplicitlyNonBlocking(isNonBlocking);
                jsonRpcMethod.setExplicitlyBlocking(isBlocking);

                // Add parameters
                if (!method.parameters().isEmpty()) {
                    Map<String, JsonRpcMethod.Parameter> params = new LinkedHashMap<>();
                    for (MethodParameterInfo param : method.parameters()) {
                        String paramName = param.name();
                        if (paramName == null) {
                            // If parameter names are not available, use arg0, arg1, etc.
                            paramName = "arg" + param.position();
                        }
                        Class<?> paramType = getClassForType(param.type());
                        params.put(paramName, new JsonRpcMethod.Parameter(paramType));
                    }
                    jsonRpcMethod.setParameters(params);
                }

                // Check if this is a subscription (returns Multi)
                Type returnType = method.returnType();
                if (returnType.kind() == Type.Kind.PARAMETERIZED_TYPE
                        && returnType.asParameterizedType().name().equals(MULTI)) {
                    runtimeSubscriptions.put(methodName, jsonRpcMethod);
                } else {
                    runtimeMethods.put(methodName, jsonRpcMethod);
                }
            }
        }
    }

    private Class<?> getClassForType(Type type) {
        switch (type.kind()) {
            case PRIMITIVE:
                switch (type.asPrimitiveType().primitive()) {
                    case INT:
                        return int.class;
                    case LONG:
                        return long.class;
                    case BOOLEAN:
                        return boolean.class;
                    case DOUBLE:
                        return double.class;
                    case FLOAT:
                        return float.class;
                    case BYTE:
                        return byte.class;
                    case SHORT:
                        return short.class;
                    case CHAR:
                        return char.class;
                    default:
                        return Object.class;
                }
            case CLASS:
                String className = type.asClassType().name().toString();
                try {
                    return Class.forName(className);
                } catch (ClassNotFoundException e) {
                    return String.class;
                }
            default:
                return Object.class;
        }
    }

    private String generateIndexHtml(String rootPath, List<ProdUICardPageBuildItem> cardPages) {
        StringBuilder pagesJson = new StringBuilder("[");
        boolean first = true;

        for (ProdUICardPageBuildItem cardPage : cardPages) {
            for (PageBuilder<?> builder : cardPage.getPages()) {
                Page page = builder.build();
                if (!first) {
                    pagesJson.append(",");
                }
                first = false;
                pagesJson.append("{")
                        .append("\"id\":\"").append(escapeJson(page.getId())).append("\",")
                        .append("\"title\":\"").append(escapeJson(page.getTitle())).append("\",")
                        .append("\"icon\":\"").append(escapeJson(page.getIcon())).append("\"")
                        .append("}");
            }
        }
        pagesJson.append("]");

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Quarkus Prod UI</title>
                    <style>
                        :root {
                            --pui-primary: #4695eb;
                            --pui-background: #1a1a2e;
                            --pui-surface: #16213e;
                            --pui-text: #e8e8e8;
                            --pui-text-secondary: #a0a0a0;
                            --pui-border: #2d3748;
                        }
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        body {
                            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                            background: var(--pui-background);
                            color: var(--pui-text);
                            min-height: 100vh;
                        }
                        .header {
                            background: var(--pui-surface);
                            padding: 1rem 2rem;
                            border-bottom: 1px solid var(--pui-border);
                            display: flex;
                            align-items: center;
                            gap: 1rem;
                        }
                        .header h1 {
                            font-size: 1.5rem;
                            font-weight: 500;
                        }
                        .header .badge {
                            background: var(--pui-primary);
                            color: white;
                            padding: 0.25rem 0.75rem;
                            border-radius: 1rem;
                            font-size: 0.75rem;
                            font-weight: 600;
                        }
                        .main {
                            display: flex;
                            min-height: calc(100vh - 60px);
                        }
                        .sidebar {
                            width: 250px;
                            background: var(--pui-surface);
                            border-right: 1px solid var(--pui-border);
                            padding: 1rem;
                        }
                        .sidebar h2 {
                            font-size: 0.75rem;
                            text-transform: uppercase;
                            color: var(--pui-text-secondary);
                            margin-bottom: 1rem;
                            letter-spacing: 0.05em;
                        }
                        .sidebar nav a {
                            display: flex;
                            align-items: center;
                            gap: 0.75rem;
                            padding: 0.75rem;
                            color: var(--pui-text);
                            text-decoration: none;
                            border-radius: 0.5rem;
                            transition: background 0.2s;
                        }
                        .sidebar nav a:hover {
                            background: rgba(255,255,255,0.05);
                        }
                        .sidebar nav a.active {
                            background: rgba(70, 149, 235, 0.2);
                            color: var(--pui-primary);
                        }
                        .content {
                            flex: 1;
                            padding: 2rem;
                        }
                        .card {
                            background: var(--pui-surface);
                            border: 1px solid var(--pui-border);
                            border-radius: 0.5rem;
                            padding: 1.5rem;
                            margin-bottom: 1rem;
                        }
                        .card h3 {
                            font-size: 1rem;
                            margin-bottom: 0.5rem;
                        }
                        .card p {
                            color: var(--pui-text-secondary);
                            font-size: 0.875rem;
                        }
                        .status {
                            display: inline-flex;
                            align-items: center;
                            gap: 0.5rem;
                            padding: 0.25rem 0.75rem;
                            background: rgba(34, 197, 94, 0.2);
                            color: #22c55e;
                            border-radius: 1rem;
                            font-size: 0.75rem;
                            font-weight: 500;
                        }
                        .status::before {
                            content: '';
                            width: 6px;
                            height: 6px;
                            background: currentColor;
                            border-radius: 50%;
                        }
                        #ws-status {
                            font-size: 0.75rem;
                            color: var(--pui-text-secondary);
                        }
                    </style>
                </head>
                <body>
                    <header class="header">
                        <h1>Quarkus Prod UI</h1>
                        <span class="badge">Production</span>
                        <span id="ws-status">Connecting...</span>
                    </header>
                    <div class="main">
                        <aside class="sidebar">
                            <h2>Overview</h2>
                            <nav id="nav">
                                <a href="#overview" class="active">Overview</a>
                            </nav>
                        </aside>
                        <main class="content" id="content">
                            <div class="card">
                                <h3>Application Status</h3>
                                <p><span class="status">Running</span></p>
                            </div>
                            <div class="card">
                                <h3>Overview</h3>
                                <p>Application info will appear here once JSON-RPC connection is established.</p>
                                <div id="app-info"></div>
                            </div>
                        </main>
                    </div>
                    <script type="module">
                        const wsUrl = `${window.location.protocol === 'https:' ? 'wss:' : 'ws:'}//${window.location.host}%s/json-rpc-ws`;
                        let ws;
                        let messageId = 0;
                        const pending = new Map();
                        const statusEl = document.getElementById('ws-status');
                        const appInfoEl = document.getElementById('app-info');

                        function connect() {
                            ws = new WebSocket(wsUrl);
                            ws.onopen = () => {
                                statusEl.textContent = 'Connected';
                                statusEl.style.color = '#22c55e';
                                // Call overview method to get app info
                                callRpc('overview_getInfo').then(info => {
                                    if (info && info.object) {
                                        const data = info.object;
                                        appInfoEl.innerHTML = `
                                            <p style="margin-top: 1rem;"><strong>Name:</strong> ${data.name || 'N/A'}</p>
                                            <p><strong>Version:</strong> ${data.version || 'N/A'}</p>
                                            <p><strong>Quarkus:</strong> ${data.quarkusVersion || 'N/A'}</p>
                                        `;
                                    }
                                }).catch(err => console.log('Overview not available:', err));
                            };
                            ws.onclose = () => {
                                statusEl.textContent = 'Disconnected - Reconnecting...';
                                statusEl.style.color = '#ef4444';
                                setTimeout(connect, 3000);
                            };
                            ws.onerror = (err) => {
                                console.error('WebSocket error:', err);
                            };
                            ws.onmessage = (event) => {
                                const msg = JSON.parse(event.data);
                                if (pending.has(msg.id)) {
                                    const { resolve, reject } = pending.get(msg.id);
                                    pending.delete(msg.id);
                                    if (msg.error) {
                                        reject(msg.error);
                                    } else {
                                        resolve(msg.result);
                                    }
                                }
                            };
                        }

                        function callRpc(method, params = {}) {
                            return new Promise((resolve, reject) => {
                                const id = ++messageId;
                                pending.set(id, { resolve, reject });
                                ws.send(JSON.stringify({
                                    jsonrpc: '2.0',
                                    id,
                                    method,
                                    params
                                }));
                            });
                        }

                        connect();
                    </script>
                </body>
                </html>
                """.formatted(rootPath.endsWith("/") ? rootPath.substring(0, rootPath.length() - 1) : rootPath);
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
