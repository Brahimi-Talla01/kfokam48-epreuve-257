# Frontend — KFOKAM48, présence et relecture par les pairs

Trois écrans (Next.js 15, App Router, TypeScript) :

| Route | Écran |
| --- | --- |
| `/formateur` | ouverture de session, code de présence, clôture, présence manuelle, tableau |
| `/etudiant` | choix du nom, code de présence, dépôt / remplacement du lien, notes reçues |
| `/relecteur` | relectures affectées, rendu note 0–20 + commentaire |

Aucune librairie tierce : TypeScript, React et Next.js uniquement, style maison dans
`src/app/globals.css`.

## Développement

```bash
npm install
npm run dev
```

L'application s'ouvre sur <http://localhost:3000>.

## Build de production

```bash
npm run build
npm start
```

## Variable d'environnement

| Variable | Rôle | Défaut |
| --- | --- | --- |
| `NEXT_PUBLIC_API_URL` | URL de base du backend Spring Boot appelé par l'application | `http://localhost:8080` |

Exemple : `NEXT_PUBLIC_API_URL=https://api.exemple.fr npm run dev`.

## Couche d'appels API

Toutes les requêtes HTTP passent par `src/lib/api/` (aucun `fetch` direct dans `src/app/**`) :

- `src/lib/api/client.ts` — base URL, classe `ApiError` (`code`, `message`, `status`) et
  fonction unique `appelerApi` qui lit le corps `{code, message}` en cas d'échec ;
- `src/lib/api/endpoints.ts` — les 14 opérations typées du contrat `api/contrat.yaml` ;
- `src/lib/api/types.ts` — types des réponses.

Aucune règle métier n'est dupliquée côté client : le tableau, l'expiration des codes et les
statuts sont affichés tels que l'API les renvoie.
