"use client";

import { useCallback, useEffect, useState } from "react";
import { ApiError } from "@/lib/api";

/**
 * Message destiné à l'utilisateur.
 * - `ApiError` → la phrase française renvoyée par l'API (`error.message`) ;
 * - tout autre cas → message générique : jamais de trace d'appel, jamais de code brut seul.
 */
export function messageErreur(erreur: unknown): string {
  if (erreur instanceof ApiError) return erreur.message;
  return "Une erreur inattendue est survenue. Veuillez réessayer.";
}

/** État d'une action (requête écrite) : chargement, erreur, succès. */
export function useAction() {
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [succes, setSucces] = useState<string | null>(null);

  const executer = useCallback(async (action: () => Promise<unknown>, messageSucces: string) => {
    setEnCours(true);
    setErreur(null);
    setSucces(null);
    try {
      await action();
      setSucces(messageSucces);
      return true;
    } catch (e) {
      setErreur(messageErreur(e));
      return false;
    } finally {
      setEnCours(false);
    }
  }, []);

  const reinitialiser = useCallback(() => {
    setErreur(null);
    setSucces(null);
  }, []);

  return { enCours, erreur, succes, executer, reinitialiser };
}

/** État d'un chargement (requête en lecture), rejouable à la demande. */
export function useChargement<T>(charger: () => Promise<T>, dependances: readonly unknown[]) {
  const [donnees, setDonnees] = useState<T | null>(null);
  const [enCours, setEnCours] = useState(true);
  const [erreur, setErreur] = useState<string | null>(null);
  const [tick, setTick] = useState(0);

  useEffect(() => {
    let actif = true;
    setEnCours(true);
    setErreur(null);
    charger()
      .then((valeur) => {
        if (!actif) return;
        setDonnees(valeur);
        setEnCours(false);
      })
      .catch((e: unknown) => {
        if (!actif) return;
        setErreur(messageErreur(e));
        setDonnees(null);
        setEnCours(false);
      });
    return () => {
      actif = false;
    };
    // Le rechargement manuel passe par `recharger` ci-dessous.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [...dependances, tick]);

  const recharger = useCallback(() => setTick((v) => v + 1), []);

  return { donnees, enCours, erreur, recharger };
}
