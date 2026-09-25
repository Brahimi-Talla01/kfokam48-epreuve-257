package epreuve.kfokam48.backend.web.dto;

import java.time.LocalDateTime;

/** Réponse imposée de POST /api/sessions : {id, code, ouvertureAt, expirationAt}. */
public record SessionOuvertureResponse(Long id, String code,
                                       LocalDateTime ouvertureAt, LocalDateTime expirationAt) {
}
