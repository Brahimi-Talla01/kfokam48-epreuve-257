# Soumission — Épreuve finale fullstack KFOKAM48

> Remplis ce fichier, **vérifie tes deux liens depuis une fenêtre de navigation privée**,
> puis téléverse-le sur la plateforme **avant 18h00**.
> Sans ce dépôt sur la plateforme, tu n'as rien rendu.

---

## Candidat

|                  |                                    |
| ---------------- | ---------------------------------- |
| Nom et prénom(s) | Talla Moussa Ibrahim               |
| Matricule        | 257                                |
| Centre           | Yaoundé                            |
| Compte GitHub    | https://github.com/Brahimi-Talla01 |

## Projet

|                                            |                                                           |
| ------------------------------------------ | --------------------------------------------------------- |
| Dépôt (public)                             | `https://github.com/Brahimi-Talla01/kfokam48-epreuve-257` |
| Commit final — hash complet, 40 caractères | `5bacaad0cce3c64ca99229c907499ec9b6c98517`                |
| Branche                                    | `main`                                                    |

## Technique

|                        |                                                                                                                                            |
| ---------------------- | ------------------------------------------------------------------------------------------------------------------------------------------ |
| Frontend utilisé       | Next.js 15 (App Router, TypeScript) — 3 écrans en routes natives, build de production en `next start`, sans rendu serveur applicatif exigé |
| Base de données        | PostgreSQL 16 (Flyway : `V1` schéma, `V2` seed de démo, `V3` évolution étape 3)                                                            |
| Commandes de démarrage | `git clone <dépôt>` puis `docker compose up --build` (1 commande)                                                                          |

## Ce que j'ai livré

Les 5 besoins sont couverts de bout en bout (présence par code, dépôt d'exercice,
affectation aléatoire, relecture notée, tableau formateur), avec l'évolution de
l'étape 3 : deux relecteurs par exercice, note = moyenne, note provisoire tant qu'un
seul a rendu. Le bug de présence concurrente (étape 3) est corrigé et couvert par un
test dédié. 46 tests backend passent, `npm run build` passe, `docker compose up
--build` démarre l'application avec des données de démonstration depuis un clone
vierge. Volontairement laissé de côté : le blocage de 2 minutes après 5 codes
erronés (RG3, priorité `could` dès l'étape 1) — sacrifié pour absorber le
changement de besoin tardif de l'étape 3 (détail : `docs/CAHIER_DES_CHARGES.md` §7.3
et `docs/JOURNAL.md`, entrée étape 3).

---

## Avant de téléverser, vérifie

- [x] Mon dépôt est **public** et s'ouvre en navigation privée
- [x] Le hash fait bien **40 caractères** et existe sur GitHub
- [x] Tout mon travail est **poussé** — `git status` est propre
- [x] Mon `README` a été testé depuis un clone vierge, dans un dossier vide
- [x] Mon `JOURNAL.md` et mon cahier des charges sont dans `docs/`
- [x] Les trois commits `[JALON]` sont poussés et dans le bon ordre

---

**Déclaration.** J'ai réalisé ce travail seul. Les outils d'IA étaient autorisés sans restriction et je les ai utilisés ; mon journal indique où et comment j'ai vérifié leurs réponses. Mes dépôts resteront publics et inchangés jusqu'à la publication des résultats.

Signature : _ib_ Date : 25/09/2026
