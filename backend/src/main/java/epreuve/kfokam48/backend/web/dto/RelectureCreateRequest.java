package epreuve.kfokam48.backend.web.dto;

import jakarta.validation.constraints.NotNull;

/** Corps de POST /api/relectures (création / affectation) : {exerciceId}. */
public record RelectureCreateRequest(
        @NotNull(message = "L'exercice est obligatoire.")
        Long exerciceId) {
}
