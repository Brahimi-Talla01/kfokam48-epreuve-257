/**
 * Types des réponses de l'API, déduits de `api/contrat.yaml`.
 * Rien n'est calculé côté client : on affiche ce que l'API renvoie.
 */

/** Corps d'erreur imposé pour TOUTES les erreurs. */
export interface CorpsErreur {
  code: string;
  message: string;
}

export type StatutSession = "OUVERTE" | "CLOTUREE";
export type StatutRelecture = "EN_ATTENTE" | "RENDUE";
export type SourcePresence = "ETUDIANT" | "FORMATEUR";

/** POST /api/sessions → 201 */
export interface SessionOuverte {
  id: number;
  code: string;
  ouvertureAt: string;
  expirationAt: string;
}

/** Session (GET /api/sessions, GET /api/sessions/{id}). */
export interface Session {
  id: number;
  titre: string;
  promotionId: number;
  code: string;
  statut: StatutSession;
  ouvertureAt: string;
  expirationAt: string;
  clotureAt: string | null;
}

/** POST /api/sessions/{id}/cloturer → 200 */
export interface SessionCloturee {
  id: number;
  statut: StatutSession;
  clotureAt: string;
}

export interface Promotion {
  id: number;
  nom: string;
}

export interface Etudiant {
  id: number;
  nom: string;
}

/** POST /api/presences → 201 */
export interface Presence {
  id: number;
  sessionId: number;
  etudiantId: number;
  source: SourcePresence;
}

/** POST /api/exercices → 201 */
export interface ExerciceDepose {
  id: number;
  statut: string;
}

/** PUT /api/exercices/{id}/lien → 200 */
export interface ExerciceMisAJour {
  id: number;
  statut: string;
  misAJourLe: string;
}

/** POST /api/relectures → 201 */
export interface RelectureCreee {
  id: number;
  exerciceId: number;
  relecteurId: number | null;
  statut: StatutRelecture;
}

/** Relecture affectée (GET /api/relectures?relecteurId=). */
export interface Relecture {
  id: number;
  exerciceId: number;
  relecteurId: number;
  statut: StatutRelecture;
  lien: string;
  note?: number | null;
  commentaire?: string | null;
}

/** Note et commentaire reçus sur mon exercice — jamais le nom du relecteur. */
export interface RelectureRecue {
  id: number;
  statut: StatutRelecture;
  note: number | null;
  commentaire: string | null;
  rendueLe?: string | null;
}

/** Ligne du tableau récapitulatif (GET /api/tableau). */
export interface LigneTableau {
  etudiantId: number;
  nom: string;
  presences: number;
  exercicesDeposes: number;
  /** `null` tant qu'aucune note n'existe — jamais recalculée ici. */
  moyenne: number | null;
  relecturesEnAttente: number;
}
