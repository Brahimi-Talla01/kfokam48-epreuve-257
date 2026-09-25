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

**Fait :** Backend Spring Boot (Java 21) conforme B1-B6 : contrôleur/service/repository/DTO
séparés, validation + `@RestControllerAdvice` centralisé, schéma Flyway `V1` + seed `V2`,
43 tests (unitaires + intégration, H2) tous verts. Les 14 issues `must` sont fermées par
14 PR sur `develop` (#17 à #30). Frontend Next.js 15 : 3 écrans (`/formateur`, `/etudiant`,
`/relecteur`), couche d'appels API unique `src/lib/api/`, moyenne jamais recalculée côté
client, `npm run build` vert. Dockerisation : `backend/Dockerfile` (multi-stage maven → jre),
`frontend/Dockerfile` (multi-stage node:20-alpine, next build → next start),
`docker-compose.yml` (postgres + backend + frontend, healthchecks). Validé de bout en bout :
`docker compose up --build` sur poste propre → API `:8080`, frontend `:3000`, données de
démo chargées (session ouverte, code `K7F2M48`), présence enregistrée via l'API en conteneur.
README réécrit avec la commande unique de démarrage et le choix du frontend justifié (F1).

**Bloqué :** Le premier Dockerfile frontend copiait un dossier `public/` qui n'existe pas
dans ce projet (`create-next-app` sans assets statiques) : le build Docker échouait sur
`COPY --from=build /app/public`. Corrigé en retirant cette ligne, revalidé par
`docker compose build` puis `docker compose up` avec smoke test API + frontend.

**IA :** demandé : rédaction des Dockerfile et du docker-compose.yml, du README. Vérifié :
`docker compose up --build` exécuté réellement (pas de relecture de code seule), healthchecks
`postgres`/`backend` passés à `healthy` avant démarrage du service suivant, `curl` sur
`GET /api/tableau` et `POST /api/presences` avec le code de démo, `GET /` et `/etudiant` du
frontend en `200` — le tout à travers les conteneurs, pas en local.

---

## Étape 3 — Enveloppe

**Fait :** Enveloppe lue (bug + changement de besoin). **Bug** (issue #31, ouverte avant
tout code) : `PresenceService.marquer` faisait un contrôle d'existence puis une écriture
non atomiques ; sous deux `POST /api/presences` concurrents sur la même présence, la
seconde écriture violait la contrainte UNIQUE et l'exception n'était pas interceptée,
remontant en `500` au lieu d'un `409 DEJA_PRESENT` propre — d'où la perte apparente côté
client. Test rouge d'abord (`PresenceConcurrenteTest`, deux threads via `CyclicBarrier`,
15 itérations), commit séparé, puis correctif (`catch DataIntegrityViolationException`
→ `409`), 44/44 tests verts, branche `fix/presence-concurrente` (PR #32) distincte de
l'évolution. **Changement de besoin** (issue #33) : deux relecteurs par exercice, note
= moyenne si les deux ont rendu (définitive), note seule marquée provisoire si un seul
a rendu, définitive d'emblée si un seul pair était éligible (cas limite tranché et écrit
au cahier §7.3). Analyse mise à jour dans un commit dédié (cahier §6/§7.3, D2, D4)
**avant** tout code de l'évolution. Migration `V3__deux_relecteurs.sql` : `V1`/`V2`
jamais modifiées ; la contrainte `uk_relecture_exercice` (1 relecteur) devient
`uk_relecture_exercice_relecteur` (2 relecteurs distincts) — la base du seed reste
valide sans transformation. Contrat (`api/contrat.yaml`) mis à jour : `GET
/api/exercices/{id}/relectures` expose désormais `{noteRetenue, provisoire,
relectures[]}` ; `POST /api/relectures` renvoie un tableau (jusqu'à deux relectures
créées d'un coup). Backend (`RelectureService`, tests) et frontend (écran étudiant :
note retenue + badge « provisoire ») mis à jour, 46/46 tests backend verts, `npm run
build` vert. Correctif et évolution séparés (2 branches, 2 PR : #32 et une PR dédiée
à l'issue #33).

**Bloqué :** ~45 min sur la migration `V3`. `ALTER TABLE relecture DROP CONSTRAINT
uk_relecture_exercice` réussissait sans erreur, mais un index physique auto-généré par
H2 pour cette contrainte restait actif en arrière-plan car réutilisé comme support de
la clé étrangère `fk_relecture_exercice` — il continuait donc à interdire un second
relecteur par exercice, même après le DROP CONSTRAINT et même après avoir ajouté la
nouvelle contrainte composite en premier. Diagnostiqué en lisant le message d'erreur H2
exact (« l'index appartient à la contrainte fk_relecture_exercice ») plutôt que de
supposer un simple problème d'ordre SQL. Résolu en retirant explicitement la clé
étrangère avant l'ancienne contrainte, puis en la recréant après la nouvelle — l'ordre
inverse laisse un index orphelin. Vérifié en relançant la suite complète (46/46 verts)
et en confirmant que le même schéma reste valide en Postgres (`DROP CONSTRAINT` y
supprime nativement l'index associé, donc la séquence reste correcte sur les deux
moteurs). Second point, plus rapide (10 min) : deux nouveaux tests ajoutaient des
présences à des étudiants du seed déjà comptés par des assertions strictes d'anciens
tests (`PresenceFormateurTest`, `TableauApiTest`) — corrigé en limitant les nouveaux
scénarios aux trois premiers étudiants de la promotion, jamais touchés par ces
assertions.

**IA :** demandé : rédaction de la migration V3, du service `RelectureService`
(affectation à deux, note agrégée), des tests de concurrence et d'affectation, du
contrat mis à jour, de l'écran étudiant. Vérifié : chaque test exécuté réellement
(`./mvnw test`) avant et après chaque correctif — jamais une lecture de code seule ;
le test de concurrence relu ligne à ligne pour confirmer qu'il reproduit vraiment la
course (15 itérations, jamais de `500`, jamais deux `201`) ; la migration testée sur H2
**et** mentalement rejouée sur la syntaxe Postgres réelle (pas seulement H2) avant de la
figer ; `npm run build` et `npx tsc --noEmit` exécutés après les changements frontend.

**Ce que je sors du périmètre pour absorber le changement, et pourquoi :** l'issue **#15**
(blocage 2 minutes après 5 codes erronés, RG3, priorité `could` — la plus basse du
backlog) sort du périmètre de cette livraison. Le changement de besoin de l'étape 3 est
un `must` arrivé tard qui a consommé le temps qui lui était initialement réservé ; entre
un `could` déjà en bas de liste et un `must` déjà engagé, c'est le `could` qui cède la
place. Décision écrite également au cahier des charges §7.3 et en commentaire sur
l'issue #15 elle-même, pour que le sacrifice soit traçable des deux côtés.

---

## Étape 4 — Version finale

**Fait :** `CHANGELOG.md` créé, cohérent avec l'historique Git réel (une entrée par
jalon/étape, renvoyant aux issues et PR de `assets/SUIVI_GIT.md`), section Backlog à
jour (16 issues fermées, 2 ouvertes avec leur état réel). README revalidé **depuis un
clone vierge** dans un dossier temporaire séparé du dépôt de travail :
`git clone` puis `docker compose up --build` en une seule commande → API `:8080`,
frontend `:3000`, données de démo chargées, `GET /api/tableau` et pages `/` et
`/etudiant` répondent `200`. Conteneurs et volume nettoyés après vérification. Backlog
trié : `#15` documentée comme sacrifiée (étape 3), `#16` en cours de fermeture (ce
fichier + `CHANGELOG.md`, `SOUMISSION.md` restant à l'étape 5).

**Bloqué :** rien de notable — la dockerisation ayant déjà été validée à l'étape 2 et
revalidée sur Postgres réel à l'étape 3, le clone vierge de l'étape 4 s'est déroulé sans
surprise.

**IA :** demandé : rédaction du `CHANGELOG.md` à partir de l'historique Git réel.
Vérifié : chaque entrée du changelog confrontée à `assets/SUIVI_GIT.md` (aucun élément
inventé, aucun oublié) ; le clone vierge et `docker compose up --build` exécutés
réellement dans un dossier séparé, pas seulement relus.

---

## Étape 5 — Soumission

**Fait :** `docs/SOUMISSION.md` complété (technique, ce qui a été livré, backlog restant).
Tout le travail poussé sur `develop`, puis `main` synchronisée une dernière fois en
fast-forward (`d5af9c6`) — c'est ce commit qui est déclaré. Dépôt vérifié **public**
et accessible sans authentification (`curl` non authentifié → `200`, API GitHub →
`private: false`), hash confirmé à 40 caractères (`git rev-parse main | wc -c`). Les
trois jalons `[JALON] analyse` (`f730f76`), `[JALON] v0.1` (`974ff81`),
`[JALON] v1.0` (`9382fa7`) vérifiés présents, poussés et dans l'ordre sur `main`.
Issue #16 fermée.

**Bloqué :** rien de bloquant. Seul point d'attention traité consciemment : le hash
déclaré dans `SOUMISSION.md` ne peut, par construction, pas inclure le commit qui
l'écrit lui-même — le fichier a donc été rempli avec le hash de `main` **juste avant**
d'être commité, en acceptant que ce dernier commit de documentation vienne
chronologiquement après le hash déclaré (« tout ce que tu pousses après est ignoré »,
ce qui est exactement l'usage prévu).

**IA :** demandé : relecture de `docs/SOUMISSION.md` avant remplissage final. Vérifié :
accessibilité publique du dépôt testée par une requête réelle non authentifiée (pas une
supposition), longueur du hash comptée par script plutôt qu'à l'œil, ordre des trois
jalons relu directement dans `git log`.

**Ce que je referais autrement avec une journée de plus :** industrialiser la
détection de l'index orphelin H2 rencontrée à l'étape 3 (§3, journal étape 3) dans un
test de migration dédié, pour la repérer avant l'exécution de la suite complète.
Implémenter l'issue #15 (blocage après 5 codes erronés), sacrifiée faute de temps
après le changement de besoin tardif de l'étape 3.
