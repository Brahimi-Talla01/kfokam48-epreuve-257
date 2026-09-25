import { appelerApi, corpsJson, parametres } from "./client";
import type {
  ExerciceDepose,
  ExerciceMisAJour,
  Etudiant,
  LigneTableau,
  NotesExercice,
  Presence,
  Promotion,
  Relecture,
  RelectureCreee,
  Session,
  SessionCloturee,
  SessionOuverte,
  StatutRelecture,
} from "./types";

/**
 * Les 14 opérations de `api/contrat.yaml` utilisées par les 3 écrans.
 * Toutes passent par `appelerApi` : aucun `fetch` direct dans `src/app/**`.
 */

// --- Sessions -------------------------------------------------------------

/** POST /api/sessions : ouverture d'une session et obtention du code. */
export function creerSession(donnees: { titre: string; promotionId: number }): Promise<SessionOuverte> {
  return appelerApi<SessionOuverte>("/api/sessions", corpsJson(donnees));
}

/** GET /api/sessions?promotionId= : sessions d'une promotion. */
export function listerSessions(promotionId: number): Promise<Session[]> {
  return appelerApi<Session[]>(`/api/sessions${parametres({ promotionId })}`);
}

/** GET /api/sessions/{id} : détail d'une session. */
export function obtenirSession(id: number): Promise<Session> {
  return appelerApi<Session>(`/api/sessions/${id}`);
}

/** POST /api/sessions/{id}/cloturer : clôture de la session. */
export function cloturerSession(id: number): Promise<SessionCloturee> {
  return appelerApi<SessionCloturee>(`/api/sessions/${id}/cloturer`, corpsJson({}));
}

// --- Présences ------------------------------------------------------------

/** POST /api/presences : `source` vaut `ETUDIANT` par défaut côté backend. */
export function marquerPresence(
  donnees: { code: string; etudiantId: number; source?: "ETUDIANT" | "FORMATEUR" },
): Promise<Presence> {
  return appelerApi<Presence>("/api/presences", corpsJson(donnees));
}

// --- Exercices ------------------------------------------------------------

/** POST /api/exercices : dépôt du lien de l'exercice. */
export function deposerExercice(
  donnees: { sessionId: number; etudiantId: number; lien: string },
): Promise<ExerciceDepose> {
  return appelerApi<ExerciceDepose>("/api/exercices", corpsJson(donnees));
}

/** PUT /api/exercices/{id}/lien : remplacement du lien (tant qu'il n'est pas relu). */
export function remplacerLienExercice(
  id: number,
  donnees: { lien: string },
): Promise<ExerciceMisAJour> {
  return appelerApi<ExerciceMisAJour>(`/api/exercices/${id}/lien`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(donnees),
  });
}

/**
 * GET /api/exercices/{id}/relectures : note retenue + détail des relectures reçues
 * (sans relecteur). Étape 3 (RG18/RG19) : jusqu'à deux relectures, `noteRetenue` et
 * `provisoire` calculés côté API : jamais recalculés ici (F3 / ENF6).
 */
export function obtenirNotesExercice(id: number): Promise<NotesExercice> {
  return appelerApi<NotesExercice>(`/api/exercices/${id}/relectures`);
}

// --- Relectures -----------------------------------------------------------

/** POST /api/relectures : création / affectation des relectures (RG18 : jusqu'à deux). */
export function creerRelecture(donnees: { exerciceId: number }): Promise<RelectureCreee[]> {
  return appelerApi<RelectureCreee[]>("/api/relectures", corpsJson(donnees));
}

/** GET /api/relectures?relecteurId=&statut= : mes relectures. */
export function listerRelectures(
  relecteurId: number,
  statut?: StatutRelecture,
): Promise<Relecture[]> {
  return appelerApi<Relecture[]>(
    `/api/relectures${parametres({ relecteurId, statut })}`,
  );
}

/**
 * POST /api/relectures/{id} : rendu d'une note entière 0–20 et d'un commentaire.
 * `relecteurId` est facultatif : le contrat n'exige que `note` et `commentaire`.
 */
export function rendreRelecture(
  id: number,
  donnees: { note: number; commentaire: string; relecteurId?: number },
): Promise<void> {
  return appelerApi<void>(`/api/relectures/${id}`, corpsJson(donnees));
}

// --- Tableau --------------------------------------------------------------

/** GET /api/tableau?promotionId= : récapitulatif du formateur. */
export function obtenirTableau(promotionId: number): Promise<LigneTableau[]> {
  return appelerApi<LigneTableau[]>(`/api/tableau${parametres({ promotionId })}`);
}

// --- Promotions -----------------------------------------------------------

/** GET /api/promotions : liste des promotions. */
export function listerPromotions(): Promise<Promotion[]> {
  return appelerApi<Promotion[]>("/api/promotions");
}

/** GET /api/promotions/{id}/etudiants : étudiants d'une promotion (choix sans mot de passe). */
export function listerEtudiants(id: number): Promise<Etudiant[]> {
  return appelerApi<Etudiant[]>(`/api/promotions/${id}/etudiants`);
}
