# KFOKAM48 — Présence et relecture par les pairs

Application de présence et de relecture par les pairs pour la formation KFOKAM48 : un
formateur ouvre une session et obtient un code de présence, un étudiant marque sa présence
et dépose le lien de son exercice, un pair tiré au hasard parmi les présents relit
l'exercice, et le formateur suit l'ensemble dans un tableau de promotion.

## Démarrage (une seule commande)

Prérequis : Docker et Docker Compose.

```bash
docker compose up --build
```

- Frontend : http://localhost:3000
- Backend (API) : http://localhost:8080

Le schéma est posé par Flyway (`V1__init.sql`) et les données de démonstration sont chargées
automatiquement au démarrage (`V2__seed_demo.sql`) : deux promotions, huit étudiants, une
session clôturée (historique complet) et une session ouverte dont le **code de présence est
`K7F2M48`** — utilisable directement dans l'écran étudiant.

## Choix technique du frontend (F1)

**Next.js 15** (App Router) a été retenu pour ses trois écrans en routes natives, sa couche
serveur légère et son build de production (`next build` puis `next start`) ; aucun rendu
côté serveur applicatif n'est exigé par le besoin.

## Écrans (F2)

| Écran | Route | Usage |
| --- | --- | --- |
| Formateur | `/formateur` | Ouvrir une session, clôturer, ajouter une présence manuelle, consulter le tableau de la promotion |
| Étudiant | `/etudiant` | Choisir son nom, marquer sa présence par code, déposer/remplacer le lien de son exercice, consulter ses notes |
| Relecteur | `/relecteur` | Rendre une note (0–20) et un commentaire sur l'exercice assigné |

## Architecture

- **Backend** : Java 21 / Spring Boot 3 / Maven (wrapper `mvnw` commité), couches
  Controller → Service → Repository, DTO exclusivement en sortie d'API (B3), validation +
  `@RestControllerAdvice` centralisé (B4).
- **Persistance** : PostgreSQL en production/dev, migrations **Flyway** versionnées
  (`backend/src/main/resources/db/migration/`), **H2** en mémoire pour les tests (B5/B6).
- **Frontend** : Next.js 15 + TypeScript, couche d'appels API unique dans
  `frontend/src/lib/api/` (F3), états chargement/erreur explicites, aucune règle métier
  dupliquée côté client (la moyenne n'est jamais recalculée dans le navigateur).
- **Contrat** : `api/contrat.yaml` (OpenAPI), respecté à la lettre par le backend (B2).

## Développement sans Docker

```bash
# backend — nécessite une base PostgreSQL locale (voir backend/src/main/resources/application.yml)
cd backend && ./mvnw spring-boot:run

# frontend
cd frontend && npm install && npm run dev
```

## Tests

```bash
cd backend && ./mvnw test
cd frontend && npm run build
```

## Documentation

- `docs/CAHIER_DES_CHARGES.md` — analyse et conception
- `docs/diagrammes/` — cas d'utilisation, modèle de données, séquence, états (Mermaid)
- `docs/JOURNAL.md` — journal de bord, une entrée par étape
- `CHANGELOG.md` — historique des livraisons
