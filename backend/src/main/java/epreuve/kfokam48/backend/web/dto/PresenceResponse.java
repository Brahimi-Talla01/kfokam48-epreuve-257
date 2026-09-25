package epreuve.kfokam48.backend.web.dto;

/** Réponse imposée de POST /api/presences : {id, sessionId, etudiantId, source}. */
public record PresenceResponse(Long id, Long sessionId, Long etudiantId, String source) {
}
