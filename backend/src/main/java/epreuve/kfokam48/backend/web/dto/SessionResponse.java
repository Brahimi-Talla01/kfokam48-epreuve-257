package epreuve.kfokam48.backend.web.dto;

import java.time.LocalDateTime;

/**
 * Vue complète d'une session. {@code expirationAt} = fin du code (RG1 / Q2),
 * {@code clotureAt} = fin de session (RG16 / Q3-Q12) : deux instants distincts.
 */
public record SessionResponse(Long id, String titre, Long promotionId, String code, String statut,
                              LocalDateTime ouvertureAt, LocalDateTime expirationAt,
                              LocalDateTime clotureAt) {
}
