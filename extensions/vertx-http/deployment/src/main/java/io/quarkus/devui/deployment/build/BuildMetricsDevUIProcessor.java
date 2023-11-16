package io.quarkus.devui.deployment.build;

import static io.quarkus.deployment.annotations.ExecutionTime.RUNTIME_INIT;

import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.arc.processor.DotNames;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.BuildSteps;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;
import io.quarkus.deployment.pkg.builditem.BuildSystemTargetBuildItem;
import io.quarkus.devui.runtime.build.BuildMetricsDevUIRecorder;
import io.quarkus.devui.runtime.build.BuildMetricsJsonRPCService;
import io.quarkus.devui.spi.IsDevUI;
import io.quarkus.devui.spi.JsonRPCProvidersBuildItem;

@BuildSteps(onlyIf = { IsDevUI.class })
public class BuildMetricsDevUIProcessor {

    private static final String SYS_PROP = "quarkus.debug.dump-build-metrics";

    @BuildStep
    public SystemPropertyBuildItem dumpMetrics() {
        System.setProperty(SYS_PROP, "true");
        return new SystemPropertyBuildItem(SYS_PROP, "true");
    }

    @BuildStep
    @Record(RUNTIME_INIT)
    public void create(BuildMetricsDevUIRecorder recorder,
            BuildSystemTargetBuildItem buildSystemTarget) {
        recorder.setBuildMetricsPath(buildSystemTarget.getOutputDirectory().resolve("build-metrics.json").toString());
    }

    @BuildStep
    AdditionalBeanBuildItem additionalBeans() {
        return AdditionalBeanBuildItem
                .builder()
                .addBeanClass(BuildMetricsJsonRPCService.class)
                .setUnremovable()
                .setDefaultScope(DotNames.APPLICATION_SCOPED)
                .build();
    }

    @BuildStep
    JsonRPCProvidersBuildItem createJsonRPCService() {
        return new JsonRPCProvidersBuildItem("devui-build-metrics", BuildMetricsJsonRPCService.class);
    }
}
