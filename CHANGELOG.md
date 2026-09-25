# Changelog — KFOKAM48 présence et relecture par les pairs

> Cohérent avec l'historique Git réel (voir `docs/SUIVI_GIT.md` pour le détail commit
> par commit). Chaque entrée renvoie aux issues et PR fermées.

## [v1.0] — Étape 4, version finale

- Backlog restant trié (voir « Backlog » ci-dessous).
- `README.md` validé depuis un clone vierge (`docker compose up --build`).
- `docs/JOURNAL.md` complété (entrée étape 4).

## [v0.2] — Étape 3, l'enveloppe

**Correctif** (issue #31, PR #32) :
- Présences perdues sous écriture concurrente : le contrôle d'existence puis
  l'écriture n'étaient pas atomiques sur `POST /api/presences`. Sous deux requêtes
  concurrentes, une violation de contrainte non interceptée remontait en `500` au
  lieu d'un `409 DEJA_PRESENT` propre. Reproduit par un test rouge
  (`PresenceConcurrenteTest`) avant correction.

**Évolution** (issue #33, PR #34) — changement de besoin, casse RG5 (`Q6`) :
- Chaque exercice est désormais relu par **deux pairs distincts** au lieu d'un seul
  (RG18).
- La note retenue est la **moyenne des deux** si les deux ont rendu (définitive), la
  note seule **marquée provisoire** si un seul a rendu, rien si aucun (RG19).
- Migration `V3__deux_relecteurs.sql` (la contrainte `UNIQUE(exercice_id)` devient
  `UNIQUE(exercice_id, relecteur_id)`) — `V1` et `V2` inchangées, données de démo
  préservées.
- `api/contrat.yaml` : `GET /api/exercices/{id}/relectures` renvoie désormais
  `{noteRetenue, provisoire, relectures[]}` ; `POST /api/relectures` renvoie un
  tableau.
- Écran étudiant : affichage de la note retenue et d'un badge « provisoire ».
- **Sacrifice de périmètre** : l'issue #15 (blocage après 5 codes erronés, priorité
  `could`) sort du périmètre de cette livraison — voir `docs/CAHIER_DES_CHARGES.md` §7.3.

## [v0.1] — Étape 2, première version

Incrément fonctionnel complet sur les stories **Must**, jalon `[JALON] v0.1`.

- Backend Spring Boot (Java 21) : contrôleur/service/repository/DTO séparés,
  validation + `@RestControllerAdvice` centralisé, schéma Flyway `V1` + seed `V2`.
- Les 5 opérations du contrat imposées, plus les opérations complémentaires
  (clôture de session, promotions/étudiants, création de relecture, remplacement de
  lien, consultation des notes).
- Frontend Next.js 15 : 3 écrans (`/formateur`, `/etudiant`, `/relecteur`), couche
  d'appels API unique `src/lib/api/`, aucune règle métier dupliquée côté client.
- Dockerisation : `docker compose up` démarre `postgres` + `backend` + `frontend` en
  une seule commande, données de démonstration chargées au démarrage.
- 14 issues `must` fermées par 14 PR (#17 à #30).
- 44 tests backend (unitaires + intégration, H2), `npm run build` vert.

## [v0.0] — Étape 1, analyse et conception — jalon `[JALON] analyse`

- `docs/CAHIER_DES_CHARGES.md` en 10 sections (exigences fonctionnelles, règles de
  gestion, contradictions `Q10`/`Q15` tranchées, trous identifiés et décidés).
- 4 diagrammes Mermaid (`docs/diagrammes/`) : cas d'utilisation, modèle de données,
  séquence de présence, états-transitions de l'exercice (bonus).
- 16 issues créées (10 `must`, 5 `should`, 1 `could`), backlog initial.
- `api/contrat.yaml` complété (14 opérations) et figé avant le premier commit de code.

## [v0.0-préparation] — Étape 0, mise en place du dépôt

- Dépôt public `kfokam48-epreuve-257`, `develop` branche par défaut, `main` en
  branche « release » (merges fast-forward aux jalons uniquement).
- `.gitignore` Java + JS posé avant le premier commit de code.
- `docs/SUIVI_GIT.md` initialisé : traçabilité de chaque commit et chaque PR.

---

## Backlog

Voir `docs/SUIVI_GIT.md` § Issues (backlog) pour le détail complet avec liens de
fermeture. État au [v1.0] :

- **18 issues** créées (16 à l'étape 1, 2 à l'étape 3) — **16 fermées**, **2 ouvertes**.
- Ouvertes, priorité à jour :
  - **#15** (`could`) — blocage après 5 codes erronés : **sacrifiée** à l'étape 3 pour
    absorber le changement de besoin (issue #33), voir cahier des charges §7.3.
  - **#16** (`should`) — journal, changelog, soumission : en cours de clôture à
    l'étape 4 (ce fichier, `docs/JOURNAL.md`, `docs/SOUMISSION.md`).
