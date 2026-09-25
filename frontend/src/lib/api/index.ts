/**
 * Couche d'appels API : point d'entrée unique.
 * Tout le reste de l'application importe depuis `@/lib/api`.
 */
export { API_BASE_URL, ApiError, appelerApi, corpsJson, parametres } from "./client";
export * from "./types";
export {
  cloturerSession,
  creerRelecture,
  creerSession,
  deposerExercice,
  listerEtudiants,
  listerPromotions,
  listerRelectures,
  listerSessions,
  marquerPresence,
  obtenirNotesExercice,
  obtenirSession,
  obtenirTableau,
  rendreRelecture,
  remplacerLienExercice,
} from "./endpoints";
