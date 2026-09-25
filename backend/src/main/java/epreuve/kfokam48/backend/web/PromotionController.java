package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.service.PromotionService;
import epreuve.kfokam48.backend.web.dto.EtudiantResponse;
import epreuve.kfokam48.backend.web.dto.PromotionResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/promotions · GET /api/promotions/{id}/etudiants (ajoutées, issue #11 / Q1) :
 * alimentent le sélecteur de nom de l'écran étudiant — 200 · 404 PROMOTION_INCONNUE.
 */
@RestController
@RequestMapping("/api")
public class PromotionController {

    private final PromotionService promotions;

    public PromotionController(PromotionService promotions) {
        this.promotions = promotions;
    }

    @GetMapping("/promotions")
    public List<PromotionResponse> lister() {
        return promotions.lister().stream()
                .map(promotion -> new PromotionResponse(promotion.getId(), promotion.getNom()))
                .toList();
    }

    @GetMapping("/promotions/{id}/etudiants")
    public List<EtudiantResponse> etudiants(@PathVariable Long id) {
        return promotions.listerEtudiants(id).stream()
                .map(etudiant -> new EtudiantResponse(etudiant.getId(), etudiant.getNom()))
                .toList();
    }
}
