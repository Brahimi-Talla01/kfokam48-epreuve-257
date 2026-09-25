package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.service.SessionService;
import epreuve.kfokam48.backend.web.dto.SessionCreateRequest;
import epreuve.kfokam48.backend.web.dto.SessionClotureeResponse;
import epreuve.kfokam48.backend.web.dto.SessionOuvertureResponse;
import epreuve.kfokam48.backend.web.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Opérations imposées et ajoutées autour de la session.
 * Les chemins, verbes et codes de statut suivent api/contrat.yaml à la lettre (B2).
 */
@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessions;

    public SessionController(SessionService sessions) {
        this.sessions = sessions;
    }

    /** Contrat imposé — 201 {id, code, ouvertureAt, expirationAt} · 400. */
    @PostMapping("/sessions")
    public ResponseEntity<SessionOuvertureResponse> ouvrir(@Valid @RequestBody SessionCreateRequest requete) {
        Session session = sessions.creer(requete.titre(), requete.promotionId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SessionOuvertureResponse(session.getId(), session.getCode(),
                        session.getOuvertureAt(), session.getExpirationAt()));
    }

    /** Ajoutée — liste des sessions d'une promotion pour l'écran formateur. 200 · 404. */
    @GetMapping("/sessions")
    public List<SessionResponse> lister(@RequestParam Long promotionId) {
        return sessions.lister(promotionId).stream().map(this::versDto).toList();
    }

    /** Ajoutée — détail d'une session : code, statut, expiration, clôture. 200 · 404. */
    @GetMapping("/sessions/{id}")
    public SessionResponse detail(@PathVariable Long id) {
        return versDto(sessions.trouver(id));
    }

    /**
     * Ajoutée (issue #7 / RG16) — clôture de session : 200 {id, statut, clotureAt} ·
     * 404 SESSION_INCONNUE · 409 SESSION_DEJA_CLOTUREE. Ferme le dépôt d'exercices (RG11)
     * et le marquage des présences (RG2), sans rien changer à l'expiration du code (RG1).
     */
    @PostMapping("/sessions/{id}/cloturer")
    public SessionClotureeResponse cloturer(@PathVariable Long id) {
        Session session = sessions.cloturer(id);
        return new SessionClotureeResponse(session.getId(), session.getStatut().name(),
                session.getClotureAt());
    }

    private SessionResponse versDto(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getTitre(),
                session.getPromotion().getId(),
                session.getCode(),
                session.getStatut().name(),
                session.getOuvertureAt(),
                session.getExpirationAt(),
                session.getClotureAt());
    }
}
