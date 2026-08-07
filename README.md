# TCGM - Gestionnaire de Cartes à Collectionner

Application full-stack pour gérer une collection de cartes TCG (Pokemon, Magic, Yu-Gi-Oh!, etc.).

## Stack technique

| Composant | Technologie |
|-----------|-------------|
| Backend   | Spring Boot 3.4 + Java 21 |
| Frontend  | React + TypeScript + Vite |
| Base de données | MySQL 8.0 |

## Structure du projet

```
TCGM_project/
├── backend/          # API REST Spring Boot
├── frontend/         # Interface React
└── docker-compose.yml  # MySQL en conteneur
```

## Prérequis

- **Java 21**
- **Node.js 18+**
- **Maven** — [Télécharger Maven](https://maven.apache.org/download.cgi) ou utiliser IntelliJ IDEA
- **Docker Desktop** (optionnel, pour MySQL en production locale)

## Démarrage rapide

### 1. Lancer le backend

Par défaut, le backend utilise une base **H2 en mémoire** (aucune installation requise) :

```bash
cd backend
# Windows
mvnw.cmd spring-boot:run
# Linux / Mac
./mvnw spring-boot:run
```

L'API est disponible sur **http://localhost:8081**

#### Utiliser MySQL (optionnel)

Si vous préférez MySQL, démarrez d'abord la base de données :

```bash
docker-compose up -d
```

Puis lancez le backend avec le profil `mysql` :

```bash
# Windows
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=mysql
# Linux / Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=mysql
```

Cela démarre MySQL sur le port `3306` avec :
- Base : `tcgm_db`
- Utilisateur : `tcgm_user`
- Mot de passe : `tcgm_password`

### 2. Lancer le frontend

```bash
cd frontend
npm install
npm run dev
```

L'interface est disponible sur **http://localhost:5173**

## API REST

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/cards` | Liste toutes les cartes |
| GET | `/api/cards/{id}` | Détails d'une carte |
| GET | `/api/cards/search?q=` | Recherche par nom |
| POST | `/api/cards` | Créer une carte |
| PUT | `/api/cards/{id}` | Modifier une carte |
| DELETE | `/api/cards/{id}` | Supprimer une carte |

## Fonctionnalités

- Affichage de la collection avec statistiques (nombre de cartes, valeur totale)
- Recherche en temps réel
- Ajout, modification et suppression de cartes
- Interface moderne avec thème sombre
- Données de démonstration au premier démarrage

## Configuration MySQL locale (sans Docker)

Modifiez `backend/src/main/resources/application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/tcgm_db
spring.datasource.username=votre_user
spring.datasource.password=votre_password
```

Créez la base de données :

```sql
CREATE DATABASE tcgm_db;
```
