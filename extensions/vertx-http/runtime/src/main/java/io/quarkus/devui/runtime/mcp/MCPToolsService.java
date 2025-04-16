package io.quarkus.devui.runtime.mcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import io.quarkus.devui.runtime.comms.JsonRpcRouter;

/**
 * This expose all Dev UI JsonRPC Methods as Tools
 */
@ApplicationScoped
public class MCPToolsService {

    @Inject
    JsonRpcRouter jsonRpcRouter;

    public Map<String, List<Tool>> list() {

        List<Tool> tools = new ArrayList<>();
        tools.addAll(toToolList(jsonRpcRouter.getRuntimeMethods()));
        tools.addAll(toToolList(jsonRpcRouter.getDeploymentMethods()));
        tools.addAll(toToolList(jsonRpcRouter.getRecordedMethods()));

        return Map.of("tools", tools);
    }

    private List<Tool> toToolList(Set<String> methods) {
        List<Tool> tools = new ArrayList<>();

        for (String method : methods) {
            Tool tool = new Tool();
            tool.name = method;
            // TODO: Add params
            tools.add(tool);
        }
        return tools;
    }

}
