package io.quarkus.devui.deployment.mcp;

import java.io.IOException;
import java.util.List;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.builditem.LaunchModeBuildItem;
import io.quarkus.devui.deployment.InternalPageBuildItem;
import io.quarkus.devui.runtime.DevUIRecorder;
import io.quarkus.devui.runtime.mcp.MCPResourcesService;
import io.quarkus.devui.runtime.mcp.MCPToolsService;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;
import io.quarkus.devui.spi.page.Page;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;

public class MCPProcessor {

    private static final String DEVMCP = "dev-mcp";
    private static final String SLASH = "/";
    private static final String JSONRPC = "json-rpc-sse";
    private static final String DOT_WELL_KNOWN = ".well-known";
    private static final String MCP_CAPABILITIES = "mcp-capabilities";

    private static final String NS_MCP = "mcp";
    private static final String NS_RESOURCES = "resources";
    private static final String NS_TOOLS = "tools";

    @BuildStep(onlyIf = IsDevelopment.class)
    InternalPageBuildItem createMCPPage(NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) {
        InternalPageBuildItem mcpServerPage = new InternalPageBuildItem("MCP Server", 28);

        // Pages
        mcpServerPage.addPage(Page.webComponentPageBuilder()
                .namespace(NS_MCP)
                .title("MCP Server")
                .icon("font-awesome-solid:robot")
                .componentLink("qwc-mcp-server.js"));

        mcpServerPage.addPage(Page.webComponentPageBuilder()
                .namespace(NS_MCP)
                .title("Tools")
                .icon("font-awesome-solid:screwdriver-wrench")
                .componentLink("qwc-mcp-tools.js"));

        mcpServerPage.addPage(Page.webComponentPageBuilder()
                .namespace(NS_MCP)
                .title("Resources")
                .componentLink("qwc-mcp-resources.js"));

        return mcpServerPage;
    }

    @BuildStep(onlyIf = IsDevelopment.class)
    @io.quarkus.deployment.annotations.Record(ExecutionTime.STATIC_INIT)
    void registerDevUiHandlers(
            BuildProducer<RouteBuildItem> routeProducer,
            DevUIRecorder recorder,
            LaunchModeBuildItem launchModeBuildItem,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) throws IOException {

        if (launchModeBuildItem.isNotLocalDevModeType()) {
            return;
        }

        String mcpPath = nonApplicationRootPathBuildItem.resolvePath(DEVMCP);

        // SSE for JsonRPC comms
        routeProducer.produce(
                nonApplicationRootPathBuildItem
                        .routeBuilder().route(DEVMCP)
                        .handler(recorder.serverSendEventHandler())
                        .build());

        // mcp-capabilities for MCP
        routeProducer.produce(
                nonApplicationRootPathBuildItem
                        .routeBuilder().route(DEVMCP + SLASH + DOT_WELL_KNOWN + SLASH + MCP_CAPABILITIES)
                        .handler(recorder.mcpWellKnownHandler(mcpPath))
                        .build());

    }

    @BuildStep(onlyIf = IsDevelopment.class)
    void createMCPJsonRPCService(BuildProducer<JsonRPCProvidersBuildItem> bp) {
        bp.produce(List.of(
                new JsonRPCProvidersBuildItem(NS_RESOURCES, MCPResourcesService.class),
                new JsonRPCProvidersBuildItem(NS_TOOLS, MCPToolsService.class)));
    }
}
