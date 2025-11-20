// fr.js
import { str } from '@lit/localize';

export const templates = {
    // Métadonnées
    'quarkus-flyway-meta-description':'Gérer vos migrations de schéma de base de données',
    // Pages
    'quarkus-flyway-datasources':'Sources de données',
    // Général
    'quarkus-flyway-name': 'Nom',
    'quarkus-flyway-action': 'Action',
    'quarkus-flyway-clean': 'Nettoyer',
    'quarkus-flyway-migrate': 'Migrer',
    'quarkus-flyway-clean-disabled-tooltip': 'Le nettoyage Flyway a été désactivé via quarkus.flyway.clean-disabled=true',
    'quarkus-flyway-update-button-title': 'Créer un fichier de migration de mise à jour. Relisez toujours manuellement le fichier créé car il peut entraîner une perte de données',
    'quarkus-flyway-generate-migration-file': 'Générer un fichier de migration',
    'quarkus-flyway-create-button-title': 'Configurer les fichiers de base pour permettre les migrations Flyway. Un fichier initial dans db/migrations sera créé et vous pourrez ensuite ajouter des fichiers de migration supplémentaires',
    'quarkus-flyway-create-initial-migration-file': 'Créer le fichier de migration initial',
    'quarkus-flyway-create': 'Créer',
    'quarkus-flyway-update': 'Mettre à jour',
    'quarkus-flyway-datasource-title': str`Source de données ${0}`,
    'quarkus-flyway-create-dialog-description': 'Configurer un fichier initial à partir de la génération de schéma Hibernate ORM pour permettre les migrations Flyway.<br/>Si vous acceptez, un fichier initial dans <code>db/migrations</code> sera <br/>créé et vous pourrez ensuite ajouter des fichiers de migration supplémentaires comme documenté.',
    'quarkus-flyway-update-dialog-description': 'Créer un fichier de migration incrémentielle à partir du diff de schéma Hibernate ORM.<br/>Si vous acceptez, un fichier supplémentaire dans <code>db/migrations</code> sera <br/>créé.',
    'quarkus-flyway-cancel': 'Annuler',
    'quarkus-flyway-clean-confirm': 'Cela supprimera tous les objets (tables, vues, procédures, triggers, …) dans le schéma configuré. Voulez-vous continuer ?'
};
