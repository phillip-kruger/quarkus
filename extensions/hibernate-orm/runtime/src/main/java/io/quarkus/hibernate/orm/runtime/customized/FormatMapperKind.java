package io.quarkus.hibernate.orm.runtime.customized;

import java.util.Optional;

import jakarta.json.bind.Jsonb;

import org.hibernate.type.format.FormatMapper;
import org.hibernate.type.format.jackson.Jackson3JsonFormatMapper;
import org.hibernate.type.format.jakartajson.JsonBJsonFormatMapper;
import org.hibernate.type.format.jaxb.JaxbXmlFormatMapper;

import io.quarkus.arc.Arc;
import tools.jackson.databind.json.JsonMapper;

public enum FormatMapperKind {
    JACKSON {
        @Override
        public FormatMapper create() {
            return new Jackson3JsonFormatMapper(Arc.container().instance(JsonMapper.class).get());
        }

        @Override
        public Optional<String> requiredBeanType() {
            return Optional.of("tools.jackson.databind.json.JsonMapper");
        }
    },
    JSONB {
        @Override
        public FormatMapper create() {
            return new JsonBJsonFormatMapper(Arc.container().instance(Jsonb.class).get());
        }

        @Override
        public Optional<String> requiredBeanType() {
            return Optional.of("io.quarkus.jsonb.JsonbProducer");
        }
    },
    JAXB {
        @Override
        public FormatMapper create() {
            return new JaxbXmlFormatMapper();
        }

        @Override
        public Optional<String> requiredBeanType() {
            return Optional.empty();
        }
    };

    public abstract FormatMapper create();

    public abstract Optional<String> requiredBeanType();
}
