# D3 — Séquence « marquer sa présence » (≡ codes HTTP)

> Ce diagramme est **contractuel** : chaque message porte le verbe, le chemin et le code de
> `api/contrat.yaml`. S'il diverge du contrat, c'est le diagramme qui a tort.
> Sources : `api/contrat.yaml` opération `POST /api/presences`, `CLIENT.md` Q1–Q4, Q14.

```mermaid
sequenceDiagram
    autonumber
    actor E as Étudiant
    participant UI as Front (Next.js)
    participant API as API Spring Boot
    participant DB as PostgreSQL

    Note over E,DB: Chemin nominal

    E->>UI: saisit le code affiché au tableau
    UI->>API: POST /api/presences  {"code":"K7F2","etudiantId":17}
    API->>DB: SELECT session WHERE code = ?
    alt session inexistante ou code différent
        API-->>UI: 400 {"code":"CODE_INCONNU","message":"..."}
    else session expirée (expiration_at dépassée) — RG1
        API-->>UI: 410 {"code":"CODE_EXPIRE","message":"..."}
    else session clôturée (statut CLOTUREE) — RG2 / RG16
        API-->>UI: 410 {"code":"SESSION_TERMINEE","message":"..."}
    else présence déjà marquée — RG14
        API->>DB: SELECT presence (session, etudiant)
        API-->>UI: 409 {"code":"DEJA_PRESENT","message":"..."}
    else 5 échecs consécutifs — RG3 (EF12, priorité Could)
        API-->>UI: 400 {"code":"TROP_DE_TENTATIVES","message":"..."} (2 min)
    else valide
        API->>DB: INSERT presences (session_id, etudiant_id, source='ETUDIANT')
        API-->>UI: 201 {"id":8,"session":3,"etudiant":17,"source":"ETUDIANT","marqueLe":"..."}
        UI->>API: GET /api/tableau?promotionId=2
        API-->>UI: 200 (moyenne=null si aucune note — contrat / RG17)
        UI-->>E: « Présence enregistrée » + tableau rafraîchi
    end

    Note over E,DB: Variante formateur — Q14 / RG13

    participant F as Formateur
    F->>API: POST /api/presences  {"code":"K7F2","etudiantId":4,"source":"FORMATEUR"}
    API-->>F: 201 {"id":9,"source":"FORMATEUR"}
```

## Table des codes — `POST /api/presences`

| Code | Déclencheur | Règle |
|---|---|---|
| `201` | Présence créée | — |
| `400` `CODE_INCONNU` | Code absent/incorrect | contrat |
| `400` `TROP_DE_TENTATIVES` | 5 échecs, 2 min de blocage | Q4 — *voir §7.2 du cahier : `400`, pas `429`, pour ne pas ajouter de code de statut à l'opération imposée* |
| `409` `DEJA_PRESENT` | Déjà présent dans la session | RG14 |
| `410` `CODE_EXPIRE` | `expiration_at` dépassée | RG1 / Q2 |
| `410` `SESSION_TERMINEE` | Session `CLOTUREE` | RG2 / Q3 + RG16 |

**Égalités à respecter entre ce tableau, `api/contrat.yaml` et le code** : un écart sur une seule
ligne = non-conformité `B2`. Le formattage d'erreur `{code, message}` est commun à **toutes** les
routes, y compris les 404 (`B4`).

## Erreurs non testées ici mais présentes

- `POST /api/sessions` → `400` promotion inconnue, `422` titre manquant.
- `POST /api/exercices` → `409` session clôturée, `410` code expiré, `400` URL invalide.
- `POST /api/relectures/{id}` → `403` `AUTO_RELECTURE` (Q5 / RG4), `409` `RELECTURE_DEJA_RENDUE` (Q15 / RG9).
