// fr.js
import { str } from '@lit/localize';

export const templates = {
    // Métadonnées
    'quarkus-liquibase-meta-description':'Gérer vos migrations de schéma de base de données avec Liquibase',
    // Pages
    'quarkus-liquibase-datasources':'Sources de données',
    // Général
    'quarkus-liquibase-loading-datasources': 'Chargement des sources de données…',
    'quarkus-liquibase-name': 'Nom',
    'quarkus-liquibase-action': 'Action',
    'quarkus-liquibase-clear-database': 'Vider la base de données',
    'quarkus-liquibase-clear': 'Effacer',
    'quarkus-liquibase-clear-confirm': 'Cela supprimera tous les objets (tables, vues, procédures, triggers, …) dans le schéma configuré. Voulez-vous continuer ?',
    'quarkus-liquibase-cleared': str`La source de données ${0} a été vidée.`,
    'quarkus-liquibase-migrate': 'Migrer',
    'quarkus-liquibase-migrated': str`La source de données ${0} a été migrée.`
};
