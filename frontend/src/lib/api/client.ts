import type { CorpsErreur } from "./types";

/** Backend visé par l'application (surchargeable par variable d'environnement). */
export const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

/**
 * Erreur levée par la couche d'appels API.
 * `message` est toujours une phrase française lisible destinée à l'utilisateur ;
 * `code` est l'identifiant stable renvoyé par le backend (ex. `CODE_EXPIRE`).
 * Aucune trace d'exécution n'est exposée.
 */
export class ApiError extends Error {
  readonly code: string;
  readonly status?: number;

  constructor(code: string, message: string, status?: number) {
    super(message);
    this.name = "ApiError";
    this.code = code;
    this.status = status;
  }
}

function estCorpsErreur(valeur: unknown): valeur is CorpsErreur {
  if (typeof valeur !== "object" || valeur === null) return false;
  const candidat = valeur as CorpsErreur;
  return typeof candidat.code === "string" && typeof candidat.message === "string";
}

async function lireCorps(response: Response): Promise<{ valide: boolean; valeur: unknown }> {
  const texte = await response.text().catch(() => "");
  if (!texte) return { valide: true, valeur: undefined };
  try {
    return { valide: true, valeur: JSON.parse(texte) as unknown };
  } catch {
    return { valide: false, valeur: undefined };
  }
}

/**
 * Appel HTTP unique de l'application.
 * - `response.ok === false` → lit le corps `{code, message}` et lève `ApiError` ;
 * - corps illisible → `ApiError` à code générique ;
 * - échec réseau → `ApiError` à code générique.
 * L'utilisateur ne voit jamais de pile d'appels : uniquement `ApiError.message`.
 */
export async function appelerApi<T>(chemin: string, options: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${chemin}`, {
      ...options,
      headers: {
        "Content-Type": "application/json",
        ...(options.headers ?? {}),
      },
    });
  } catch {
    throw new ApiError(
      "API_INACCESSIBLE",
      "Le serveur est injoignable. Vérifiez que l'API est démarrée, puis réessayez.",
    );
  }

  const corps = await lireCorps(response);

  if (!response.ok) {
    if (estCorpsErreur(corps.valeur)) {
      throw new ApiError(corps.valeur.code, corps.valeur.message, response.status);
    }
    if (!corps.valide) {
      throw new ApiError(
        "REPONSE_INVALIDE",
        "Le serveur a renvoyé une réponse illisible. Vérifiez que l'API est bien démarrée.",
        response.status,
      );
    }
    throw new ApiError(
      "ERREUR_SERVEUR",
      `Le serveur a répondu une erreur (${response.status}). Veuillez réessayer.`,
      response.status,
    );
  }

  if (!corps.valide) {
    throw new ApiError(
      "REPONSE_INVALIDE",
      "Le serveur a renvoyé une réponse illisible. Vérifiez que l'API est bien démarrée.",
      response.status,
    );
  }

  return corps.valeur as T;
}

/** Construit le corps d'une requête JSON. */
export function corpsJson(valeur: unknown): RequestInit {
  return {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(valeur),
  };
}

/** Construit une chaîne de requête à partir d'un paramètre optionnel. */
export function parametres(query: Record<string, string | number | undefined>): string {
  const search = new URLSearchParams();
  for (const [cle, valeur] of Object.entries(query)) {
    if (valeur !== undefined && valeur !== "") search.set(cle, String(valeur));
  }
  const chaine = search.toString();
  return chaine ? `?${chaine}` : "";
}
