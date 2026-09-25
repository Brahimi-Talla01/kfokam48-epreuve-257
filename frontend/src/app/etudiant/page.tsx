"use client";

import { useState, type FormEvent } from "react";
import {
  deposerExercice,
  listerEtudiants,
  listerPromotions,
  listerRelecturesExercice,
  listerSessions,
  marquerPresence,
  remplacerLienExercice,
} from "@/lib/api";
import type { Etudiant, RelectureRecue, Session } from "@/lib/api";
import { Badge, Chargement, MessageErreur, MessageSucces } from "@/components/ui";
import { useAction, useChargement } from "@/lib/feedback";
import { formaterDate } from "@/lib/format";

export default function EcranEtudiant() {
  const promotions = useChargement(() => listerPromotions(), []);

  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [etudiantChoisi, setEtudiantChoisi] = useState<number | null>(null);

  const etudiants = useChargement<Etudiant[]>(
    async () => (promotionId === null ? [] : listerEtudiants(promotionId)),
    [promotionId],
  );
  const sessions = useChargement<Session[]>(
    async () => (promotionId === null ? [] : listerSessions(promotionId)),
    [promotionId],
  );

  const [code, setCode] = useState("");
  const [sessionId, setSessionId] = useState<number | null>(null);
  const [lien, setLien] = useState("");
  const [exerciceId, setExerciceId] = useState<number | null>(null);

  const notes = useChargement<RelectureRecue[]>(
    async () => (exerciceId === null ? [] : listerRelecturesExercice(exerciceId)),
    [],
  );
  // Les notes ne sont demandées qu'à l'explicitation de l'étudiant (bouton
  // « Consulter mes notes ») ou après un dépôt : taper un identifiant chiffre
  // par chiffre ne déclenche donc aucune requête.
  const [notesConsultees, setNotesConsultees] = useState(false);

  function consulterNotes() {
    setNotesConsultees(true);
    notes.recharger();
  }

  const presence = useAction();
  const depot = useAction();
  const remplacement = useAction();

  function changerPromotion(valeur: string) {
    setPromotionId(valeur === "" ? null : Number(valeur));
    setEtudiantChoisi(null);
    setSessionId(null);
    setExerciceId(null);
    presence.reinitialiser();
    depot.reinitialiser();
    remplacement.reinitialiser();
    setNotesConsultees(false);
    notes.recharger();
  }

  async function marquerMaPresence(event: FormEvent) {
    event.preventDefault();
    if (etudiantChoisi === null || code.trim() === "") return;
    const ok = await presence.executer(async () => {
      const resultat = await marquerPresence({
        code: code.trim(),
        etudiantId: etudiantChoisi,
      });
      setSessionId(resultat.sessionId);
    }, "Votre présence est enregistrée.");
    if (ok) {
      setCode("");
      sessions.recharger();
    }
  }

  async function deposerMonExercice(event: FormEvent) {
    event.preventDefault();
    if (etudiantChoisi === null || sessionId === null) return;
    const ok = await depot.executer(async () => {
      const resultat = await deposerExercice({
        sessionId,
        etudiantId: etudiantChoisi,
        lien: lien.trim(),
      });
      setExerciceId(resultat.id);
    }, "Exercice déposé. Notez l'identifiant affiché : il sert à remplacer le lien et à lire vos notes.");
    if (ok) {
      consulterNotes();
    }
  }

  async function remplacerMonLien(event: FormEvent) {
    event.preventDefault();
    if (exerciceId === null) return;
    const ok = await remplacement.executer(
      () => remplacerLienExercice(exerciceId, { lien: lien.trim() }),
      "Lien remplacé.",
    );
    if (ok) consulterNotes();
  }

  const promotionChoisie = promotionId !== null;
  const identiteChoisie = etudiantChoisi !== null;

  return (
    <>
      <h1>Écran étudiant</h1>

      <section className="card">
        <h2>Qui êtes-vous ?</h2>
        <p className="hint">Aucun mot de passe : choisissez simplement votre nom.</p>
        {promotions.enCours && <Chargement texte="Chargement des promotions…" />}
        <MessageErreur texte={promotions.erreur} />
        {(promotions.donnees ?? []).length > 0 && (
          <div className="champ">
            <label htmlFor="promotion">Promotion</label>
            <select
              id="promotion"
              className="select"
              value={promotionId === null ? "" : String(promotionId)}
              onChange={(event) => changerPromotion(event.target.value)}
            >
              <option value="">— Choisir —</option>
              {(promotions.donnees ?? []).map((promotion) => (
                <option key={promotion.id} value={promotion.id}>
                  {promotion.nom}
                </option>
              ))}
            </select>
          </div>
        )}
        {promotionChoisie && (
          <div className="champ">
            <label htmlFor="etudiant">Mon nom</label>
            <select
              id="etudiant"
              className="select"
              value={etudiantChoisi === null ? "" : String(etudiantChoisi)}
              onChange={(event) =>
                setEtudiantChoisi(event.target.value === "" ? null : Number(event.target.value))
              }
            >
              <option value="">— Choisir mon nom —</option>
              {(etudiants.donnees ?? []).map((etudiant) => (
                <option key={etudiant.id} value={etudiant.id}>
                  {etudiant.nom}
                </option>
              ))}
            </select>
          </div>
        )}
        {promotionChoisie && etudiants.enCours && (
          <Chargement texte="Chargement des étudiants…" />
        )}
        <MessageErreur texte={etudiants.erreur} />
      </section>

      <section className="card">
        <h2>Marquer ma présence</h2>
        <form onSubmit={marquerMaPresence}>
          <div className="champ">
            <label htmlFor="code">Code de présence</label>
            <input
              id="code"
              className="input input--code"
              type="text"
              inputMode="text"
              autoComplete="off"
              autoCapitalize="characters"
              spellCheck={false}
              placeholder="CODE"
              value={code}
              required
              disabled={!identiteChoisie}
              onChange={(event) => setCode(event.target.value)}
            />
            <span className="hint">
              Demandez le code au formateur. Il expire à la date indiquée par l&apos;API.
            </span>
          </div>
          <div className="boutons">
            <button
              className="bouton"
              type="submit"
              disabled={!identiteChoisie || presence.enCours}
            >
              {presence.enCours ? "Envoi en cours…" : "Marquer ma présence"}
            </button>
          </div>
          {presence.enCours && <Chargement texte="Enregistrement de votre présence…" />}
          <MessageErreur texte={presence.erreur} />
          <MessageSucces texte={presence.succes} />
        </form>
        {!identiteChoisie && (
          <p className="hint">Choisissez d&apos;abord votre nom ci-dessus.</p>
        )}
      </section>

      <section className="card">
        <h2>Mon exercice</h2>
        {!identiteChoisie && (
          <p className="hint">Choisissez votre nom pour déposer un exercice.</p>
        )}
        {identiteChoisie && (
          <>
            <div className="champ">
              <label htmlFor="session">Session de dépôt</label>
              <select
                id="session"
                className="select"
                value={sessionId === null ? "" : String(sessionId)}
                onChange={(event) =>
                  setSessionId(event.target.value === "" ? null : Number(event.target.value))
                }
              >
                <option value="">— Choisir une session —</option>
                {(sessions.donnees ?? []).map((session) => (
                  <option key={session.id} value={session.id}>
                    {session.titre} — {session.statut === "OUVERTE" ? "ouverte" : "clôturée"}
                  </option>
                ))}
              </select>
              <span className="hint">
                Remplie automatiquement quand vous marquez votre présence.
              </span>
            </div>
            {sessions.enCours && <Chargement texte="Chargement des sessions…" />}
            <MessageErreur texte={sessions.erreur} />

            <div className="champ">
              <label htmlFor="lien">Lien de mon exercice (URL)</label>
              <input
                id="lien"
                className="input"
                type="url"
                required
                placeholder="https://…"
                value={lien}
                onChange={(event) => setLien(event.target.value)}
              />
            </div>

            <div className="boutons">
              <button
                className="bouton"
                type="button"
                disabled={sessionId === null || depot.enCours}
                onClick={deposerMonExercice}
              >
                Déposer mon exercice
              </button>
              <button
                className="bouton bouton--discret"
                type="button"
                disabled={exerciceId === null || remplacement.enCours}
                onClick={remplacerMonLien}
              >
                Remplacer le lien
              </button>
            </div>

            {depot.enCours && <Chargement texte="Dépôt de l'exercice…" />}
            <MessageErreur texte={depot.erreur} />
            <MessageSucces texte={depot.succes} />

            {remplacement.enCours && <Chargement texte="Remplacement du lien…" />}
            <MessageErreur texte={remplacement.erreur} />
            <MessageSucces texte={remplacement.succes} />

            <div className="champ champ--apres">
              <label htmlFor="exercice-id">Identifiant de mon exercice</label>
              <input
                id="exercice-id"
                className="input"
                type="number"
                min={1}
                placeholder="Retourné au dépôt"
                value={exerciceId === null ? "" : String(exerciceId)}
                onChange={(event) =>
                  setExerciceId(event.target.value === "" ? null : Number(event.target.value))
                }
              />
              <span className="hint">
                Nécessaire pour remplacer le lien et pour lire vos notes après un
                rechargement de la page.
              </span>
            </div>
          </>
        )}
      </section>

      <section className="card">
        <h2>Mes notes et commentaires</h2>
        {exerciceId === null && (
          <p className="hint">
            Renseignez l&apos;identifiant de votre exercice (champ ci-dessus) pour consulter
            les notes reçues.
          </p>
        )}
        <div className="boutons">
          <button
            className="bouton"
            type="button"
            disabled={exerciceId === null || notes.enCours}
            onClick={consulterNotes}
          >
            Consulter mes notes
          </button>
        </div>
        {notes.enCours && <Chargement texte="Chargement de vos notes…" />}
        <MessageErreur texte={notes.erreur} />
        {notesConsultees &&
          !notes.enCours &&
          !notes.erreur &&
          (notes.donnees ?? []).length === 0 && (
            <p className="hint">Aucune relecture sur cet exercice pour le moment.</p>
          )}
        {(notes.donnees ?? []).map((relecture) => (
          <article key={relecture.id} className="item-liste espace-bas">
            <div className="item-liste__entete">
              <h3>Relecture n° {relecture.id}</h3>
              <Badge statut={relecture.statut} />
            </div>
            <p className="meta">
              Note :{" "}
              {relecture.note === null || relecture.note === undefined ? "—" : relecture.note}
              {relecture.rendueLe ? ` — reçue le ${formaterDate(relecture.rendueLe)}` : ""}
            </p>
            <p>
              {relecture.commentaire
                ? relecture.commentaire
                : "Aucun commentaire pour cette relecture."}
            </p>
          </article>
        ))}
        <p className="hint">Le nom du relecteur n&apos;est jamais affiché.</p>
      </section>
    </>
  );
}
