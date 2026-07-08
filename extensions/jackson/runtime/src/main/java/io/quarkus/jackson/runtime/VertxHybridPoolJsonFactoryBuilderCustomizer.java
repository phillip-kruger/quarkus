package io.quarkus.jackson.runtime;

import java.io.InputStream;

import io.quarkus.jackson.JsonFactoryBuilderCustomizer;
import io.quarkus.runtime.ImageMode;
import tools.jackson.core.json.JsonFactoryBuilder;
import tools.jackson.core.util.RecyclerPool;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class VertxHybridPoolJsonFactoryBuilderCustomizer implements JsonFactoryBuilderCustomizer {

    @Override
    public void customize(JsonFactoryBuilder builder) {
        try {
            Class<?> poolClass;
            if (ImageMode.current().isNativeImage()) {
                // in native mode, we can't use a custom ClassLoader and moreover we don't support Java 17 anyway
                poolClass = Class.forName("io.vertx.core.json.jackson.v3.HybridJacksonPool");
            } else {
                ClassLoader tccl = Thread.currentThread().getContextClassLoader();
                // HybridJacksonPool lives in META-INF/versions/21/ of vertx-core (MR JAR)..
                MrJarClassLoader loader = new MrJarClassLoader(tccl);
                try {
                    poolClass = loader.loadClass("io.vertx.core.json.jackson.v3.HybridJacksonPool");
                } finally {
                    loader = null;
                }
            }
            Object pool = poolClass.getMethod("getInstance").invoke(null);
            builder.recyclerPool((RecyclerPool) pool);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create instance of `io.vertx.core.json.jackson.v3.HybridJacksonPool`",
                    e);
        }
    }

    private static class MrJarClassLoader extends ClassLoader {

        MrJarClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        protected Class<?> findClass(String name) throws ClassNotFoundException {
            if (name.startsWith("io.vertx.core.json.jackson.v3.")) {
                String path = "META-INF/versions/21/" + name.replace('.', '/') + ".class";
                try (InputStream is = getParent().getResourceAsStream(path)) {
                    if (is != null) {
                        byte[] bytes = is.readAllBytes();
                        return defineClass(name, bytes, 0, bytes.length);
                    }
                } catch (java.io.IOException ex) {
                    throw new ClassNotFoundException(name, ex);
                }
            }
            throw new ClassNotFoundException(name);
        }
    }
}
