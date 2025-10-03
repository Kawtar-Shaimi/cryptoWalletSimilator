# Configuration de la Base de Données

Ce dossier contient la configuration de la base de données pour des raisons de sécurité et d'organisation.

## Fichiers

- `DatabaseConfig.java` : Configuration isolée contenant les paramètres de connexion à la base de données
- `Database.java` : Singleton pour gérer les connexions JDBC PostgreSQL

## Sécurité

Les paramètres de base de données peuvent être surchargés via les propriétés système :

```bash
java -Ddb.url=jdbc:postgresql://prod-server:5432/cryptowallet \
     -Ddb.user=prod_user \
     -Ddb.password=secure_password \
     -jar application.jar
```

## Bonnes pratiques

1. **Ne jamais commiter de vrais mots de passe** dans le code source
2. Utiliser des variables d'environnement ou des fichiers de configuration externe en production
3. Les valeurs par défaut dans `DatabaseConfig` sont uniquement pour le développement local