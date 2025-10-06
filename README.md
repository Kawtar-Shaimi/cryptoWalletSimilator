# Crypto Wallet Simulator

## 📋 Description du Projet

Crypto Wallet Simulator est une application console en Java 8 qui simule un portefeuille de cryptomonnaies avec système de mempool et optimisation des frais de transaction. Cette application permet aux utilisateurs de gérer des wallets Bitcoin et Ethereum, créer des transactions, et analyser leur position dans le mempool pour optimiser les frais.

## 🚀 Technologies Utilisées

- **Java 8** (JDK 1.8)
- **PostgreSQL** avec driver JDBC
- **H2 Database** (alternative pour développement)
- **JUnit 4.13.2** (tests unitaires)
- **Git** (gestion de versions)
- **Architecture en couches** (MVC)

## 🏗️ Structure du Projet

```
cryptoWalletSimilator/
├── src/
│   ├── config/               # Configuration base de données
│   │   ├── ConfigManager.java
│   │   ├── Database.java
│   │   └── config.properties
│   ├── metier/              # Couche métier
│   │   ├── enums/           # Énumérations
│   │   │   ├── CryptoType.java
│   │   │   ├── FeePriority.java
│   │   │   └── TransactionStatus.java
│   │   ├── model/           # Modèles de données
│   │   │   ├── Wallet.java
│   │   │   ├── BitcoinWallet.java
│   │   │   ├── EthereumWallet.java
│   │   │   └── Transaction.java
│   │   └── service/         # Services métier
│   │       ├── WalletService.java
│   │       ├── TransactionService.java
│   │       ├── MempoolService.java
│   │       ├── FeeCalculator.java
│   │       ├── BitcoinFeeCalculator.java
│   │       ├── EthereumFeeCalculator.java
│   │       ├── FeeCalculatorFactory.java
│   │       └── WalletFactory.java
│   ├── repository/          # Couche de données
│   │   ├── WalletRepository.java
│   │   ├── TransactionRepository.java
│   │   └── jdbc/
│   │       ├── JdbcWalletRepository.java
│   │       └── JdbcTransactionRepository.java
│   ├── ui/                  # Interface utilisateur
│   │   └── ConsoleApp.java
│   ├── util/                # Utilitaires
│   │   ├── AddressGenerator.java
│   │   ├── ValidationUtils.java
│   │   └── LoggerProvider.java
│   └── Lib/                 # Bibliothèques externes
│       ├── postgresql-42.7.7.jar
├── sql/
│   └── schema.sql          # Schéma base de données 
└── README.md
```

## 🔧 Prérequis et Installation

### Prérequis

1. **Java Development Kit (JDK) 8**
   ```bash
   java -version  # Doit afficher 1.8.x
   javac -version # Doit afficher 1.8.x
   ```

2. **PostgreSQL** (optionnel - H2 inclus comme alternative)
   - Version 12 ou supérieure
   - Base de données `cryptowallet` créée
   - Utilisateur avec droits d'accès

3. **Git** pour le clonage du repository

### Installation

1. **Cloner le repository**
   ```bash
   git clone https://github.com/Kawtar-Shaimi/cryptoWalletSimilator.git
   cd cryptoWalletSimilator
   ```

2. **Configuration de la base de données**
   
   Éditer `src/config/config.properties` :
   ```properties
   # PostgreSQL (production)
   db.url=jdbc:postgresql://localhost:5432/cryptowallet
   db.user=postgres
   db.password=votre_mot_de_passe
   
   # H2 (développement - décommentez si PostgreSQL non disponible)
   # db.url=jdbc:h2:mem:cryptowallet;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
   # db.user=sa
   # db.password=
   ```

3. **Initialiser la base de données**
   ```sql
   -- Exécuter sql/schema.sql dans PostgreSQL
   psql -U postgres -d cryptowallet -f sql/schema.sql
   ```

4. **Compiler le projet**
   ```bash
   # Windows
   javac -cp "src/Lib/*" -d build src/ui/ConsoleApp.java src/metier/**/*.java src/repository/**/*.java src/util/*.java src/config/*.java
   
   # Linux/Mac
   javac -cp "src/Lib/*" -d build $(find src -name "*.java" | grep -v test)
   ```

5. **Créer le JAR exécutable**
   ```bash
   # Créer le manifeste
   echo "Main-Class: ui.ConsoleApp" > manifest.txt
   echo "Class-Path: Lib/postgresql-42.7.7.jar" >> manifest.txt
   
   # Créer le JAR
   jar -cfm dist/crypto-wallet-simulator.jar manifest.txt -C build . -C src Lib
   ```

## 🎮 Guide d'Utilisation

### Lancement de l'Application

**Méthode 1 : Via JAR**
```bash
cd dist
java -jar crypto-wallet-simulator.jar
```

**Méthode 2 : Via classes compilées**
```bash
java -cp "build;src/Lib/*" ui.ConsoleApp
```

### Fonctionnalités Principales

#### 1. Créer un Wallet Crypto
- Choisir le type : `1` pour Bitcoin, `2` pour Ethereum
- Génération automatique d'une adresse unique conforme au format
- Solde initial à zéro

#### 2. Ajouter des Fonds
- Saisir l'ID du wallet
- Spécifier le montant à ajouter (simulation de réception)
- Validation automatique des montants positifs

#### 3. Créer une Transaction
- ID du wallet source
- Adresse de destination (validation du format selon le type de crypto)
- Montant à envoyer
- Priorité des frais :
  - `1` : ÉCONOMIQUE (lent, moins cher)
  - `2` : STANDARD (moyen)
  - `3` : RAPIDE (rapide, plus cher)

#### 4. Position dans le Mempool
- Affiche la position de votre transaction dans la file d'attente
- Calcule le temps d'attente estimé (position × 10 minutes)
- Montre les frais payés

#### 5. Comparaison des Frais
- Tableau comparatif des 3 niveaux de priorité
- Position estimée dans le mempool pour chaque niveau
- Aide à l'optimisation coût/rapidité

#### 6. État du Mempool
- Liste des transactions en attente
- Simulation d'activité réseau avec transactions aléatoires
- Identification de votre transaction dans la liste

### Exemple d'Utilisation

```
=== Crypto Wallet Simulator ===
1. Creer un wallet crypto
2. Ajouter des fonds à un wallet
3. Consulter la balance d'un wallet
4. Creer une nouvelle transaction
5. Voir ma position dans le mempool
6. Comparer les 3 niveaux de frais
7. Consulter l'etat actuel du mempool
0. Quitter
Votre choix: 1

Type de wallet (1=BITCOIN, 2=ETHEREUM): 2
Wallet créé: id=abc123, type=ETHEREUM, address=0x742d35cc..., balance=0
```

## 🧪 Tests Unitaires

### Lancement des Tests

```bash
# Compiler les tests
javac -cp "src/Lib/*;build" -d build src/test/java/**/*.java

# Exécuter les tests
java -cp "src/Lib/*;build" org.junit.runner.JUnitCore metier.service.WalletServiceTest
java -cp "src/Lib/*;build" org.junit.runner.JUnitCore util.ValidationUtilsTest
```

### Tests Implémentés

- **WalletServiceTest** : 7 tests couvrant la création de wallets, ajout de fonds, validations
- **ValidationUtilsTest** : 4 tests pour les validations d'adresses et montants

## 🏛️ Architecture et Design Patterns

### Patterns Utilisés

- **Singleton** : `Database` pour la gestion des connexions
- **Repository Pattern** : Abstraction de la couche de données
- **Factory Pattern** : `WalletFactory`, `FeeCalculatorFactory`
- **Strategy Pattern** : `FeeCalculator` avec implémentations Bitcoin/Ethereum
- **Service Layer** : Séparation logique métier/présentation

### Principes SOLID

- **SRP** : Classes avec responsabilités uniques
- **OCP** : Extension via interfaces (FeeCalculator)
- **LSP** : Substitution Wallet → BitcoinWallet/EthereumWallet
- **ISP** : Interfaces spécialisées (WalletRepository, TransactionRepository)
- **DIP** : Dépendance vers abstractions

## 🔍 Détails Techniques

### Calcul des Frais

**Bitcoin**
- Formule : `taille_transaction_bytes × sat_per_byte`
- Économique : 5 sat/byte
- Standard : 20 sat/byte
- Rapide : 60 sat/byte

**Ethereum**
- Formule : `gas_limit × gas_price_gwei`
- Gas limit : 21,000 (transfert simple)
- Économique : 5 gwei
- Standard : 20 gwei
- Rapide : 60 gwei

### Simulation Mempool

- File d'attente ordonnée par frais décroissants
- Position = rang dans la liste triée
- Temps estimé = position × 10 minutes
- Génération de 10-20 transactions aléatoires pour simulation

### Validation des Adresses

**Bitcoin**
- Format P2PKH : commence par "1"
- Format P2SH : commence par "3"
- Format Bech32 : commence par "bc1"

**Ethereum**
- Format : "0x" + 40 caractères hexadécimaux
- Insensible à la casse

## 🐛 Débogage et Logs

### Configuration des Logs

- **java.util.logging** pour les opérations système
- **System.out.println** uniquement pour l'interface utilisateur
- Niveaux : INFO, WARNING, SEVERE

### Problèmes Courants

1. **Erreur de connexion base de données**
   - Vérifier PostgreSQL en cours d'exécution
   - Contrôler les paramètres dans config.properties
   - Utiliser H2 comme alternative

2. **ClassNotFoundException**
   - Vérifier le classpath (-cp)
   - S'assurer que tous les JARs sont présents

3. **Compilation échouée**
   - Utiliser JDK 8 exclusivement
   - Vérifier les dépendances dans src/Lib/

## 📊 Métriques et Performance

- **Couverture de tests** : Tests unitaires sur services critiques
- **Complexité** : Architecture modulaire avec faible couplage
- **Maintenabilité** : Code documenté et respectant les conventions Java

## 🔮 Évolutions Futures

- Export des transactions en CSV
- Containerisation Docker
- Interface graphique
- Support d'autres cryptomonnaies
- Intégration APIs blockchain réelles

## 👥 Contribution

Projet développé par Kawtar Shaimi dans le cadre de la formation Java 8.

## 📄 Licence

Projet éducatif - Utilisation libre pour l'apprentissage.

---

*Crypto Wallet Simulator v1.0 - Simulation éducative de portefeuille crypto*