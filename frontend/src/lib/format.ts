/** Formatage à l'affichage uniquement : aucune règle métier n'est appliquée ici. */

/** Valeur affichée quand l'API ne renvoie rien (jamais de tiret). */
export const VALEUR_VIDE = "n/a";

/** Date ISO renvoyée par l'API → texte lisible (fr-FR). */
export function formaterDate(valeur: string | null | undefined): string {
  if (!valeur) return VALEUR_VIDE;
  const date = new Date(valeur);
  if (Number.isNaN(date.getTime())) return VALEUR_VIDE;
  return date.toLocaleString("fr-FR", { dateStyle: "medium", timeStyle: "short" });
}

/** `moyenne` est fournie par l'API ; `null` s'affiche en valeur vide (aucun recalcul). */
export function formaterMoyenne(moyenne: number | null | undefined): string {
  if (moyenne === null || moyenne === undefined) return VALEUR_VIDE;
  return String(moyenne);
}

/** Libellés français des statuts renvoyés par l'API. */
export function libelleStatut(statut: string): string {
  switch (statut) {
    case "OUVERTE":
      return "Ouverte";
    case "CLOTUREE":
      return "Clôturée";
    case "EN_ATTENTE":
      return "En attente";
    case "RENDUE":
      return "Rendue";
    case "PROVISOIRE":
      return "Provisoire : un seul des deux relecteurs a rendu sa note";
    default:
      return statut;
  }
}
