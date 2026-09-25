# Journal de bord — 257 · Brahimi Talla

> Une entrée **par étape**, écrite **au moment où je la termine**, pas à la fin de la journée.
> Trois lignes suffisent. Un journal rédigé d'un bloc juste avant de soumettre se repère
> immédiatement dans l'historique Git et ne compte pas.

Chaque entrée répond aux trois mêmes questions :

- **Fait** — ce que je viens de terminer
- **Bloqué** — ce qui m'a coûté du temps, et combien
- **IA** — ce que j'ai demandé, et **comment j'ai vérifié la réponse**

---

## Étape 1 — Analyse et conception

**Fait :** `docs/CAHIER_DES_CHARGES.md` en 10 sections (12 exigences fonctionnelles, 17 règles de
gestion, contradictions tranchées en §7.1, trous documentés en §7.2), les **4** diagrammes Mermaid
D1 à D4 (D4 en bonus), **16 issues** créées et étiquetées `must`/`should`/`could` (9 · 6 · 1),
`api/contrat.yaml` complété : **14 opérations**, dont les 5 imposées **non modifiées**, le tout
poussé sur `develop` avec les jalons remontés sur `main`.

**Bloqué :** 10 min sur le contrat. En ajoutant `GET /api/sessions` à côté du `POST` imposé, j'ai
créé une **clé YAML dupliquée** : le `POST` imposée disparaissait silencieusement du document —
violation de `B2` invisible à la lecture. Détecté par un script de validation (charge les deux
fichiers, compare les 5 opérations imposées champ par champ, refuse les clés dupliquées), corrigé
en fusionnant `get` et `post` sous une seule entrée de chemin. Le script repasse : 5/5 intactes.
Second point : la contradiction **Q10 / Q15** a pris 20 min de réflexion, tranchée **en faveur de
Q15** (cahier §7.1) — l'exemple du modèle de journal penche pour Q10, je prends l'autre sens et
j'écris pourquoi.

**IA :** demandé : rédaction du cahier des charges, des diagrammes, des corps d'issues, et la
complétion du contrat. Vérifié : chaque affirmation du cahier porte sa source (`Qx`, `contrat`,
ou `*décision du candidat*`), script de validation YAML exécuté avant chaque commit, titres d'issues
relus un par un (« est-ce que le client comprend ce qu'il obtient ? »), 3 tâches techniques
supprimées du backlog au profit de résultats utilisateur — sauf `Flyway`, `Docker` et `couche API`
qui sont des critères **imposés** par le sujet (B5, démarrage, F3).

---

## Étape 2 — Première version

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 3 — Enveloppe

**Fait :**

**Bloqué :**

**IA :**

**Ce que je sors du périmètre pour absorber le changement, et pourquoi :**

---

## Étape 4 — Version finale

**Fait :**

**Bloqué :**

**IA :**

---

## Étape 5 — Soumission

**Fait :**

**Bloqué :**

**IA :**

**Ce que je referais autrement avec une journée de plus :**
