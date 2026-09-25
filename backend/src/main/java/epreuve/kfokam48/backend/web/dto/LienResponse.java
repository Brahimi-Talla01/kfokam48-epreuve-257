package epreuve.kfokam48.backend.web.dto;

import java.time.LocalDateTime;

/** Réponse de PUT /api/exercices/{id}/lien : {id, statut, misAJourLe} (contrat ajouté). */
public record LienResponse(Long id, String statut, LocalDateTime misAJourLe) {
}
