package epreuve.kfokam48.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Corps imposé de POST /api/exercices : {sessionId, etudiantId, lien}. */
public record ExerciceCreateRequest(
        @NotNull(message = "La session est obligatoire.")
        Long sessionId,

        @NotNull(message = "L'étudiant est obligatoire.")
        Long etudiantId,

        @NotBlank(message = "Le lien de l'exercice est obligatoire.")
        @Size(max = 500, message = "Le lien ne peut pas dépasser 500 caractères.")
        String lien) {
}
