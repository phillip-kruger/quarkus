package io.quarkus.assistant.deployment;

import io.quarkus.deployment.Feature;
import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;

/**
 * The main Assistant processor
 */
public class AssistantProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    public void build(BuildProducer<FeatureBuildItem> feature) {
        feature.produce(new FeatureBuildItem(Feature.ASSISTANT));
    }

}
