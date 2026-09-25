"use client";

import { useEffect, useState, type FormEvent } from "react";
import {
  cloturerSession,
  creerSession,
  listerEtudiants,
  listerPromotions,
  listerSessions,
  marquerPresence,
  obtenirTableau,
} from "@/lib/api";
import type { Etudiant, LigneTableau, Promotion, Session, SessionOuverte } from "@/lib/api";
import { Badge, Chargement, MessageErreur, MessageSucces } from "@/components/ui";
import { useAction, useChargement } from "@/lib/feedback";
import { formaterDate, formaterMoyenne } from "@/lib/format";

export default function EcranFormateur() {
  const promotions = useChargement(() => listerPromotions(), []);

  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [titre, setTitre] = useState("");
  const [sessionCreee, setSessionCreee] = useState<SessionOuverte | null>(null);

  const etudiants = useChargement<Etudiant[]>(
    async () => (promotionId === null ? [] : listerEtudiants(promotionId)),
    [promotionId],
  );
  const sessions = useChargement<Session[]>(
    async () => (promotionId === null ? [] : listerSessions(promotionId)),
    [promotionId],
  );
  const tableau = useChargement<LigneTableau[]>(
    async () => (promotionId === null ? [] : obtenirTableau(promotionId)),
    [promotionId],
  );

  const [etudiantChoisi, setEtudiantChoisi] = useState<number | null>(null);
  const [code, setCode] = useState("");
  const [codePersonnalise, setCodePersonnalise] = useState(false);

  // Le code proposé est celui de la session ouverte de la promotion choisie.
  useEffect(() => {
    if (codePersonnalise) return;
    const ouverte = sessions.donnees?.find((s) => s.statut === "OUVERTE");
    setCode(ouverte ? ouverte.code : "");
  }, [sessions.donnees, codePersonnalise]);

  const creation = useAction();
  const cloture = useAction();
  const presence = useAction();

  function changerPromotion(valeur: string) {
    setPromotionId(valeur === "" ? null : Number(valeur));
    setSessionCreee(null);
    setEtudiantChoisi(null);
    setCodePersonnalise(false);
    setCode("");
    creation.reinitialiser();
    cloture.reinitialiser();
    presence.reinitialiser();
  }

  async function creerSessionAction(event: FormEvent) {
    event.preventDefault();
    if (promotionId === null) return;
    const ok = await creation.executer(async () => {
      const session = await creerSession({ titre: titre.trim(), promotionId });
      setSessionCreee(session);
    }, "Session ouverte : le code de présence est affiché ci-dessous.");
    if (ok) {
      setTitre("");
      setCodePersonnalise(false);
      sessions.recharger();
    }
  }

  async function cloturerSessionAction(sessionId: number) {
    const ok = await cloture.executer(
      () => cloturerSession(sessionId),
      "Session clôturée.",
    );
    if (ok) {
      sessions.recharger();
      tableau.recharger();
    }
  }

  async function presenceManuelleAction(event: FormEvent) {
    event.preventDefault();
    if (etudiantChoisi === null || code.trim() === "") return;
    const ok = await presence.executer(
      () =>
        marquerPresence({
          code: code.trim(),
          etudiantId: etudiantChoisi,
          source: "FORMATEUR",
        }),
      "Présence ajoutée par le formateur.",
    );
    if (ok) tableau.recharger();
  }

  const promotionsDonnees: Promotion[] = promotions.donnees ?? [];

  return (
    <>
      <h1>Écran formateur</h1>

      <section className="card">
        <h2>Promotion</h2>
        {promotions.enCours && <Chargement texte="Chargement des promotions…" />}
        <MessageErreur texte={promotions.erreur} />
        {!promotions.enCours && !promotions.erreur && promotionsDonnees.length === 0 && (
          <p className="hint">Aucune promotion disponible.</p>
        )}
        {promotionsDonnees.length > 0 && (
          <div className="champ">
            <label htmlFor="promotion">Choisir une promotion</label>
            <select
              id="promotion"
              className="select"
              value={promotionId === null ? "" : String(promotionId)}
              onChange={(event) => changerPromotion(event.target.value)}
            >
              <option value="">— Choisir —</option>
              {promotionsDonnees.map((promotion) => (
                <option key={promotion.id} value={promotion.id}>
                  {promotion.nom}
                </option>
              ))}
            </select>
          </div>
        )}
      </section>

      {promotionId !== null && (
        <>
          <section className="card">
            <h2>Ouvrir une session</h2>
            <form onSubmit={creerSessionAction}>
              <div className="champ">
                <label htmlFor="titre-session">Titre de la session</label>
                <input
                  id="titre-session"
                  className="input"
                  type="text"
                  value={titre}
                  required
                  placeholder="Ex. Séance du 25 septembre"
                  onChange={(event) => setTitre(event.target.value)}
                />
              </div>
              <div className="boutons">
                <button className="bouton" type="submit" disabled={creation.enCours}>
                  {creation.enCours ? "Ouverture en cours…" : "Ouvrir la session"}
                </button>
              </div>
              {creation.enCours && <Chargement texte="Ouverture de la session…" />}
              <MessageErreur texte={creation.erreur} />
              <MessageSucces texte={creation.succes} />
            </form>

            {sessionCreee && (
              <div className="code-carte">
                <p className="code-libelle">Code de présence</p>
                <p className="code-valeur">{sessionCreee.code}</p>
                <p className="meta">
                  Session n° {sessionCreee.id} — ouverte le{" "}
                  {formaterDate(sessionCreee.ouvertureAt)}.
                </p>
                <p className="meta">
                  Le code expire le {formaterDate(sessionCreee.expirationAt)}.
                </p>
              </div>
            )}
          </section>

          <section className="card">
            <h2>Sessions de la promotion</h2>
            {sessions.enCours && <Chargement texte="Chargement des sessions…" />}
            <MessageErreur texte={sessions.erreur} />
            <MessageSucces texte={cloture.succes} />
            {!sessions.enCours &&
              !sessions.erreur &&
              (sessions.donnees ?? []).length === 0 && (
                <p className="hint">Aucune session pour cette promotion.</p>
              )}
            {(sessions.donnees ?? []).length > 0 && (
              <div className="table-wrap">
                <table className="tableau">
                  <thead>
                    <tr>
                      <th>Titre</th>
                      <th>Code</th>
                      <th>Statut</th>
                      <th>Ouverture</th>
                      <th>Expiration du code</th>
                      <th>Clôture</th>
                      <th>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(sessions.donnees ?? []).map((session) => (
                      <tr key={session.id}>
                        <td>{session.titre}</td>
                        <td>
                          <code>{session.code}</code>
                        </td>
                        <td>
                          <Badge statut={session.statut} />
                        </td>
                        <td>{formaterDate(session.ouvertureAt)}</td>
                        <td>{formaterDate(session.expirationAt)}</td>
                        <td>{formaterDate(session.clotureAt)}</td>
                        <td>
                          {session.statut === "OUVERTE" ? (
                            <button
                              type="button"
                              className="bouton bouton--danger"
                              disabled={cloture.enCours}
                              onClick={() => cloturerSessionAction(session.id)}
                            >
                              Clôturer
                            </button>
                          ) : (
                            <span className="hint">—</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            {cloture.enCours && <Chargement texte="Clôture en cours…" />}
            <MessageErreur texte={cloture.erreur} />
          </section>

          <section className="card">
            <h2>Ajouter une présence manuellement</h2>
            <form onSubmit={presenceManuelleAction}>
              <div className="champ">
                <label htmlFor="etudiant-presence">Étudiant</label>
                <select
                  id="etudiant-presence"
                  className="select"
                  value={etudiantChoisi === null ? "" : String(etudiantChoisi)}
                  required
                  onChange={(event) =>
                    setEtudiantChoisi(
                      event.target.value === "" ? null : Number(event.target.value),
                    )
                  }
                >
                  <option value="">— Choisir un étudiant —</option>
                  {(etudiants.donnees ?? []).map((etudiant) => (
                    <option key={etudiant.id} value={etudiant.id}>
                      {etudiant.nom}
                    </option>
                  ))}
                </select>
              </div>
              <div className="champ">
                <label htmlFor="code-presence">Code de la session en cours</label>
                <input
                  id="code-presence"
                  className="input input--code"
                  type="text"
                  value={code}
                  required
                  autoComplete="off"
                  placeholder="CODE"
                  onChange={(event) => {
                    setCodePersonnalise(true);
                    setCode(event.target.value);
                  }}
                />
                <span className="hint">
                  Pré-rempli avec le code de la session ouverte de cette promotion.
                </span>
              </div>
              <div className="boutons">
                <button className="bouton" type="submit" disabled={presence.enCours}>
                  {presence.enCours ? "Enregistrement…" : "Ajouter la présence"}
                </button>
              </div>
              {presence.enCours && <Chargement texte="Enregistrement de la présence…" />}
              <MessageErreur texte={presence.erreur} />
              <MessageSucces texte={presence.succes} />
            </form>
          </section>

          <section className="card">
            <h2>Tableau de la promotion</h2>
            {tableau.enCours && <Chargement texte="Chargement du tableau…" />}
            <MessageErreur texte={tableau.erreur} />
            {!tableau.enCours &&
              !tableau.erreur &&
              (tableau.donnees ?? []).length === 0 && (
                <p className="hint">Aucun étudiant dans ce tableau.</p>
              )}
            {(tableau.donnees ?? []).length > 0 && (
              <div className="table-wrap">
                <table className="tableau">
                  <thead>
                    <tr>
                      <th>Nom</th>
                      <th className="num">Présences</th>
                      <th className="num">Exercices déposés</th>
                      <th className="num">Moyenne</th>
                      <th className="num">Relectures en attente</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(tableau.donnees ?? []).map((ligne) => (
                      <tr key={ligne.etudiantId}>
                        <td>{ligne.nom}</td>
                        <td className="num">{ligne.presences}</td>
                        <td className="num">{ligne.exercicesDeposes}</td>
                        <td className="num">{formaterMoyenne(ligne.moyenne)}</td>
                        <td className="num">{ligne.relecturesEnAttente}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
            <p className="hint">
              Les valeurs (dont la moyenne) sont celles renvoyées par l&apos;API.
            </p>
          </section>
        </>
      )}
    </>
  );
}
