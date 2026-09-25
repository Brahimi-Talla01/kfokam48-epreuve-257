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
| 2026-09-25 | `develop` | *(ce commit)* | docs: création du suivi Git (assets/SUIVI_GIT.md) | — | 0 |

---

### Comment ce fichier est tenu

1. Après chaque `git push`, ajouter la ligne correspondante (ou les lignes, s'il y a plusieurs commits).
2. Après chaque création / merge de PR, ajouter ou mettre à jour la ligne de la table **Pull requests** (`État` : `open` → `merged`).
3. La colonne **Étape** situe le travail dans la chronologie officielle de l'épreuve : `0` mise en place · `1` analyse · `2` v0.1 · `3` enveloppe · `4` final · `5` soumission.
4. Le fichier est poussé sur `develop` ; il est repris dans `main` lors des merges de jalon.
