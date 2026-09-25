package epreuve.kfokam48.backend.web.dto;

/** Réponse de POST /api/relectures/{id} : la relecture après rendu (200). */
public record RenduResponse(Long id, Long exerciceId, String statut, Integer note,
                            String commentaire) {
}
