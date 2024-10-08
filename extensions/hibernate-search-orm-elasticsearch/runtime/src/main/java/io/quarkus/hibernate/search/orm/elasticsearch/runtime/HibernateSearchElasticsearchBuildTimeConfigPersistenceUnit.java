package io.quarkus.hibernate.search.orm.elasticsearch.runtime;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.search.backend.elasticsearch.ElasticsearchVersion;

import io.quarkus.runtime.annotations.ConfigDocDefault;
import io.quarkus.runtime.annotations.ConfigDocMapKey;
import io.quarkus.runtime.annotations.ConfigDocSection;
import io.quarkus.runtime.annotations.ConfigGroup;
import io.smallrye.config.WithName;
import io.smallrye.config.WithParentName;
import io.smallrye.config.WithUnnamedKey;

@ConfigGroup
public interface HibernateSearchElasticsearchBuildTimeConfigPersistenceUnit {

    /**
     * Configuration for backends.
     */
    @ConfigDocSection
    @WithName("elasticsearch")
    @WithUnnamedKey // The default backend has the null key
    @ConfigDocMapKey("backend-name")
    Map<String, ElasticsearchBackendBuildTimeConfig> backends();

    /**
     * A xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean reference] to a component
     * that should be notified of any failure occurring in a background process
     * (mainly index operations).
     *
     * The referenced bean must implement `FailureHandler`.
     *
     * See
     * link:{hibernate-search-docs-url}#configuration-background-failure-handling[this section of the reference documentation]
     * for more information.
     *
     * [NOTE]
     * ====
     * Instead of setting this configuration property,
     * you can simply annotate your custom `FailureHandler` implementation with `@SearchExtension`
     * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
     * See xref:hibernate-search-orm-elasticsearch.adoc#plugging-in-custom-components[this section]
     * for more information.
     *
     * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
     * ====
     *
     * @asciidoclet
     */
    Optional<String> backgroundFailureHandler();

    /**
     * Configuration for coordination between threads or application instances.
     */
    CoordinationConfig coordination();

    /**
     * Configuration for mapping.
     */
    MappingConfig mapping();

    @ConfigGroup
    interface ElasticsearchBackendBuildTimeConfig {
        /**
         * The version of Elasticsearch used in the cluster.
         *
         * As the schema is generated without a connection to the server, this item is mandatory.
         *
         * It doesn't have to be the exact version (it can be `7` or `7.1` for instance) but it has to be sufficiently precise
         * to choose a model dialect (the one used to generate the schema) compatible with the protocol dialect (the one used
         * to communicate with Elasticsearch).
         *
         * There's no rule of thumb here as it depends on the schema incompatibilities introduced by Elasticsearch versions. In
         * any case, if there is a problem, you will have an error when Hibernate Search tries to connect to the cluster.
         *
         * @asciidoclet
         */
        Optional<ElasticsearchVersion> version();

        /**
         * The default configuration for the Elasticsearch indexes.
         */
        @WithParentName
        ElasticsearchIndexBuildTimeConfig indexDefaults();

        /**
         * Per-index configuration overrides.
         */
        @ConfigDocSection
        @ConfigDocMapKey("index-name")
        Map<String, ElasticsearchIndexBuildTimeConfig> indexes();
    }

    @ConfigGroup
    interface ElasticsearchIndexBuildTimeConfig {
        /**
         * Configuration for automatic creation and validation of the Elasticsearch schema:
         * indexes, their mapping, their settings.
         */
        SchemaManagementConfig schemaManagement();

        /**
         * Configuration for full-text analysis.
         */
        AnalysisConfig analysis();
    }

    @ConfigGroup
    interface SchemaManagementConfig {

        // @formatter:off
        /**
         * Path to a file in the classpath holding custom index settings to be included in the index definition
         * when creating an Elasticsearch index.
         *
         * The provided settings will be merged with those generated by Hibernate Search, including analyzer definitions.
         * When analysis is configured both through an analysis configurer and these custom settings, the behavior is undefined;
         * it should not be relied upon.
         *
         * See link:{hibernate-search-docs-url}#backend-elasticsearch-configuration-index-settings[this section of the reference documentation]
         * for more information.
         *
         * @asciidoclet
         */
        // @formatter:on
        Optional<String> settingsFile();

        // @formatter:off
        /**
         * Path to a file in the classpath holding a custom index mapping to be included in the index definition
         * when creating an Elasticsearch index.
         *
         * The file does not need to (and generally shouldn't) contain the full mapping:
         * Hibernate Search will automatically inject missing properties (index fields) in the given mapping.
         *
         * See link:{hibernate-search-docs-url}#backend-elasticsearch-mapping-custom[this section of the reference documentation]
         * for more information.
         *
         * @asciidoclet
         */
        // @formatter:on
        Optional<String> mappingFile();

    }

    @ConfigGroup
    interface AnalysisConfig {
        /**
         * One or more xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean references]
         * to the component(s) used to configure full text analysis (e.g. analyzers, normalizers).
         *
         * The referenced beans must implement `ElasticsearchAnalysisConfigurer`.
         *
         * See xref:hibernate-search-orm-elasticsearch.adoc#analysis-configurer[Setting up the analyzers] for more
         * information.
         *
         * [NOTE]
         * ====
         * Instead of setting this configuration property,
         * you can simply annotate your custom `ElasticsearchAnalysisConfigurer` implementations with `@SearchExtension`
         * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
         * See xref:hibernate-search-orm-elasticsearch.adoc#plugging-in-custom-components[this section]
         * for more information.
         *
         * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
         * ====
         *
         * @asciidoclet
         */
        Optional<List<String>> configurer();
    }

    @ConfigGroup
    interface CoordinationConfig {

        /**
         * The strategy to use for coordinating between threads or even separate instances of the application,
         * in particular in automatic indexing.
         *
         * See xref:hibernate-search-orm-elasticsearch.adoc#coordination[coordination] for more information.
         *
         * @asciidoclet
         */
        @ConfigDocDefault("none")
        Optional<String> strategy();
    }

    @ConfigGroup
    interface MappingConfig {
        /**
         * One or more xref:hibernate-search-orm-elasticsearch.adoc#bean-reference-note-anchor[bean references]
         * to the component(s) used to configure the Hibernate Search mapping,
         * in particular programmatically.
         *
         * The referenced beans must implement `HibernateOrmSearchMappingConfigurer`.
         *
         * See xref:hibernate-search-orm-elasticsearch.adoc#programmatic-mapping[Programmatic mapping] for an example
         * on how mapping configurers can be used to apply programmatic mappings.
         *
         * [NOTE]
         * ====
         * Instead of setting this configuration property,
         * you can simply annotate your custom `HibernateOrmSearchMappingConfigurer` implementations with `@SearchExtension`
         * and leave the configuration property unset: Hibernate Search will use the annotated implementation automatically.
         * See xref:hibernate-search-orm-elasticsearch.adoc#plugging-in-custom-components[this section]
         * for more information.
         *
         * If this configuration property is set, it takes precedence over any `@SearchExtension` annotation.
         * ====
         *
         * @asciidoclet
         */
        Optional<List<String>> configurer();
    }

}
