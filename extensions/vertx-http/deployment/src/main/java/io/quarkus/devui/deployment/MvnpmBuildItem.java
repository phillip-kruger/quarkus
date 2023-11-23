package io.quarkus.devui.deployment;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.builder.item.SimpleBuildItem;

/**
 * All mvnpm jars used by Dev UI
 */
public final class MvnpmBuildItem extends SimpleBuildItem {
    private final Set<URL> mvnpmJars;

    public MvnpmBuildItem(Set<URL> mvnpmJars) {
        this.mvnpmJars = mvnpmJars;
    }

    public Set<URL> getMvnpmJars() {
        return mvnpmJars;
    }

    public List<Path> getMvnpmPaths() {
        if (this.mvnpmJars != null) {
            return mvnpmJars.stream()
                    .map(url -> {
                        try {
                            return Paths.get(url.toURI());
                        } catch (URISyntaxException ex) {
                            ex.printStackTrace();
                            return null;
                        }
                    })
                    .filter(path -> path != null)
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
