// fr.js
import { str } from '@lit/localize';

export const templates = {
    // Métadonnées
    'quarkus-hibernate-search-orm-elasticsearch-meta-description':'Indexer automatiquement vos entités Hibernate dans Elasticsearch',
    // Pages
    'quarkus-hibernate-search-orm-elasticsearch-indexed_entity_types':'Types d’entités indexées',
    // Général
    'quarkus-hibernate-search-orm-elasticsearch-loading': 'Chargement…',
    'quarkus-hibernate-search-orm-elasticsearch-no-persistence-units':
        'Aucune unité de persistance trouvée.',
    'quarkus-hibernate-search-orm-elasticsearch-persistence-unit':
        'Unité de persistance',
    'quarkus-hibernate-search-orm-elasticsearch-no-indexed-entities':
        'Aucune entité indexée trouvée.',
    'quarkus-hibernate-search-orm-elasticsearch-selected-entity-types':
        str`${0} types d’entités sélectionnés`,
    'quarkus-hibernate-search-orm-elasticsearch-reindex-selected':
        'Réindexer la sélection',
    'quarkus-hibernate-search-orm-elasticsearch-entity-name':
        'Nom de l’entité',
    'quarkus-hibernate-search-orm-elasticsearch-class-name':
        'Nom de la classe',
    'quarkus-hibernate-search-orm-elasticsearch-index-names':
        'Noms d’index',
    'quarkus-hibernate-search-orm-elasticsearch-select-entity-types-to-reindex':
        str`Sélectionnez les types d’entités à réindexer pour l’unité de persistance « ${0} ».`,
    'quarkus-hibernate-search-orm-elasticsearch-reindex-started':
        str`Réindexation demandée de ${0} types d’entités pour l’unité de persistance « ${1} ».`,
    'quarkus-hibernate-search-orm-elasticsearch-reindex-success':
        str`Réindexation réussie de ${0} types d’entités pour l’unité de persistance « ${1} ».`,
    'quarkus-hibernate-search-orm-elasticsearch-reindex-error':
        str`Une erreur s’est produite lors de la réindexation de ${0} types d’entités pour l’unité de persistance « ${1} » :\n${2}`
};
