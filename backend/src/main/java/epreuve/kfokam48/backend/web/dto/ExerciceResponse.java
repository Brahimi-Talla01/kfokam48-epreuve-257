package epreuve.kfokam48.backend.web.dto;

/** Réponse imposée de POST /api/exercices : {id, statut}. */
public record ExerciceResponse(Long id, String statut) {
}
