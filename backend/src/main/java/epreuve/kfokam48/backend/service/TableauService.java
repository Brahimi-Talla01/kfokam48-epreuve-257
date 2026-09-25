package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.StatutRelecture;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.web.ApiException;
import epreuve.kfokam48.backend.web.dto.TableauLigneResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Tableau de progression de la promotion (Q16 / RG15).
 * Une seule requête SQL : les 4 colonnes sont calculées côté base, puis arrondies ici —
 * le front n'additionne ni ne moyenne rien (RG17), et on reste large sous les 2 s à 60
 * étudiants (ENF2).
 */
@Service
public class TableauService {

    private final EtudiantRepository etudiants;
    private final PromotionRepository promotions;

    public TableauService(EtudiantRepository etudiants, PromotionRepository promotions) {
        this.etudiants = etudiants;
        this.promotions = promotions;
    }

    @Transactional(readOnly = true)
    public List<TableauLigneResponse> tableau(Long promotionId) {
        if (promotionId == null || !promotions.existsById(promotionId)) {
            throw ApiException.notFound("PROMOTION_INCONNUE", "La promotion demandée n'existe pas.");
        }

        List<Object[]> lignes = etudiants.tableauPromotion(promotionId, StatutRelecture.EN_ATTENTE);
        return lignes.stream()
                .map(ligne -> new TableauLigneResponse(
                        (Long) ligne[0],
                        (String) ligne[1],
                        nombre(ligne[2]),
                        nombre(ligne[3]),
                        moyenneArrondie(ligne[4]),
                        nombre(ligne[5])))
                .toList();
    }

    private Long nombre(Object valeur) {
        return valeur == null ? 0L : ((Number) valeur).longValue();
    }

    /** RG17 : arrondi à 2 décimales, null s'il n'y a encore aucune note rendue. */
    private Double moyenneArrondie(Object valeur) {
        if (valeur == null) {
            return null;
        }
        double moyenne = ((Number) valeur).doubleValue();
        return Math.round(moyenne * 100.0) / 100.0;
    }
}
