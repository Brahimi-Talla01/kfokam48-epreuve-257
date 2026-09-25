package epreuve.kfokam48.backend.web.dto;

import epreuve.kfokam48.backend.domain.SourcePresence;
import jakarta.validation.constraints.NotNull;

/**
 * Corps imposé de POST /api/presences : {code, etudiantId}.
 * {@code source} est une propriété <b>facultative</b> ajoutée par le candidat (Q14 / RG13) :
 * chemin, verbe, codes de statut et format d'erreur restent identiques au contrat (B2).
 */
public record PresenceRequest(
        @NotNull(message = "Le code de présence est obligatoire.")
        String code,

        @NotNull(message = "L'étudiant est obligatoire.")
        Long etudiantId,

        SourcePresence source) {

    public SourcePresence sourceEffective() {
        return source == null ? SourcePresence.ETUDIANT : source;
    }
}
