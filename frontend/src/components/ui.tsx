import { libelleStatut } from "@/lib/format";

/** État de chargement visible pendant chaque requête. */
export function Chargement({ texte = "Chargement en cours…" }: { texte?: string }) {
  return (
    <p className="message message--info" role="status">
      <span className="spinner" aria-hidden="true" />
      <span>{texte}</span>
    </p>
  );
}

/** Message d'erreur : la phrase française renvoyée par l'API. */
export function MessageErreur({ texte }: { texte: string | null }) {
  if (!texte) return null;
  return (
    <p className="message message--erreur" role="alert">
      {texte}
    </p>
  );
}

/** Message de succès après une action réussie. */
export function MessageSucces({ texte }: { texte: string | null }) {
  if (!texte) return null;
  return (
    <p className="message message--succes" role="status">
      {texte}
    </p>
  );
}

const CLASSES_STATUT: Record<string, string> = {
  OUVERTE: "badge badge--ouverte",
  CLOTUREE: "badge badge--cloturee",
  EN_ATTENTE: "badge badge--attente",
  RENDUE: "badge badge--rendue",
  PROVISOIRE: "badge badge--attente",
};

/** Pastille de statut : on affiche la valeur renvoyée par l'API. */
export function Badge({ statut }: { statut: string }) {
  return (
    <span className={CLASSES_STATUT[statut] ?? "badge"}>{libelleStatut(statut)}</span>
  );
}
