package io.quarkus.devui.deployment.menu;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.devui.deployment.ExtensionsBuildItem;
import io.quarkus.devui.deployment.spi.buildtime.JsonRPCResponsesBuildItem;
import io.quarkus.devui.runtime.service.extension.Extension;
import io.quarkus.devui.runtime.service.extension.ExtensionGroup;

/**
 * Processor the get all the extensions
 */
public class ExtensionsProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    void createJsonRPCResponses(BuildProducer<JsonRPCResponsesBuildItem> jsonRPCResponsesProducer,
            ExtensionsBuildItem extensionsBuildItem) {

        Map<ExtensionGroup, List<Extension>> response = Map.of(
                ExtensionGroup.active, extensionsBuildItem.getActiveExtensions(),
                ExtensionGroup.inactive, extensionsBuildItem.getInactiveExtensions());

        Map<String, Object> methodNameAndResponseData = new HashMap<>();
        methodNameAndResponseData.put("getExtensions", response);

        jsonRPCResponsesProducer
                .produce(new JsonRPCResponsesBuildItem(JsonRPCResponsesBuildItem.INTERNAL, methodNameAndResponseData));

    }

}
