package epreuve.kfokam48.backend.web.dto;

import jakarta.validation.constraints.NotNull;

/**
 * Corps imposé de POST /api/relectures/{id} : {note, commentaire}.
 * {@code relecteurId} est une propriété facultative ajoutée par le candidat : sans elle,
 * impossible d'appliquer RG4 (interdiction de se relire soi-même) — le contrat n'impose
 * que le chemin, le verbe, les codes de statut et le format d'erreur (B2).
 */
public record RenduRequest(
        @NotNull(message = "La note est obligatoire.")
        Integer note,

        @NotNull(message = "Le commentaire est obligatoire.")
        String commentaire,

        Long relecteurId) {
}
