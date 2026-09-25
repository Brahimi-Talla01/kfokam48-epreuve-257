package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Presence;
import epreuve.kfokam48.backend.service.PresenceService;
import epreuve.kfokam48.backend.web.dto.PresenceRequest;
import epreuve.kfokam48.backend.web.dto.PresenceResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/presences — opération imposée du contrat.
 * 201 · 400 CODE_INCONNU · 409 DEJA_PRESENT · 410 CODE_EXPIRE
 * (+ 410 SESSION_TERMINEE pour RG2, + 400 ETUDIANT_INCONNU).
 */
@RestController
@RequestMapping("/api")
public class PresenceController {

    private final PresenceService presences;

    public PresenceController(PresenceService presences) {
        this.presences = presences;
    }

    @PostMapping("/presences")
    public ResponseEntity<PresenceResponse> marquer(@Valid @RequestBody PresenceRequest requete) {
        Presence presence = presences.marquer(requete.code(), requete.etudiantId(),
                requete.sourceEffective());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new PresenceResponse(presence.getId(),
                        presence.getSession().getId(),
                        presence.getEtudiant().getId(),
                        presence.getSource().name()));
    }
}
