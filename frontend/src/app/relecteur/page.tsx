"use client";

import { useState, type FormEvent } from "react";
import { listerEtudiants, listerPromotions, listerRelectures, rendreRelecture } from "@/lib/api";
import type { Etudiant, Relecture, StatutRelecture } from "@/lib/api";
import { Badge, Chargement, MessageErreur, MessageSucces } from "@/components/ui";
import { useAction, useChargement } from "@/lib/feedback";
import { VALEUR_VIDE } from "@/lib/format";

export default function EcranRelecteur() {
  const promotions = useChargement(() => listerPromotions(), []);

  const [promotionId, setPromotionId] = useState<number | null>(null);
  const [relecteurChoisi, setRelecteurChoisi] = useState<number | null>(null);
  const [statutFiltre, setStatutFiltre] = useState<StatutRelecture | "">("");

  const etudiants = useChargement<Etudiant[]>(
    async () => (promotionId === null ? [] : listerEtudiants(promotionId)),
    [promotionId],
  );
  const relectures = useChargement<Relecture[]>(
    async () =>
      relecteurChoisi === null
        ? []
        : listerRelectures(relecteurChoisi, statutFiltre === "" ? undefined : statutFiltre),
    [relecteurChoisi, statutFiltre],
  );

  return (
    <>
      <h1>Écran relecteur</h1>

      <section className="card">
        <h2>Qui suis-je ?</h2>
        <p className="hint">
          Un relecteur est un étudiant désigné : choisissez votre promotion puis votre nom.
        </p>
        {promotions.enCours && <Chargement texte="Chargement des promotions…" />}
        <MessageErreur texte={promotions.erreur} />
        {(promotions.donnees ?? []).length > 0 && (
          <div className="champ">
            <label htmlFor="promotion">Promotion</label>
            <select
              id="promotion"
              className="select"
              value={promotionId === null ? "" : String(promotionId)}
              onChange={(event) => {
                setPromotionId(event.target.value === "" ? null : Number(event.target.value));
                setRelecteurChoisi(null);
              }}
            >
              <option value="">Choisir…</option>
              {(promotions.donnees ?? []).map((promotion) => (
                <option key={promotion.id} value={promotion.id}>
                  {promotion.nom}
                </option>
              ))}
            </select>
          </div>
        )}
        {promotionId !== null && (
          <div className="champ">
            <label htmlFor="relecteur">Mon nom</label>
            <select
              id="relecteur"
              className="select"
              value={relecteurChoisi === null ? "" : String(relecteurChoisi)}
              onChange={(event) =>
                setRelecteurChoisi(
                  event.target.value === "" ? null : Number(event.target.value),
                )
              }
            >
              <option value="">Choisir mon nom…</option>
              {(etudiants.donnees ?? []).map((etudiant) => (
                <option key={etudiant.id} value={etudiant.id}>
                  {etudiant.nom}
                </option>
              ))}
            </select>
          </div>
        )}
        {promotionId !== null && etudiants.enCours && (
          <Chargement texte="Chargement des étudiants…" />
        )}
        <MessageErreur texte={etudiants.erreur} />
      </section>

      <section className="card">
        <div className="item-liste__entete">
          <h2>Mes relectures</h2>
          <div className="filtre">
            <label htmlFor="statut">Filtrer par statut</label>
            <select
              id="statut"
              className="select"
              value={statutFiltre}
              onChange={(event) => setStatutFiltre(event.target.value as StatutRelecture | "")}
            >
              <option value="">Toutes</option>
              <option value="EN_ATTENTE">En attente</option>
              <option value="RENDUE">Rendues</option>
            </select>
          </div>
        </div>

        {relecteurChoisi === null && (
          <p className="hint">Choisissez votre nom pour voir les relectures qui vous sont affectées.</p>
        )}
        {relectures.enCours && <Chargement texte="Chargement de vos relectures…" />}
        <MessageErreur texte={relectures.erreur} />
        {relecteurChoisi !== null &&
          !relectures.enCours &&
          !relectures.erreur &&
          (relectures.donnees ?? []).length === 0 && (
            <p className="hint">Aucune relecture pour ce filtre.</p>
          )}
      </section>

      <div className="liste">
        {(relectures.donnees ?? []).map((relecture) => (
          <CarteRelecture
            key={relecture.id}
            relecture={relecture}
            surRendu={relectures.recharger}
          />
        ))}
      </div>
    </>
  );
}

function CarteRelecture({
  relecture,
  surRendu,
}: {
  relecture: Relecture;
  surRendu: () => void;
}) {
  const [note, setNote] = useState("");
  const [commentaire, setCommentaire] = useState("");
  const rendu = useAction();

  async function rendreNoteAction(event: FormEvent) {
    event.preventDefault();
    const ok = await rendu.executer(
      () =>
        rendreRelecture(relecture.id, {
          note: Number(note),
          commentaire: commentaire.trim(),
        }),
      "Note et commentaire enregistrés.",
    );
    if (ok) {
      setNote("");
      setCommentaire("");
      surRendu();
    }
  }

  return (
    <article className="item-liste">
      <div className="item-liste__entete">
        <h3>
          Relecture n° {relecture.id} · exercice n° {relecture.exerciceId}
        </h3>
        <Badge statut={relecture.statut} />
      </div>

      <p className="meta">
        Lien de l&apos;exercice à relire :{" "}
        <a className="lien-exercice" href={relecture.lien} target="_blank" rel="noopener noreferrer">
          {relecture.lien}
        </a>
      </p>

      {rendu.enCours && <Chargement texte="Enregistrement de la note…" />}
      {/* Message affiché même après le rechargement de la liste (statut « Rendue »). */}
      <MessageErreur texte={rendu.erreur} />
      <MessageSucces texte={rendu.succes} />

      {relecture.statut === "RENDUE" ? (
        <>
          <p className="meta">
            Note rendue :{" "}
            {relecture.note === null || relecture.note === undefined ? VALEUR_VIDE : relecture.note} / 20
          </p>
          <p>{relecture.commentaire ?? "Aucun commentaire."}</p>
        </>
      ) : (
        <form onSubmit={rendreNoteAction}>
          <div className="champ">
            <label htmlFor={`note-${relecture.id}`}>Note (entier de 0 à 20)</label>
            <input
              id={`note-${relecture.id}`}
              className="input"
              type="number"
              min={0}
              max={20}
              step={1}
              required
              inputMode="numeric"
              value={note}
              onChange={(event) => setNote(event.target.value)}
            />
          </div>
          <div className="champ">
            <label htmlFor={`commentaire-${relecture.id}`}>Commentaire</label>
            <textarea
              id={`commentaire-${relecture.id}`}
              className="textarea"
              required
              value={commentaire}
              onChange={(event) => setCommentaire(event.target.value)}
            />
          </div>
          <div className="boutons">
            <button className="bouton" type="submit" disabled={rendu.enCours}>
              {rendu.enCours ? "Envoi en cours…" : "Rendre ma relecture"}
            </button>
          </div>
        </form>
      )}
    </article>
  );
}
