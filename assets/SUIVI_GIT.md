# Suivi Git — dépôt `kfokam48-epreuve-257`

> **Traçabilité de tous les commits et de toutes les pull requests** du dépôt projet (décision T5 du plan).
> **Règle :** une ligne par objet, ajoutée **dès qu'il est poussé** — ce fichier est mis à jour dans le même mouvement que chaque poussée.
> Toute la vie du dépôt se lit ici : date, branche, hash, message exact, issue / PR, étape de l'épreuve.

**Rappels de convention**

- `develop` = branche par défaut, **toutes les PR ciblent `develop`** ; `main` n'avance que par merge (fast-forward) de `develop` **aux trois jalons** `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`.
- **Aucun co-author** : chaque commit a pour unique auteur le candidat (`Ibrahim Talla <ibrahimtalla01@gmail.com>`), aucun trailer `Co-Authored-By` (décision T6).
- Une ligne de ce fichier comporte le hash **une fois le commit poussé** ; la ligne écrite dans le commit lui-même porte la mention *(ce commit)* et son hash est complété à la mise à jour suivante.

---

## Pull requests

| # | Titre | Branche source | Branche cible | Issue | État | Lien |
|---|---|---|---|---|---|---|
| — | *Aucune PR pour l'instant — la première sera ouverte à l'étape 2 (backlog Must)* | — | `develop` | — | — | — |

---

## Commits

| Date | Branche | Hash | Message | Issue / PR | Étape |
|---|---|---|---|---|---|
| 2026-09-25 | `main` *(avant convention)* | `e74a11b` | first commit | — | 0 |
| 2026-09-25 | `main` *(avant convention)* | `b434d23` | chore: gitignore limite aux documents d'epreuve, entrees Java et JS ajoutees | — | 0 |
| 2026-09-25 | `main` *(avant convention)* | `07a7d22` | docs: plan d'implémentation enrichi à partir des documents officiels KFOKAM48 | — | 0 |
| 2026-09-25 | `main` *(avant convention)* | `90849d2` | docs: plan aligné sur les 4 précisions du prof (issues, chore de test, 5 étapes, enveloppe au surveillant) | — | 0 |
| 2026-09-25 | `develop` | `76798d1` | docs: décisions techniques — Next.js 15, docker compose, seed Flyway, branche develop, suivi Git | — | 0 |
| 2026-09-25 | `develop` | `b30509c` | docs: création du suivi Git (assets/SUIVI_GIT.md) | — | 0 |
| 2026-09-25 | `develop` | `69576fa` | docs: convention auto-référente du suivi Git explicite (§4.6) | — | 0 |
| 2026-09-25 | `develop` | `eda97be` | docs: suivi Git — hash du commit de création complété | — | 0 |
| 2026-09-25 | `develop` | `0113e7f` | docs: configuration du dépôt tracée — develop branche par défaut, étape 0 avancée | — | 0 |
| 2026-09-25 | `develop` | `6f5cccb` | chore: arborescence docs/ et api/ — modèles et contrat d'épreuve initial copiés | — | 1 |
| 2026-09-25 | `develop` | `f8c2f52` | docs: cahier des charges et 4 diagrammes Mermaid (D1-D4) de l'étape 1 | #1 → #16 | 1 |
| 2026-09-25 | `develop` | `d7c4e34` | api: contrat complété et figé avant le premier commit de code (9 opérations ajoutées) | — | 1 |
| 2026-09-25 | `develop` | `7558bd6` | docs: journal de bord — entrée de l'étape 1 (analyse) | #16 | 1 |
| 2026-09-25 | `develop` | `7a5f523` | docs: suivi Git — trace de l'étape 1 et du backlog en issues | — | 1 |
| 2026-09-25 | `develop` → `main` | `f730f76` | `[JALON] analyse` (commit vide) — `main` avancée en fast-forward de `develop` | — | 1 |
| 2026-09-25 | `develop` | *(ce commit)* | docs: plan et suivi — étapes 0 et 1 cochées, décision Q4 `429` → `400` | — | 1 |

---

## Issues (backlog)

16 issues ouvertes le 2026-09-25, étiquetées `must` / `should` / `could` — **9 · 6 · 1**.
La colonne **Références** les relie au cahier des charges et au contrat.

| # | Titre (extrait) | Label | Références |
|---|---|---|---|
| 1 | Ouvrir une session et obtenir un code de présence | `must` | EF1 · RG16 |
| 2 | Marquer ma présence en saisissant le code | `must` | EF2 · RG14, RG1, RG2 |
| 3 | Déposer le lien de mon exercice | `must` | EF3 · RG11 |
| 4 | Voir le tableau de ma promotion | `must` | EF6 · RG15, RG17 |
| 5 | Être affecté à la relecture d'un pair | `must` | EF4 · RG5, RG6 |
| 6 | Rendre une note entière 0–20 et un commentaire | `must` | EF5 · RG8, RG9, RG4 |
| 7 | Clôturer la session (**trou principal**, cahier §7.2) | `must` | EF7 · RG11, RG16 |
| 8 | Consulter mes notes sans voir le nom du relecteur | `should` | EF8 · RG7 |
| 9 | Ajouter une présence manuelle tracée `FORMATEUR` | `should` | EF9 · RG13 |
| 10 | Remplacer le lien tant qu'il n'est pas relu | `should` | EF10 · RG12 |
| 11 | Choisir mon nom dans la liste sans mot de passe | `should` | EF11 · Q1 |
| 12 | Flyway, schéma `V1` conforme à D2, seed `V2` | `must` | B5 · ENF4 |
| 13 | Dockeriser `postgres` + `backend` + `frontend` | `must` | démarrage · ENF4 |
| 14 | Couche d'appels API Next.js avec états de charge | `must` | F3 · ENF3, ENF6 |
| 15 | Blocage après 5 codes erronés | `could` | EF12 · RG3 |
| 16 | Journal d'étape, changelog et soumission | `should` | journal 5 pts |

---

## Configuration du dépôt

| Élément | Valeur | Quand |
|---|---|---|
| Branche par défaut GitHub | **`develop`** (`gh repo edit --default-branch develop`) | 2026-09-25 |
| Branche `main` | branche « release » : reçoit uniquement les merges fast-forward de `develop` aux 3 jalons | 2026-09-25 |
| Visibilité | **public** (obligatoire — un dépôt privé = partie non corrigée) | 2026-09-25 |
| Auteur unique des commits | `Ibrahim Talla <ibrahimtalla01@gmail.com>` — **aucun co-author** | permanent |

---

### Comment ce fichier est tenu

1. Après chaque `git push`, ajouter la ligne correspondante (ou les lignes, s'il y a plusieurs commits).
2. Après chaque création / merge de PR, ajouter ou mettre à jour la ligne de la table **Pull requests** (`État` : `open` → `merged`).
3. La colonne **Étape** situe le travail dans la chronologie officielle de l'épreuve : `0` mise en place · `1` analyse · `2` v0.1 · `3` enveloppe · `4` final · `5` soumission.
4. Le fichier est poussé sur `develop` ; il est repris dans `main` lors des merges de jalon.
