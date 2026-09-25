# Cahier des charges — KFOKAM48 · Présence et relecture par les pairs

**Auteur :** Brahimi Talla · matricule **257** (dépôt `kfokam48-epreuve-257` — *⚠️ matricule complet à confirmer*)
**Version :** 1 · **Date :** 25/09/2026
**Frontend choisi :** **Next.js 15** (App Router), parce que ses trois écrans se naturellement en trois routes, que son build de production est un simple `next start` dans le conteneur, et qu'il donne une couche serveur sans imposer de framework lourd pour une application à trois écrans.

> Sources : `SUJET.pdf` (§1 à §6), `CLIENT.md` (Q1–Q16), `api/contrat.yaml`, `ENVELOPPE_etape3.pdf`.
> Chaque décision renvoie à sa source. Une décision sans source est une décision qu'on ne peut pas discuter.

---

## 1. Contexte et objectif

La direction de la formation KFOKAM48 organise des sessions de cours réparties en promotions. Aujourd'hui, la présence se relève à la main et les exercices se relisent sans trace : impossible de savoir qui était présent, qui a rendu, ni quelle note a été obtenue.

L'application répond à cinq besoins écrits par le client lui-même :

1. un formateur ouvre une session de cours et obtient un **code de présence** ;
2. un étudiant saisit ce code pour **marquer sa présence** ;
3. un étudiant **dépose le lien** de son exercice pour une session ;
4. un étudiant est assigné à la **relecture** de l'exercice d'un pair : note et commentaire ;
5. le formateur voit un **tableau** : présence et moyenne des notes par étudiant.

L'objectif n'est pas seulement de livrer une application : il faut transformer une demande floue et contradictoire en un **spécification numérotée, testable et tenable**, puis la livrer par étapes avec un historique Git lisible.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire | Ce qu'il ne peut pas faire |
|---|---|---|
| **Formateur** | Ouvrir une session, obtenir le code, clôturer la session, ajouter une présence manuellement (tracée), consulter le tableau par promotion | Modifier une note rendue ; relire un exercice comme un étudiant s'il n'est pas dans la promotion |
| **Étudiant** | Choisir son nom dans une liste (Q1), marquer sa présence, déposer puis remplacer le lien de son exercice, consulter ses notes et commentaires | Relire son propre exercice (Q5) ; marquer une présence après clôture (Q3) |
| **Relecteur** *(un étudiant désigné)* | Recevoir l'assignation d'un exercice d'un pair, rendre une note entière 0–20 et un commentaire | Relire son propre exercice ; modifier sa note après validation ; voir… *rien à voir : il ne voit que l'exercice à relire* |
| **Système** | Générer le code, l'expirer après 15 min, tirer au hasard le relecteur parmi les présents, calculer la moyenne | — |

**Décision de modélisation :** le **relecteur n'est pas un acteur distinct** ; c'est un **étudiant dans un état** (il a reçu une relecture). Un étudiant est tour à tour auteur, relecteur et observateur de ses propres notes. Cette réponse a une conséquence directe sur le modèle : il n'y a **pas de table `Relecteur`**, seulement une table `Relecture` qui relie un `Exercice` à un `Etudiant`.

## 3. Périmètre

**Inclus dans cette version :**

- Ouverture / clôture d'une session et génération du code de présence.
- Marquage de présence par code, avec expiration, unicité et source (`ETUDIANT` / `FORMATEUR`).
- Dépôt (et remplacement) du lien d'exercice pour une session.
- Affectation d'un relecteur unique par exercice, tiré au hasard parmi les étudiants présents.
- Rendu d'une note entière 0–20 et d'un commentaire, avec interdiction de se relire soi-même.
- Tableau de synthèse du formateur : présences, exercices déposés, moyenne, relectures en attente.
- Consultation par l'étudiant de ses notes et commentaires, **sans le nom du relecteur**.
- Jeu de données de démonstration chargé au démarrage.

**Explicitement exclu :**

- **Authentification et gestion des comptes** — l'étudiant choisit son nom dans une liste, le formateur n'est pas authentifié (`Q1` : « Ne perdez pas de temps là-dessus »). *Conséquence assumée : toute personne ayant l'URL peut agir en tant que formateur ; c'est un exercice, pas une mise en production.*
- **Notifications** (e-mail, push) : aucune exigence du client.
- **Dépôt de fichiers** : seul un **lien** est stocké (`Q13`).
- **Historique / audit des notes** : seule la valeur courante compte.
- **Multi-formateur, multi-centre, rôles et permissions**.
- **Application mobile native** : l'écran de présence est responsive, c'est tout.
- **Statistiques avancées** (courbes, export CSV, PDF) : le tableau demandé en `Q16` suffit.

*Ce que tu exclus compte autant que ce que tu inclus. Un périmètre sans exclusion n'est pas un périmètre.*

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| **EF1** | Le formateur ouvre une session et obtient un code | Quand j'envoie `POST /api/sessions` avec `titre` et `promotionId`, je reçois `201` avec `{id, code, ouvertureAt, expirationAt}` et le code s'affiche à l'écran | Must |
| **EF2** | L'étudiant marque sa présence avec un code | Quand je saisis un code valide et non expiré, je reçois `201` et ma présence apparaît dans le tableau du formateur | Must |
| **EF3** | L'étudiant dépose le lien de son exercice | Quand j'envoie une URL valide pour une session non clôturée, je reçois `201 {id, statut}` et le dépot compte dans le tableau | Must |
| **EF4** | Le système affecte l'exercice à un relecteur | Quand un exercice est déposé et qu'au moins un autre étudiant de la session était présent, une relecture `EN_ATTENTE` est créée pour un pair tiré au hasard parmi les présents (`Q7`) | Must |
| **EF5** | Le relecteur rend note et commentaire | Quand un relecteur envoie une note entière 0–20 et un commentaire sur sa relecture, il reçoit `200` ; une seconde soumission reçoit `409 RELECTURE_DEJA_RENDUE` | Must |
| **EF6** | Le formateur voit le tableau | Quand j'appelle `GET /api/tableau?promotionId=`, je reçois par étudiant `{etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente}` ; `moyenne` est `null` tant qu'aucune note n'existe | Must |
| **EF7** | Le formateur clôture la session | Quand la session est clôturée, un nouveau dépôt d'exercice reçoit `409`, et le statut de la session passe à `CLOTUREE` dans l'écran formateur | Must |
| **EF8** | L'étudiant consulte ses notes et commentaires | Quand j'ouvre mon exercice relu, je vois la note et le commentaire, et **jamais le nom du relecteur** (`Q8`) | Should |
| **EF9** | Le formateur ajoute une présence à la main | Quand le formateur ajoute une présence, elle est enregistrée avec `source = FORMATEUR` et apparaît marquée comme telle dans le tableau (`Q14`) | Should |
| **EF10** | L'étudiant remplace le lien de son exercice | Quand la relecture est encore `EN_ATTENTE`, le remplacement du lien répond `200` ; dès qu'une note est rendue, il répond `409` (`Q13`) | Should |
| **EF11** | L'étudiant choisit son nom sans mot de passe | Quand j'ouvre l'écran étudiant, la liste des étudiants de la promotion s'affiche et ma sélection est utilisée comme `etudiantId` dans la suite (`Q1`) | Should |
| **EF12** | Blocage après 5 codes erronés | Quand un étudiant se trompe 5 fois, la 6e tentative est refusée pendant 2 minutes (`Q4`) — *voir §7 : code `400 TROP_DE_TENTATIVES`, pour ne pas ajouter de code de statut à l'opération imposée* | Could |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| **ENF1** | L'écran de saisie du code est utilisable sur un téléphone (viewport 360 px) | Ouvrir `/etudiant` en mode responsive : bouton et champ atteignables sans zoom |
| **ENF2** | Le tableau répond en moins de 2 s pour une promotion de 60 étudiants | Chronométrer `GET /api/tableau` avec un seed élargi à 60 étudiants |
| **ENF3** | Toute erreur est un JSON `{code, message}` lisible, **jamais** une stack trace | Casser volontairement un appel (code expiré) et inspecter la réponse : status `410`, corps `{code, message}` |
| **ENF4** | L'application démarre avec **une seule commande** et des données de démonstration | `git clone` dans un dossier vide puis `docker compose up` → application sur `:3000` déjà peuplée |
| **ENF5** | Les tests tournent sur un poste vierge, sans base locale | `./mvnw test` avec aucun service en cours (tests sur H2) |
| **ENF6** | Aucune règle métier dupliquée côté client | Revue du code front : aucun calcul de moyenne, aucune décision d'expiration dans `frontend/src` ; la moyenne vient de l'API (`F3`) |
| **ENF7** | Le build de production du front passe | `npm run build` dans `frontend/` et dans l'image Docker |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| **RG1** | Le code de présence expire **15 minutes après l'ouverture** de la session ; passé ce délai, `POST /api/presences` répond `410 CODE_EXPIRE` | `Q2` |
| **RG2** | Aucune présence ne peut être marquée **après la clôture** de la session | `Q3` |
| **RG3** | Après **5 codes erronés**, l'étudiant est bloqué **2 minutes** | `Q4` |
| **RG4** | Un étudiant **ne peut jamais relire son propre exercice** → `403 AUTO_RELECTURE` | `Q5` |
| **RG5** | Un exercice a **un seul** relecteur | `Q6` |
| **RG6** | Le relecteur est choisi **au hasard** parmi les étudiants **présents à cette session**, auteur exclu | `Q7` |
| **RG7** | L'étudiant relu voit la note et le commentaire, **jamais le nom du relecteur** | `Q8` |
| **RG8** | La note est un **entier de 0 à 20** ; sinon `400 NOTE_INVALIDE` | `Q9` |
| **RG9** | La note est **définitive une fois envoyée** : toute nouvelle soumission répond `409 RELECTURE_DEJA_RENDUE` | `Q15` (**contradiction tranchée, voir §7**) |
| **RG10** | Un exercice non relu reste au statut `EN_ATTENTE` et apparaît comme tel dans le tableau (`relecturesEnAttente`) | `Q11` |
| **RG11** | Le dépôt d'exercice reste ouvert **jusqu'à la clôture** de la session par le formateur | `Q12` |
| **RG12** | Le lien est remplaçable **tant que la relecture n'a pas été rendue** (statut `EN_ATTENTE`) | `Q13` + §7 |
| **RG13** | Une présence ajoutée par le formateur porte `source = FORMATEUR` et est affichée comme telle | `Q14` |
| **RG14** | Une présence est **unique** par couple (étudiant, session) → `409 DEJA_PRESENT` | `contrat` |
| **RG15** | Le tableau affiche, par étudiant : présences, exercices déposés, moyenne des notes reçues, relectures en attente | `Q16` |
| **RG16** | Une session a un statut `OUVERTE` → `CLOTUREE` ; **l'expiration du code (RG1) et la clôture (RG16) sont deux événements distincts** : le code expire, la session se clôture | *décision du candidat, comble un trou — voir §7* |
| **RG17** | La moyenne affichée est calculée **côté serveur uniquement**, arrondie à 2 décimales ; `null` si aucune note | `F3` + `Q16` |

## 7. Zones d'ombre, hypothèses et contradictions

### 7.1 Contradictions relevées

| Réponses en conflit | Ce que j'ai choisi | Pourquoi |
|---|---|---|
| **`Q10`** « la note est modifiable tant que le formateur n'a pas clôturé la session » **vs** **`Q15`** « une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir » | **`Q15` retenue** → **RG9** : la note est définitive dès l'envoi, seconde soumission = `409` | **(1)** Le contrat imposé attend un `409 RELECTURE_DEJA_RENDUE` *sans* condition de clôture : retenir `Q10` rendrait cet état inatteignable dans le cas nominal, donc contraire à `B2`. **(2)** `Q15` est opérationnellement complète ; `Q10` dépend de la clôture, qui **n'était définie nulle part** avant `RG16` — une règle qui dépend d'un concept absent n'est pas applicable en l'état. **(3)** `Q15` cite un « validé » du relecteur : on l'interprète comme l'envoi effectif de la note, ce qui rend la règle auto-suffisante. *La lecture `Q10` reste défendable : si elle avait été retenue, le `409` n'aurait plus été délivré qu'après clôture, à documenter explicitement dans le contrat. Le choix est écrit, il est discutable, c'est ce qui est demandé.* |

### 7.2 Trous et hypothèses

| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Conséquence |
|---|---|---|---|
| **Le cycle de vie de la session n'existe pas** — `Q3` (« fin de la session »), `Q10` et `Q12` (« clôture par le formateur ») supposent un événement jamais défini ; aucune opération de clôture n'est dans le contrat | *trou repéré, aucune réponse client* | **RG16** : statut `OUVERTE`/`CLOTUREE` + opération **`POST /api/sessions/{id}/cloturer`** ajoutée au contrat ; `expirationAt` = fin du code, `clotureAt` = fin de la session | `RG2`, `RG11` deviennent applicables ; D2 et D3 mentionnent le statut |
| **`Q6` + `Q11` : un seul relecteur qui ne rend jamais → l'étudiant n'a aucune note, jamais** | `Q6` + `Q11` | Signalé ici et dans le backlog ; la réponse viendra de l'évolution du besoin | *point de vigilance : c'est exactement l'angle mort que le changement de besoin de l'étape 3 vient toucher* |
| **`Q2` (expiration 15 min) ≠ `Q3` (fin de session)** | `Q2`, `Q3` | Deux notions distinctes, **jamais confondues** : `expirationAt` pour le code, `clotureAt` pour la session | Écran formateur affiche les deux |
| **`Q7` : quand l'affectation se déclenche-t-elle, et s'il n'y a aucun pair présent ?** | `Q7` | Affectation **au dépôt de l'exercice**. Si aucun pair éligible : relecture `EN_ATTENTE` **sans relecteur**, visible dans le tableau (`relecturesEnAttente`) | Ajout de `POST /api/relectures` (création), le `POST /api/relectures/{id}` du contrat ne fait que **rendre** une relecture existante |
| **`Q13` : « tant que personne n'a commencé à le relire » — l'état `commencé` n'existe pas** | `Q13` | « Commencé » = **rendu** : tant que le statut est `EN_ATTENTE`, le lien est remplaçable (`RG12`, `EF10`) | Un seul état, pas d'ajout de colonne |
| **`Q4` : aucun code HTTP prévu pour le blocage** | `Q4` | Réponse en **`400`** avec `code = TROP_DE_TENTATIVES` : on refuse d'ajouter un code de statut à une opération imposée (`B2`). **Priorité Could** — implémenté en dernier | Contrat des 5 opérations intact |
| **`Q1` : qui crée promotions et étudiants ? Comment le formateur est-il identifié ?** | `Q1` | **Seed** (`V2__seed_demo.sql`) crée promotions et étudiants ; le formateur n'est **pas identifié** (exclu, §3) | `GET /api/promotions` et `GET /api/promotions/{id}/etudiants` ajoutés pour le choix sans mot de passe |
| **`Q14` : comment distinguer une présence ajoutée par le formateur si le corps imposé est `{code, etudiantId}` ?** | `Q14`, `contrat` | Champ **optionnel** `source` (défaut `ETUDIANT`) sur `POST /api/presences` : chemin, verbe, codes de statut et format d'erreur restent **identiques** au contrat | `RG13`, `EF9` |

**Exclusions assumées :** authentification (`Q1`), notifications, historique des notes, statistiques (§3). Toute demande nouvelle sortant de ce périmètre déclenche une **réécriture de cette section**, pas une extension silencieuse.

## 8. Contraintes techniques

**Imposées par le sujet** (non négociables) :

| # | Contrainte | Engagement |
|---|---|---|
| **B1** | Java 17+, Maven, **wrapper `mvnw` commité** | ☐ |
| **B2** | **`api/contrat.yaml` respecté à la lettre** : chemins, verbes, codes de statut, format d'erreur | ☐ |
| **B3** | Contrôleur / service / repository séparés ; **aucune entité JPA en JSON** → DTO | ☐ |
| **B4** | Validation des entrées + `@RestControllerAdvice` ; **jamais de stack trace** au client | ☐ |
| **B5** | **Flyway**, migrations commitées ; `ddl-auto=update` interdit hors tests | ☐ |
| **B6** | 1 test unitaire sur une règle réelle + 1 test d'intégration sur un endpoint, sur poste vierge | ☐ |
| **F1** | Framework déclaré et justifié **en une ligne** dans le README, build qui passe | **Next.js 15** — justification en tête de ce document |
| **F2** | **3 écrans** : formateur, étudiant, relecteur | `app/formateur`, `app/etudiant`, `app/relecteur` |
| **F3** | Couche d'appels API dédiée, états chargement/erreur, **aucune règle métier dupliquée** | `src/lib/api/` seul point d'accès |
| **Démar.** | `docker compose up` ou 3 commandes max, testé depuis un clone vierge, **avec données de démo** | `postgres` + `backend` + `frontend` |

**Choix arrêtés (hypothèses du candidat) :**

- **Frontend :** Next.js 15, App Router, TypeScript, composants client.
- **Base :** **PostgreSQL 16** en conteneur ; **H2** pour les tests (poste vierge).
- **Schéma :** Flyway — `V1__init.sql` (schéma, conforme à D2), `V2__seed_demo.sql` (données de démo), `V3__*` (ajouts ultérieurs, jamais de modification en place).
- **Conteneurs :** `backend/Dockerfile` (Maven → JRE), `frontend/Dockerfile` (`next build` → `next start`), `docker-compose.yml` avec `healthcheck` Postgres.
- **API :** Spring Boot 3, DTO systématiques, erreurs centralisées `{code, message}`.

## 9. Livrables

1. `docs/CAHIER_DES_CHARGES.md` (ce document), tenu à jour après chaque changement de besoin.
2. `docs/diagrammes/` : **D1** cas d'utilisation, **D2** classes/modèle (≡ migrations), **D3** séquence « marquer sa présence » (≡ codes HTTP), **D4** états-transitions de l'exercice *(bonus)* — tous en Mermaid texte.
3. Backlog en **issues** du dépôt : titre = un résultat, critères « quand… alors… », priorité, renvoi `EFx`/`RGx`.
4. `api/contrat.yaml` **figé avant le premier commit de code**.
5. `backend/` Spring Boot conforme à B1–B6, migrations Flyway.
6. `frontend/` Next.js 15 conforme à F1–F3.
7. `docker-compose.yml` + README d'installation testé depuis un clone vierge.
8. `docs/JOURNAL.md` : une entrée par étape.
9. `assets/PLAN_EPREUVE.md` et `assets/SUIVI_GIT.md` : plan d'exécution et traçabilité commits/PR.
10. `SOUMISSION.md` téléversé sur la plateforme avant 18h00.

## 10. Démarche prévue

| Étape | Ce que je livre | Jalon |
|---|---|---|
| **0** | Dépôt, `develop` par défaut, `.gitignore`, suivi Git | — |
| **1** | Ce cahier, 4 diagrammes, backlog en issues, contrat figé — **aucun code** | **`[JALON] analyse`** |
| **2** | Stories **Must** : backend + 3 écrans, Flyway, seed, Docker, 1 PR par issue ciblant `develop` | **`[JALON] v0.1`** |
| **3** | Enveloppe : issue d'abord, test qui échoue, correction en branche dédiée ; changement de besoin : analyse mise à jour, `V3`, contrat ajusté, sacrifice écrit, 2 PR séparées | — |
| **4** | `CHANGELOG.md`, README testé sur clone vierge, backlog trié | **`[JALON] v1.0`** |
| **5** | `SOUMISSION.md` sur la plateforme avant 18h00 | — |

**Definition of Done — une issue est terminée quand :**

- ses critères d'acceptation sont vérifiés **manuellement ou par un test** ;
- le code est sur `develop` via **une PR unique** qui la référence (`Closes #N`) ;
- `./mvnw test` et `npm run build` passent ;
- aucune règle métier n'a été dupliquée côté client ;
- le commit cite l'`EFx` / `RGx` concerné ;
- `assets/SUIVI_GIT.md` contient la ligne du commit et de la PR.

---

## Journal des révisions

| Version | Quand | Ce qui a changé et pourquoi |
|---|---|---|
| 1 | 25/09/2026 | Version initiale : analyse du sujet et de `CLIENT.md`, contradictions tranchées (§7), trous documentés, contrat complété. |
| 2 | *étape 3* | **Prévu :** mise à jour après ouverture de l'enveloppe (évolution du nombre de relecteurs) — un cahier des charges périmé est un cahier des charges mort. |
