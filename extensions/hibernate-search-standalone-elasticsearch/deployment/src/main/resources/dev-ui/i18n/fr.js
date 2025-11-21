// fr.js
import { str } from '@lit/localize';

export const templates = {
    // Métadonnées
    'quarkus-hibernate-search-standalone-elasticsearch-meta-description':'Indexer/rechercher explicitement des entités dans Elasticsearch',
    // Pages
    'quarkus-hibernate-search-standalone-elasticsearch-indexed_entity_types':'Types d’entités indexées',
    // Général
    'quarkus-hibernate-search-standalone-elasticsearch-loading': 'Chargement…',
    'quarkus-hibernate-search-standalone-elasticsearch-no-indexed-entities': 'Aucune entité indexée trouvée.',
    'quarkus-hibernate-search-standalone-elasticsearch-selected-entity-types': str`${0} type${1} d’entité sélectionné${1}`,
    'quarkus-hibernate-search-standalone-elasticsearch-reindex-selected': 'Réindexer la sélection',
    'quarkus-hibernate-search-standalone-elasticsearch-entity-name': 'Nom de l’entité',
    'quarkus-hibernate-search-standalone-elasticsearch-class-name': 'Nom de la classe',
    'quarkus-hibernate-search-standalone-elasticsearch-index-names': 'Noms d’index',
    'quarkus-hibernate-search-standalone-elasticsearch-select-entity-types': 'Sélectionnez les types d’entités à réindexer.',
    'quarkus-hibernate-search-standalone-elasticsearch-reindex-started': str`Réindexation demandée de ${0} types d’entités.`,
    'quarkus-hibernate-search-standalone-elasticsearch-reindex-success': str`Réindexation réussie de ${0} types d’entités.`,
    'quarkus-hibernate-search-standalone-elasticsearch-reindex-error': str`Une erreur s’est produite lors de la réindexation de ${0} types d’entités :\n${1}`
};
