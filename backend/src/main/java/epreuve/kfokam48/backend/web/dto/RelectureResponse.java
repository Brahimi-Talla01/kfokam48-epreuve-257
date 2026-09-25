package epreuve.kfokam48.backend.web.dto;

/** Réponse de POST /api/relectures : {id, exerciceId, relecteurId, statut}. */
public record RelectureResponse(Long id, Long exerciceId, Long relecteurId, String statut) {
}
