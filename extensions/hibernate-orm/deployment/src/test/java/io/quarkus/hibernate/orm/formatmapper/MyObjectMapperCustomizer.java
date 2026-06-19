package io.quarkus.hibernate.orm.formatmapper;

import jakarta.inject.Singleton;

import io.quarkus.jackson.JsonMapperBuilderCustomizer;
import tools.jackson.databind.json.JsonMapper;

@Singleton
public class MyObjectMapperCustomizer implements JsonMapperBuilderCustomizer {
    @Override
    public void customize(JsonMapper.Builder builder) {
        // we don't really have to do anything here, it is enough that we have the customizer...
    }
}
