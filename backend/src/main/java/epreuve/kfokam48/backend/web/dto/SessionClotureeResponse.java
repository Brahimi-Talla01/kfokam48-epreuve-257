package epreuve.kfokam48.backend.web.dto;

import java.time.LocalDateTime;

/** Réponse de POST /api/sessions/{id}/cloturer : {id, statut, clotureAt} (contrat ajouté). */
public record SessionClotureeResponse(Long id, String statut, LocalDateTime clotureAt) {
}
