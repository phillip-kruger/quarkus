package io.quarkus.vertx.http.deployment.devmode.ui;

import java.io.IOException;
import java.nio.file.Path;

import io.quarkus.deployment.IsDevelopment;
import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.pkg.builditem.CurateOutcomeBuildItem;
import io.quarkus.maven.dependency.ResolvedDependency;
import io.quarkus.paths.PathTree;
import io.quarkus.vertx.http.deployment.NonApplicationRootPathBuildItem;
import io.quarkus.vertx.http.deployment.RouteBuildItem;
import io.quarkus.vertx.http.runtime.devmode.ui.DevUIRecorder;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Processor for Dev UI
 */
public class DevUIProcessor {

    @BuildStep(onlyIf = IsDevelopment.class)
    @Record(ExecutionTime.RUNTIME_INIT)
    void registerDevUiHandler(
            BuildProducer<RouteBuildItem> routeProducer,
            DevUIRecorder recorder,
            CurateOutcomeBuildItem curateOutcomeBuildItem,
            NonApplicationRootPathBuildItem nonApplicationRootPathBuildItem) throws IOException {

        ResolvedDependency resolvedDependency = getResolvedDependency(curateOutcomeBuildItem, "io.quarkus",
                "quarkus-vertx-http-deployment", "jar");

        Path root = getRoot(resolvedDependency);

        String relativeRootPath = nonApplicationRootPathBuildItem.resolvePath(DEV);

        System.out.println(">>>>>>> relativeRootPath = " + relativeRootPath);
        System.out.println(">>>>>>> Path = " + root);

        Handler<RoutingContext> handler = recorder.uiHandler(root.toUri().toString(), "dev-ui", relativeRootPath);

        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .route(DEV)
                .displayOnNotFoundPage("Dev UI 2.0")
                .handler(handler)
                .build());

        routeProducer.produce(nonApplicationRootPathBuildItem.routeBuilder()
                .route(DEV + "/*")
                .handler(handler)
                .build());

    }

    private Path getRoot(ResolvedDependency dependency) {
        PathTree contentTree = dependency.getContentTree();

        return contentTree.apply("dev-ui", (t) -> {
            return t.getPath();
        });
    }

    private ResolvedDependency getResolvedDependency(CurateOutcomeBuildItem curateOutcomeBuildItem, String groupId,
            String artifactId, String type) {
        for (ResolvedDependency dep : curateOutcomeBuildItem.getApplicationModel().getDependencies()) {
            if (dep.getKey().getGroupId().equals(groupId) && dep.getKey().getArtifactId().equals(artifactId)
                    && dep.getKey().getType().equals(type)) {
                return dep;
            }
        }
        return null;
    }

    private static final String DEV = "dev2";
}
