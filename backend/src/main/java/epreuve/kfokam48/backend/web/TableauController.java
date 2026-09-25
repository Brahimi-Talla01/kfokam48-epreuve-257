package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.service.TableauService;
import epreuve.kfokam48.backend.web.dto.TableauLigneResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * GET /api/tableau — opération imposée : 200 (lignes de la promotion) · 404 PROMOTION_INCONNUE.
 * Aucun autre code de statut n'est produit, conformément au contrat (B2).
 */
@RestController
@RequestMapping("/api")
public class TableauController {

    private final TableauService tableau;

    public TableauController(TableauService tableau) {
        this.tableau = tableau;
    }

    @GetMapping("/tableau")
    public List<TableauLigneResponse> tableau(@RequestParam(required = false) Long promotionId) {
        return tableau.tableau(promotionId);
    }
}
