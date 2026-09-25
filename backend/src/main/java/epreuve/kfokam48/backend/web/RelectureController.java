package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import epreuve.kfokam48.backend.service.RelectureService;
import epreuve.kfokam48.backend.web.dto.NoteResponse;
import epreuve.kfokam48.backend.web.dto.RelectureCreateRequest;
import epreuve.kfokam48.backend.web.dto.RelectureDetailResponse;
import epreuve.kfokam48.backend.web.dto.RelectureResponse;
import epreuve.kfokam48.backend.web.dto.RenduRequest;
import epreuve.kfokam48.backend.web.dto.RenduResponse;
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
 * POST /api/relectures — création / affectation (ajoutée, Q7).
 * GET /api/relectures — écran relecteur (ajoutée).
 * Le rendu de la note reste sur POST /api/relectures/{id}, opération imposée.
 */
@RestController
@RequestMapping("/api")
public class RelectureController {

    private final RelectureService relectures;

    public RelectureController(RelectureService relectures) {
        this.relectures = relectures;
    }

    @PostMapping("/relectures")
    public ResponseEntity<RelectureResponse> creer(@Valid @RequestBody RelectureCreateRequest requete) {
        Relecture relecture = relectures.creer(requete.exerciceId());
        return ResponseEntity.status(HttpStatus.CREATED).body(versDto(relecture));
    }

    @GetMapping("/relectures")
    public List<RelectureDetailResponse> lister(@RequestParam Long relecteurId,
                                                @RequestParam(required = false) StatutRelecture statut) {
        return relectures.lister(relecteurId, statut).stream().map(this::versDetail).toList();
    }

    /** Opération imposée — 200 · 400 NOTE_INVALIDE · 403 AUTO_RELECTURE · 409 RELECTURE_DEJA_RENDUE. */
    @PostMapping("/relectures/{id}")
    public ResponseEntity<RenduResponse> rendre(@PathVariable Long id,
                                                @Valid @RequestBody RenduRequest requete) {
        Relecture relecture = relectures.rendre(id, requete.note(), requete.commentaire(),
                requete.relecteurId());
        return ResponseEntity.ok(new RenduResponse(relecture.getId(),
                relecture.getExercice().getId(),
                relecture.getStatut().name(),
                relecture.getNote(),
                relecture.getCommentaire()));
    }

    /**
     * Ajoutée (issue #8) — notes reçues sur un exercice : 200 · 404 EXERCICE_INCONNU.
     * RG7 / Q8 : aucun champ d'identité du relecteur dans la réponse.
     */
    @GetMapping("/exercices/{id}/relectures")
    public List<NoteResponse> notes(@PathVariable Long id) {
        return relectures.listerPourExercice(id).stream()
                .map(relecture -> new NoteResponse(
                        relecture.getId(),
                        relecture.getStatut().name(),
                        relecture.getNote(),
                        relecture.getCommentaire(),
                        relecture.getRendueLe()))
                .toList();
    }

    private RelectureResponse versDto(Relecture relecture) {
        return new RelectureResponse(relecture.getId(),
                relecture.getExercice().getId(),
                relecture.getRelecteur() == null ? null : relecture.getRelecteur().getId(),
                relecture.getStatut().name());
    }

    private RelectureDetailResponse versDetail(Relecture relecture) {
        return new RelectureDetailResponse(relecture.getId(),
                relecture.getExercice().getId(),
                relecture.getRelecteur() == null ? null : relecture.getRelecteur().getId(),
                relecture.getStatut().name(),
                relecture.getExercice().getLien(),
                relecture.getNote(),
                relecture.getCommentaire());
    }
}
