package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.service.ExerciceService;
import epreuve.kfokam48.backend.web.dto.ExerciceCreateRequest;
import epreuve.kfokam48.backend.web.dto.ExerciceResponse;
import epreuve.kfokam48.backend.web.dto.LienRequest;
import epreuve.kfokam48.backend.web.dto.LienResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * POST /api/exercices — opération imposée : 201 {id, statut} · 400 LIEN_INVALIDE
 * · 409 EXERCICE_DEJA_DEPOSE (+ 409 SESSION_TERMINEE, RG11).
 * PUT /api/exercices/{id}/lien — ajoutée : RG12 / Q13.
 */
@RestController
@RequestMapping("/api")
public class ExerciceController {

    private final ExerciceService exercices;

    public ExerciceController(ExerciceService exercices) {
        this.exercices = exercices;
    }

    @PostMapping("/exercices")
    public ResponseEntity<ExerciceResponse> deposer(@Valid @RequestBody ExerciceCreateRequest requete) {
        Exercice exercice = exercices.deposer(requete.sessionId(), requete.etudiantId(), requete.lien());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ExerciceResponse(exercice.getId(), exercice.getStatut().name()));
    }

    @PutMapping("/exercices/{id}/lien")
    public ResponseEntity<LienResponse> remplacerLien(@PathVariable Long id,
                                                      @Valid @RequestBody LienRequest requete) {
        Exercice exercice = exercices.remplacerLien(id, requete.lien());
        return ResponseEntity.ok(new LienResponse(exercice.getId(), exercice.getStatut().name(),
                exercice.getMiseAJourLe()));
    }
}
