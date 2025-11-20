// fr.js
import { str } from '@lit/localize';

export const templates = {
    // Métadonnées
    'quarkus-smallrye-openapi-meta-description':'Documentez vos API REST avec OpenAPI - livré avec Swagger UI',
    // Pages
    'quarkus-smallrye-openapi-swagger_ui':'Swagger UI',
    'quarkus-smallrye-openapi-schema_yaml':'Schéma YAML',
    'quarkus-smallrye-openapi-schema_json':'Schéma JSON',
    'quarkus-smallrye-openapi-generate_clients':'Générer des clients',
    // Général
    'quarkus-smallrye-openapi-technology-language': 'Technologie / Langage',
    'quarkus-smallrye-openapi-description': "Générez du code client à partir du document de schéma OpenAPI produit par votre application Quarkus au moment de la compilation.",
    'quarkus-smallrye-openapi-talking-to-ai': 'Discussion avec l’IA…',
    'quarkus-smallrye-openapi-can-take-while': 'Cela peut prendre un certain temps',
    'quarkus-smallrye-openapi-code-generated': str`${0} code généré à partir du schéma OpenAPI avec l’IA :`,
    'quarkus-smallrye-openapi-copy': 'Copier',
    'quarkus-smallrye-openapi-failed-generate': str`Échec de la génération du code : ${0}`,
    'quarkus-smallrye-openapi-copied': 'Contenu copié dans le presse-papiers',
    'quarkus-smallrye-openapi-failed-copy': str`Échec de la copie du contenu : ${0}`,
    'quarkus-smallrye-openapi-no-content': 'Aucun contenu',
    'quarkus-smallrye-openapi-java-quarkus': 'Java (Quarkus)',
    'quarkus-smallrye-openapi-java-context': "Ce code doit être du code Java Quarkus valide utilisant l’extension quarkus-rest-client-jackson. Il est très important d’utiliser l’espace de noms jakarta.ws lors de l’import des classes. N’utilisez PAS l’ancien espace de noms javax.ws. Utilisez l’annotation org.eclipse.microprofile.rest.client.inject.RegisterRestClient",
    'quarkus-smallrye-openapi-kotlin-quarkus': 'Kotlin (Quarkus)',
    'quarkus-smallrye-openapi-kotlin-context': "Ce code doit être du code Kotlin Quarkus valide utilisant l’extension quarkus-rest-client-jackson. Il est très important d’utiliser l’espace de noms jakarta.ws lors de l’import des classes. N’utilisez PAS l’ancien espace de noms javax.ws. Utilisez l’annotation org.eclipse.microprofile.rest.client.inject.RegisterRestClient",
    'quarkus-smallrye-openapi-javascript': 'JavaScript',
    'quarkus-smallrye-openapi-typescript': 'TypeScript',
    'quarkus-smallrye-openapi-csharp': 'C#',
    'quarkus-smallrye-openapi-cpp': 'C++',
    'quarkus-smallrye-openapi-php': 'PHP',
    'quarkus-smallrye-openapi-python': 'Python',
    'quarkus-smallrye-openapi-rust': 'Rust',
    'quarkus-smallrye-openapi-go': 'Go'
};
