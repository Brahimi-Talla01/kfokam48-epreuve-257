package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Promotions et listes d'étudiants — issue #11 / Q1.
 * La sélection se fait « juste un nom dans une liste » : aucun compte, aucun mot de passe.
 */
@Service
public class PromotionService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public PromotionService(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    @Transactional(readOnly = true)
    public List<Promotion> lister() {
        return promotions.findAllByOrderByIdAsc();
    }

    @Transactional(readOnly = true)
    public List<Etudiant> listerEtudiants(Long promotionId) {
        if (promotionId == null || !promotions.existsById(promotionId)) {
            throw ApiException.notFound("PROMOTION_INCONNUE", "La promotion demandée n'existe pas.");
        }
        return etudiants.findByPromotionIdOrderByNomAsc(promotionId);
    }
}
