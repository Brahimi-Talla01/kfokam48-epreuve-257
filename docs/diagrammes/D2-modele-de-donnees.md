# D2 — Modèle de données

> **Ce diagramme EST le schéma des migrations Flyway.** Toute divergence entre les colonnes ci-dessous et `V1__init.sql` est un bug, pas un détail.
> Sources : `api/contrat.yaml` (colonnes et énumérations), `CLIENT.md` Q1–Q16, cahier §6.

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION : "accueille"
    SESSION ||--o{ PRESENCE : "releve"
    SESSION ||--o{ EXERCICE : "recue"
    ETUDIANT ||--o{ PRESENCE : "signe"
    ETUDIANT ||--o{ EXERCICE : "depot"
    EXERCICE ||--o| RELECTURE : "est relu par"
    ETUDIANT ||--o{ RELECTURE : "relit"

    PROMOTION {
        int id PK
        string nom UK
        timestamp created_at
    }

    ETUDIANT {
        int id PK
        string nom
        int promotion_id FK
        timestamp created_at
    }

    SESSION {
        int id PK
        string titre
        int promotion_id FK
        string code UK "code de présence, RG1"
        string statut "OUVERTE | CLOTUREE, RG16"
        timestamp ouverture_at
        timestamp expiration_at "fin du code, RG1"
        timestamp cloture_at "fin de session, RG16"
        timestamp created_at
    }

    PRESENCE {
        int id PK
        int session_id FK
        int etudiant_id FK
        string source "ETUDIANT | FORMATEUR, RG13"
        timestamp marque_le
    }

    EXERCICE {
        int id PK
        int session_id FK
        int etudiant_id FK
        string lien "URL, RG12"
        string statut "EN_ATTENTE | RENDU"
        timestamp depose_le
        timestamp mis_a_jour_le
    }

    RELECTURE {
        int id PK
        int exercice_id FK
        int relecteur_id FK "null si aucun pair éligible, §7.2"
        string statut "EN_ATTENTE | RENDUE, RG10"
        int note "entier 0..20, RG8"
        string commentaire
        timestamp rendue_le
    }
```

## Correspondance 1:1 avec les migrations

| Migration | Éléments du diagramme |
|---|---|
| **`V1__init.sql`** | Les 6 tables + contraintes `UNIQUE` + `CHECK` + index |
| **`V2__seed_demo.sql`** | 1–2 promotions, ~15 étudiants, 1 session ouverte avec code réel, quelques présences et exercices |
| **`V3__*.sql`** (étape 3) | Ajouts provenant de l'évolution du besoin — **jamais** de modification en place (B5) |

## Contraintes d'intégrité (écrites dans `V1`)

| Contrainte | Règle | Source |
|---|---|---|
| `uk_presence_etudiant_session` | 1 présence par (étudiant, session) → `409 DEJA_PRESENT` | RG14 |
| `uk_exercice_etudiant_session` | 1 exercice par (étudiant, session) | logique métier |
| `uk_relecture_exercice` | **Un seul relecteur par exercice** | RG5 |
| `ck_note_entre_0_et_20` | `note IS NULL OR note BETWEEN 0 AND 20` | RG8 |
| `ck_statut_session` | `IN ('OUVERTE','CLOTUREE')` | RG16 |
| `ck_statut_relecture` | `IN ('EN_ATTENTE','RENDUE')` | RG10 |
| `ck_source_presence` | `IN ('ETUDIANT','FORMATEUR')` | RG13 |
| `idx_tableau` | `(session_id, etudiant_id)` sur `presences`, `(etudiant_id)` sur `exercices` | ENF2 |

## Points d'attention

- **Pas de table `Relecteur`** : le relecteur est un `Etudiant` référencé par `RELECTURE` (cahier §2).
- **`relecture.relecteur_id` est nullable** : cas « aucun pair présent à la session » (§7.2 du cahier) — l'exercice reste `EN_ATTENTE` et remonte dans `relecturesEnAttente` (Q11, RG10).
- **`note` est nullable** : c'est ce qui rend `moyenne = null` possible dans `GET /api/tableau` (contrat) tant qu'aucune note n'existe (RG17).
- **Deux timestamps de fin distincts** : `expiration_at` (code, Q2) ≠ `cloture_at` (session, Q3/Q12) — les confondre invaliderait RG1 et RG11.
