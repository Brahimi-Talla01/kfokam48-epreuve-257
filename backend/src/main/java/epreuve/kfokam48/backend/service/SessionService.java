package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.repository.SessionRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ouverture et consultation des sessions.
 * RG1 / Q2 : le code expire 15 minutes après l'ouverture — règle tranchée, pas inventée.
 */
@Service
public class SessionService {

    /** RG1 (Q2) : durée de validité du code de présence. */
    public static final int DUREE_VALIDITE_CODE_MINUTES = 15;

    private static final String ALPHABET_CODE = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // sans O/0, I/1
    private static final int LONGUEUR_CODE = 8;

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final SecureRandom aleatoire = new SecureRandom();

    public SessionService(SessionRepository sessions, PromotionRepository promotions) {
        this.sessions = sessions;
        this.promotions = promotions;
    }

    @Transactional
    public Session creer(String titre, Long promotionId) {
        Promotion promotion = promotions.findById(promotionId)
                .orElseThrow(() -> ApiException.badRequest("PROMOTION_INCONNUE",
                        "La promotion demandée n'existe pas."));
        LocalDateTime ouverture = LocalDateTime.now();
        Session session = new Session(titre, promotion, genererCodeUnique(),
                ouverture, ouverture.plusMinutes(DUREE_VALIDITE_CODE_MINUTES));
        return sessions.save(session);
    }

    @Transactional(readOnly = true)
    public List<Session> lister(Long promotionId) {
        verifierPromotionExistante(promotionId);
        return sessions.findByPromotionIdOrderByIdDesc(promotionId);
    }

    @Transactional(readOnly = true)
    public Session trouver(Long id) {
        return sessions.findById(id)
                .orElseThrow(() -> ApiException.notFound("SESSION_INCONNUE",
                        "La session demandée n'existe pas."));
    }

    /**
     * Clôture de session — POST /api/sessions/{id}/cloturer (issue #7 / RG16).
     * Opération <b>ajoutée</b> au contrat : le sujet n'en décrit aucune alors que le cahier
     * des charges en a besoin (Q3 / Q12 : on ne peut plus rien faire après) — trou n° 1 du §7.2.
     * 200 · 404 SESSION_INCONNUE · 409 SESSION_DEJA_CLOTUREE.
     */
    @Transactional
    public Session cloturer(Long id) {
        Session session = trouver(id);
        if (!session.estOuverte()) {
            throw ApiException.conflict("SESSION_DEJA_CLOTUREE",
                    "Cette session est déjà clôturée.");
        }
        session.cloturer(LocalDateTime.now());
        return sessions.save(session);
    }

    protected void verifierPromotionExistante(Long promotionId) {
        if (promotionId == null || !promotions.existsById(promotionId)) {
            throw ApiException.notFound("PROMOTION_INCONNUE", "La promotion demandée n'existe pas.");
        }
    }

    private String genererCodeUnique() {
        for (int tentative = 0; tentative < 20; tentative++) {
            StringBuilder code = new StringBuilder(LONGUEUR_CODE);
            for (int i = 0; i < LONGUEUR_CODE; i++) {
                code.append(ALPHABET_CODE.charAt(aleatoire.nextInt(ALPHABET_CODE.length())));
            }
            if (!sessions.existsByCode(code.toString())) {
                return code.toString();
            }
        }
        throw ApiException.conflict("CODE_NON_GENERE", "Impossible de générer un code de présence unique.");
    }
}
