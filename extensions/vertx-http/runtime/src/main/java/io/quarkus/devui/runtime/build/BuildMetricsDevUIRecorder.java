package io.quarkus.devui.runtime.build;

import java.nio.file.Path;

import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class BuildMetricsDevUIRecorder {

    public void setBuildMetricsPath(String buildMetricsPath) {
        BuildMetricsDevUIController controller = BuildMetricsDevUIController.get();
        controller.setBuildMetricsPath(Path.of(buildMetricsPath));
        controller.getBuildStepsMetrics();
    }
}
