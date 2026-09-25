package epreuve.kfokam48.backend.web.dto;

import java.time.LocalDateTime;

/**
 * Note reçue sur un exercice — GET /api/exercices/{id}/relectures.
 * Le nom (et l'identifiant) du relecteur **ne figure jamais** dans cette réponse (RG7 / Q8).
 */
public record NoteResponse(Long id, String statut, Integer note,
                           String commentaire, LocalDateTime rendueLe) {
}
