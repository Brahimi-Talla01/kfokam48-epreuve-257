package epreuve.kfokam48.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Corps de PUT /api/exercices/{id}/lien (RG12 / Q13). */
public record LienRequest(
        @NotBlank(message = "Le lien de l'exercice est obligatoire.")
        @Size(max = 500, message = "Le lien ne peut pas dépasser 500 caractères.")
        String lien) {
}
