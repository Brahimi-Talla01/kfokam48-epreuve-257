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
| #17 | Schéma Flyway V1 + seed de démonstration V2 | `feature/flyway-schema-seed` | `develop` | **#12** | `merged` | [pull/17](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/17) |
| #18 | Ouverture de session et code de presence | `feature/ouverture-session` | `develop` | **#1** | `merged` | [pull/18](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/18) |
| #19 | Marquage d'une presence par code + tests B6 | `feature/presence-code` | `develop` | **#2** | `merged` | [pull/19](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/19) |
| #20 | Depot du lien d'exercice et remplacement | `feature/depot-exercice` | `develop` | **#3** | `merged` | [pull/20](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/20) |
| #21 | Affectation aleatoire du relecteur | `feature/affectation-relecteur` | `develop` | **#5** | `merged` | [pull/21](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/21) |
| #22 | Rendu d'une note 0-20 sur une relecture | `feature/rendu-relecture` | `develop` | **#6** | `merged` | [pull/22](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/22) |
| #23 | Cloture de session | `feature/cloture-session` | `develop` | **#7** | `merged` | [pull/23](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/23) |
| #24 | Tableau de progression de la promotion | `feature/tableau-progression` | `develop` | **#4** | `merged` | [pull/24](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/24) |
| #25 | Consultation des notes et commentaires | `feature/consultation-notes` | `develop` | **#8** | `merged` | [pull/25](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/25) |
| #26 | Promotions et liste des étudiants pour choisir son nom | `feature/choix-nom` | `develop` | **#11** | `merged` | [pull/26](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/26) |
| #27 | Remplacement du lien d'exercice | `feature/remplacement-lien` | `develop` | **#10** | `merged` | [pull/27](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/27) |
| #28 | Présence relevée par le formateur | `feature/presence-formateur` | `develop` | **#9** | `merged` | [pull/28](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/28) |
| #29 | Écrans formateur/etudiant/relecteur + couche d'appels API unique | `feature/api-layer-next` | `develop` | **#14** | `merged` | [pull/29](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/29) |
| #30 | Dockerisation postgres + backend + frontend | `infra/dockerisation` | `develop` | **#13** | `merged` | [pull/30](https://github.com/Brahimi-Talla01/kfokam48-epreuve-257/pull/30) |

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
| 2026-09-25 | `develop` | `1f25a72` | docs: plan et suivi — étapes 0 et 1 cochées, décision Q4 `429` → `400` | — | 1 |
| 2026-09-25 | `develop` | *(ce commit)* | chore: gitignore retenu tel quel (`/assets` + Java/JS), plan §4.1 aligné | — | 1 |
| 2026-09-25 | `develop` | `4362748` | docs: correction du backlog (10 must / 5 should / 1 could) et versions réelles de l'environnement | — | 1 |
| 2026-09-25 | `feature/flyway-schema-seed` | `d58821c` | Schéma versionné Flyway (V1) et jeu de démonstration (V2), wrapper Maven commité | **#12** | 2 |
| 2026-09-25 | `develop` | `1b1bf18` | Merge pull request #17 (feature/flyway-schema-seed → develop) | **#12** · PR #17 | 2 |
| 2026-09-25 | `develop` | *(ce commit)* | docs: suivi Git — PR #17 et issue #12 tracées | — | 2 |
| 2026-09-25 | `feature/ouverture-session` | `b5d3216` | Ouverture d'une session et generation du code de presence (EF1, RG1) | **#1** | 2 |
| 2026-09-25 | `develop` | `66ef39f` | Merge pull request #18 (feature/ouverture-session → develop) | **#1** · PR #18 | 2 |
| 2026-09-25 | `feature/presence-code` | `389fbe7` | Enregistrement d'une presence par code (EF2, RG1, RG14) + tests B6 | **#2** | 2 |
| 2026-09-25 | `develop` | `f4bf45a` | Merge pull request #19 (feature/presence-code → develop) | **#2** · PR #19 | 2 |
| 2026-09-25 | `feature/depot-exercice` | `2a8dd73` | Depot du lien d'exercice et remplacement tant qu'il n'est pas relu (EF3, EF10, RG11, RG12) | **#3** | 2 |
| 2026-09-25 | `develop` | `3fd10db` | Merge pull request #20 (feature/depot-exercice → develop) | **#3** · PR #20 | 2 |
| 2026-09-25 | `feature/affectation-relecteur` | `e67416c` | Affectation aleatoire d'un relecteur parmi les presents (EF4, RG5, RG6) | **#5** | 2 |
| 2026-09-25 | `develop` | `08d8af1` | Merge pull request #21 (feature/affectation-relecteur → develop) | **#5** · PR #21 | 2 |
| 2026-09-25 | `feature/rendu-relecture` | `ae13810` | Rendu d'une note 0-20 definitive sur une relecture (EF5, RG4, RG8, RG9) | **#6** | 2 |
| 2026-09-25 | `develop` | `378e8aa` | Merge pull request #22 (feature/rendu-relecture → develop) | **#6** · PR #22 | 2 |
| 2026-09-25 | `feature/cloture-session` | `0fba006` | Cloture de session par le formateur, distincte de l'expiration du code (EF6, RG2, RG11, RG16) | **#7** | 2 |
| 2026-09-25 | `develop` | `5b2dbea` | Merge pull request #23 (feature/cloture-session → develop) | **#7** · PR #23 | 2 |
| 2026-09-25 | `feature/tableau-progression` | `95522f4` | Tableau de progression de la promotion (EF8, RG15, RG17) | **#4** | 2 |
| 2026-09-25 | `develop` | `15c7277` | Merge pull request #24 (feature/tableau-progression → develop) | **#4** · PR #24 | 2 |
| 2026-09-25 | `feature/consultation-notes` | `27b946e` | Consultation des notes et commentaires sans identite du relecteur (EF11, RG7) | **#8** | 2 |
| 2026-09-25 | `develop` | `0cc042f` | Merge pull request #25 (feature/consultation-notes → develop) | **#8** · PR #25 | 2 |
| 2026-09-25 | `feature/choix-nom` | `b4bd503` | Liste des promotions et des etudiants pour choisir son nom sans mot de passe (EF11, Q1) | **#11** | 2 |
| 2026-09-25 | `develop` | `22986b2` | Merge pull request #26 (feature/choix-nom → develop) | **#11** · PR #26 | 2 |
| 2026-09-25 | `feature/remplacement-lien` | `1151d65` | Remplacement du lien d'exercice tant qu'aucune note n'est rendue (EF10, RG12) | **#10** | 2 |
| 2026-09-25 | `develop` | `aa38a05` | Merge pull request #27 (feature/remplacement-lien → develop) | **#10** · PR #27 | 2 |
| 2026-09-25 | `feature/presence-formateur` | `0cfa69a` | Presence relevee par le formateur tracée et visible au tableau (EF9, RG13, RG14) | **#9** | 2 |
| 2026-09-25 | `develop` | `b5151f9` | Merge pull request #28 (feature/presence-formateur → develop) | **#9** · PR #28 | 2 |
| 2026-09-25 | `develop` | `84b85ed` | docs: suivi Git — PR #18 à #28, issues #1 à #12 fermées | — | 2 |
| 2026-09-25 | `feature/api-layer-next` | `00f220b` | Ecran formateur, etudiant et relecteur + couche d'appels API unique (F2, F3) | **#14** | 2 |
| 2026-09-25 | `develop` | `037283f` | Merge pull request #29 (feature/api-layer-next → develop) | **#14** · PR #29 | 2 |
| 2026-09-25 | `infra/dockerisation` | `022ec9b` | Dockerisation postgres + backend + frontend en une commande (demarrage, T2) | **#13** | 2 |
| 2026-09-25 | `develop` | `6b66175` | Merge pull request #30 (infra/dockerisation → develop) | **#13** · PR #30 | 2 |
| 2026-09-25 | `develop` | *(ce commit)* | docs: suivi Git — PR #29 et #30, issues #13 et #14 fermées | — | 2 |

---

## Issues (backlog)

16 issues ouvertes le 2026-09-25, étiquetées `must` / `should` / `could` — **10 · 5 · 1** (10 `must`, 5 `should`, 1 `could`).
La colonne **Références** les relie au cahier des charges et au contrat ; la colonne **État** donne la PR de fermeture.

| # | Titre (extrait) | Label | Références | État |
|---|---|---|---|---|
| 1 | Ouvrir une session et obtenir un code de présence | `must` | EF1 · RG16 | `closed` · PR #18 |
| 2 | Marquer ma présence en saisissant le code | `must` | EF2 · RG14, RG1, RG2 | `closed` · PR #19 |
| 3 | Déposer le lien de mon exercice | `must` | EF3 · RG11 | `closed` · PR #20 |
| 4 | Voir le tableau de ma promotion | `must` | EF6 · RG15, RG17 | `closed` · PR #24 |
| 5 | Être affecté à la relecture d'un pair | `must` | EF4 · RG5, RG6 | `closed` · PR #21 |
| 6 | Rendre une note entière 0–20 et un commentaire | `must` | EF5 · RG8, RG9, RG4 | `closed` · PR #22 |
| 7 | Clôturer la session (**trou principal**, cahier §7.2) | `must` | EF7 · RG11, RG16 | `closed` · PR #23 |
| 8 | Consulter mes notes sans voir le nom du relecteur | `should` | EF8 · RG7 | `closed` · PR #25 |
| 9 | Ajouter une présence manuelle tracée `FORMATEUR` | `should` | EF9 · RG13 | `closed` · PR #28 |
| 10 | Remplacer le lien tant qu'il n'est pas relu | `should` | EF10 · RG12 | `closed` · PR #27 |
| 11 | Choisir mon nom dans la liste sans mot de passe | `should` | EF11 · Q1 | `closed` · PR #26 |
| 12 | Flyway, schéma `V1` conforme à D2, seed `V2` | `must` | B5 · ENF4 | `closed` · PR #17 |
| 13 | Dockeriser `postgres` + `backend` + `frontend` | `must` | démarrage · ENF4 | `closed` · PR #30 |
| 14 | Couche d'appels API Next.js avec états de charge | `must` | F3 · ENF3, ENF6 | `closed` · PR #29 |
| 15 | Blocage après 5 codes erronés | `could` | EF12 · RG3 | `open` |
| 16 | Journal d'étape, changelog et soumission | `should` | journal 5 pts | `open` |

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
