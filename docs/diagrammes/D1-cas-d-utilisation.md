# D1 — Cas d'utilisation

> Sources : `SUJET.pdf` §2, `CLIENT.md` Q1–Q16, `api/contrat.yaml`.
> Chaque cas renvoie à l'exigence (`EFx`) qu'il satisfait.

```mermaid
usecaseDiagram
    actor Formateur as F
    actor Etudiant as E
    actor Relecteur as R
    rectangle "KFOKAM48 — Présence et relecture" {
        usecase "Ouvrir une session et obtenir le code" as UC1
        usecase "Clôturer la session" as UC2
        usecase "Consulter le tableau de la promotion" as UC3
        usecase "Ajouter une présence manuelle (source FORMATEUR)" as UC4
        usecase "Choisir son nom dans la liste" as UC5
        usecase "Marquer sa présence avec le code" as UC6
        usecase "Déposer le lien de son exercice" as UC7
        usecase "Remplacer le lien de son exercice" as UC8
        usecase "Consulter ses notes et commentaires" as UC9
        usecase "Recevoir l'assignation d'un exercice" as UC10
        usecase "Rendre une note et un commentaire" as UC11
    }
    F --> UC1
    F --> UC2
    F --> UC3
    F --> UC4
    E --> UC5
    E --> UC6
    E --> UC7
    E --> UC8
    E --> UC9
    E --> UC10
    R --> UC11
    UC6 ..> UC5 : <<include>>\n(le code est saisi par un étudiant identifié)
    UC8 ..> UC7 : <<extend>>\n tant que relecture EN_ATTENTE (RG12)
    UC11 ..> UC10 : <<include>>
    UC10 ..> UC6 : <<extend>>\n rélecteur = étudiant présent à la session (RG6)
```

## Tableau associatif

| Cas d'utilisation | Acteur | Exigence | Opération(s) du contrat |
|---|---|---|---|
| Ouvrir une session et obtenir le code | Formateur | EF1 | `POST /api/sessions` |
| Clôturer la session | Formateur | EF7 | `POST /api/sessions/{id}/cloturer` *(ajoutée)* |
| Consulter le tableau de la promotion | Formateur | EF6 | `GET /api/tableau` |
| Ajouter une présence manuelle | Formateur | EF9 | `POST /api/presences` (`source=FORMATEUR`) |
| Choisir son nom dans la liste | Étudiant | EF11 | `GET /api/promotions/{id}/etudiants` *(ajoutée)* |
| Marquer sa présence avec le code | Étudiant | EF2 | `POST /api/presences` |
| Déposer le lien de son exercice | Étudiant | EF3 | `POST /api/exercices` |
| Remplacer le lien de son exercice | Étudiant | EF10 | `PUT /api/exercices/{id}/lien` *(ajoutée)* |
| Consulter ses notes et commentaires | Étudiant | EF8 | `GET /api/exercices/{id}/relectures` *(ajoutée)* |
| Recevoir l'assignation d'un exercice | Étudiant (relecteur) | EF4 | `POST /api/relectures` *(ajoutée)* |
| Rendre une note et un commentaire | Relecteur | EF5 | `POST /api/relectures/{id}` |

## Notes de modélisation

- **Pas d'acteur « système »** : l'expiration du code (RG1) et le tirage au hasard du relecteur (RG6) sont des comportements internes, pas des cas d'utilisation.
- **Le relecteur n'a pas de cas d'utilisation propre** : il reçoit l'assignation (UC10) puis rend (UC11). C'est un étudiant dans un état (§2 du cahier).
- **UC2 « Clôturer » est ajouté** : `Q3`, `Q10` et `Q12` en dépendent sans que l'opération existe dans le contrat — voir §7.2 du cahier (trou principal).
- **Exclusion visible** : aucun cas « s'authentifier » — `Q1` l'interdit explicitement.
