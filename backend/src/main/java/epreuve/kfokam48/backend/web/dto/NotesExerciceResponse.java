package epreuve.kfokam48.backend.web.dto;

import java.util.List;

/**
 * Notes reçues sur un exercice — GET /api/exercices/{id}/relectures.
 * Étape 3 (RG19, ENVELOPPE §2) : {@code noteRetenue} est la moyenne des deux relectures
 * si les deux sont rendues, la note seule si une seule est rendue (alors
 * {@code provisoire = true}), {@code null} tant qu'aucune n'est rendue. Calculée par
 * l'API — le front ne moyenne rien (F3 / ENF6).
 */
public record NotesExerciceResponse(Double noteRetenue, boolean provisoire, List<NoteResponse> relectures) {
}
