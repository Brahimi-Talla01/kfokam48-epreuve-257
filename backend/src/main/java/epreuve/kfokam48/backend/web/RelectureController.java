package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import epreuve.kfokam48.backend.service.RelectureService;
import epreuve.kfokam48.backend.web.dto.RelectureCreateRequest;
import epreuve.kfokam48.backend.web.dto.RelectureDetailResponse;
import epreuve.kfokam48.backend.web.dto.RelectureResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
