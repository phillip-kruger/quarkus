package io.quarkus.devui.deployment.jsonrpc;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.junit.platform.engine.UniqueId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import io.quarkus.deployment.dev.testing.TestResult;
import io.quarkus.devui.runtime.jsonrpc.json.JsonMapper;
import io.quarkus.devui.runtime.jsonrpc.json.JsonTypeAdapter;
import io.quarkus.vertx.runtime.jackson.ByteArrayDeserializer;
import io.quarkus.vertx.runtime.jackson.ByteArraySerializer;
import io.quarkus.vertx.runtime.jackson.InstantDeserializer;
import io.quarkus.vertx.runtime.jackson.InstantSerializer;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.EncodeException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.json.JsonReadFeature;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper.Builder;
import tools.jackson.databind.module.SimpleModule;

public class DevUIDatabindCodec implements JsonMapper {
    private final ObjectMapper mapper;
    private volatile ObjectMapper prettyMapper;
    private final Function<Map<String, Object>, ?> runtimeObjectDeserializer;
    private final Function<List<?>, ?> runtimeArrayDeserializer;

    private DevUIDatabindCodec(ObjectMapper mapper,
            Function<Map<String, Object>, ?> runtimeObjectDeserializer,
            Function<List<?>, ?> runtimeArrayDeserializer) {
        this.mapper = mapper;
        this.runtimeObjectDeserializer = runtimeObjectDeserializer;
        this.runtimeArrayDeserializer = runtimeArrayDeserializer;
    }

    private ObjectMapper prettyMapper() {
        if (prettyMapper == null) {
            prettyMapper = ((tools.jackson.databind.json.JsonMapper) mapper).rebuild()
                    .configure(SerializationFeature.INDENT_OUTPUT, true)
                    .build();
        }
        return prettyMapper;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T fromValue(Object json, Class<T> clazz) {
        T value = mapper.convertValue(json, clazz);
        if (clazz == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public <T> T fromString(String str, Class<T> clazz) throws DecodeException {
        return fromParser(createParser(str), clazz);
    }

    private JsonParser createParser(String str) {
        return mapper.createParser(str);
    }

    @SuppressWarnings("unchecked")
    private <T> T fromParser(JsonParser parser, Class<T> type) throws DecodeException {
        T value;
        JsonToken remaining;
        try {
            value = mapper.readValue(parser, type);
            remaining = parser.nextToken();
        } catch (Exception e) {
            throw new DecodeException("Failed to decode:" + e.getMessage(), e);
        } finally {
            close(parser);
        }
        if (remaining != null) {
            throw new DecodeException("Unexpected trailing token");
        }
        if (type == Object.class) {
            value = (T) adapt(value);
        }
        return value;
    }

    @Override
    public String toString(Object object, boolean pretty) throws EncodeException {
        try {
            ObjectMapper theMapper = pretty ? prettyMapper() : mapper;
            return theMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new EncodeException("Failed to encode as JSON: " + e.getMessage(), e);
        }
    }

    private static void close(Closeable parser) {
        try {
            parser.close();
        } catch (IOException ignore) {
        }
    }

    private Object adapt(Object o) {
        try {
            if (o instanceof List) {
                List<?> list = (List<?>) o;
                return runtimeArrayDeserializer.apply(list);
            } else if (o instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) o;
                return runtimeObjectDeserializer.apply(map);
            }
            return o;
        } catch (Exception e) {
            throw new DecodeException("Failed to decode: " + e.getMessage());
        }
    }

    public static final class Factory implements JsonMapper.Factory {
        @Override
        public JsonMapper create(JsonTypeAdapter<?, Map<String, Object>> jsonObjectAdapter,
                JsonTypeAdapter<?, List<?>> jsonArrayAdapter, JsonTypeAdapter<?, String> bufferAdapter) {
            // We want our own mapper, separate from the user-configured one.
            Builder builder = tools.jackson.databind.json.JsonMapper.builder();

            // Non-standard JSON but we allow C style comments in our JSON
            builder.configure(JsonReadFeature.ALLOW_JAVA_COMMENTS, true);
            builder.changeDefaultPropertyInclusion(new UnaryOperator<>() {
                @Override
                public JsonInclude.Value apply(JsonInclude.Value value) {
                    return value.withValueInclusion(JsonInclude.Include.NON_NULL);
                }
            });

            builder.addMixIn(TestResult.class, TestResultMixIn.class);
            SimpleModule module = new SimpleModule("vertx-module-common");
            module.addSerializer(Instant.class, new InstantSerializer());
            module.addDeserializer(Instant.class, new InstantDeserializer());
            module.addSerializer(byte[].class, new ByteArraySerializer());
            module.addDeserializer(byte[].class, new ByteArrayDeserializer());
            module.addSerializer(ByteArrayInputStream.class, new ByteArrayInputStreamSerializer());
            module.addDeserializer(ByteArrayInputStream.class, new ByteArrayInputStreamDeserializer());
            builder.addModule(module);

            SimpleModule runtimeModule = new SimpleModule("vertx-module-runtime");
            addAdapterToObject(runtimeModule, jsonObjectAdapter);
            addAdapterToObject(runtimeModule, jsonArrayAdapter);
            addAdapterToString(runtimeModule, bufferAdapter);
            builder.addModule(runtimeModule);

            ObjectMapper mapper = builder.build();
            return new DevUIDatabindCodec(mapper, jsonObjectAdapter.deserializer, jsonArrayAdapter.deserializer);
        }

        private static <T, S> void addAdapterToObject(SimpleModule module, JsonTypeAdapter<T, S> adapter) {
            module.addSerializer(adapter.type, new ValueSerializer<>() {
                @Override
                public void serialize(T value, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
                    jgen.writePOJO(adapter.serializer.apply(value));
                }
            });
        }

        private static <T> void addAdapterToString(SimpleModule module, JsonTypeAdapter<T, String> adapter) {
            module.addSerializer(adapter.type, new ValueSerializer<>() {
                @Override
                public void serialize(T value, JsonGenerator jgen, SerializationContext provider) throws JacksonException {
                    jgen.writeString(adapter.serializer.apply(value));
                }
            });
            module.addDeserializer(adapter.type, new ValueDeserializer<T>() {
                @Override
                public T deserialize(JsonParser parser, DeserializationContext ctxt) throws JacksonException {
                    return adapter.deserializer.apply(parser.getString());
                }
            });
        }
    }

    private interface TestResultMixIn {
        @JsonIgnore
        UniqueId getUniqueId();
    }
}
