package epreuve.kfokam48.backend.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Corps imposé de POST /api/sessions (contrat) : {titre, promotionId}.
 * Validation d'entrée (B4) → 400 VALIDATION si un champ obligatoire manque.
 */
public record SessionCreateRequest(
        @NotBlank(message = "Le titre de la session est obligatoire.")
        @Size(max = 200, message = "Le titre ne peut pas dépasser 200 caractères.")
        String titre,

        @NotNull(message = "La promotion est obligatoire.")
        Long promotionId) {
}
