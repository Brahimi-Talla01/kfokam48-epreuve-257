package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Presence;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.SourcePresence;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.repository.RelectureRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Affectation du relecteur — issue #5, règles RG5 / RG6 / Q7.
 * Le tirage se fait parmi les PRÉSENTS à la session, auteur exclu ; sans pair présent,
 * la relecture existe quand même avec {@code relecteurId} null (cahier §7.2).
 */
@SpringBootTest
@ActiveProfiles("test")
class RelectureAffectationTest {

    @Autowired
    private SessionService sessions;

    @Autowired
    private ExerciceService exercices;

    @Autowired
    private RelectureService relectures;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private PresenceRepository presences;

    @Autowired
    private RelectureRepository relectureRepository;

    private Promotion promotion() {
        return promotions.findAllByOrderByIdAsc().get(0);
    }

    private List<Etudiant> etudiants(int nombre) {
        return etudiants.findByPromotionIdOrderByNomAsc(promotion().getId()).subList(0, nombre);
    }

    @Test
    @DisplayName("RG6 / Q7 : le relecteur est tiré parmi les présents, jamais l'auteur")
    void relecteurTireParmiLesPresents() {
        List<Etudiant> monde = etudiants(3);
        Etudiant auteur = monde.get(0);
        Etudiant present1 = monde.get(1);
        Etudiant present2 = monde.get(2);

        var session = sessions.creer("Session affectation", promotion().getId());
        presences.save(new Presence(session, present1, SourcePresence.ETUDIANT));
        presences.save(new Presence(session, present2, SourcePresence.ETUDIANT));

        Exercice exercice = exercices.deposer(session.getId(), auteur.getId(),
                "https://example.com/devoir");

        Relecture relecture = relectureRepository.findByExerciceId(exercice.getId()).orElseThrow();
        assertNotNull(relecture.getRelecteur(), "un relecteur doit être affecté");
        assertFalse(relecture.getRelecteur().getId().equals(auteur.getId()),
                "on ne se relit jamais soi-même (RG4 / Q5)");
        assertTrue(relecture.getRelecteur().getId().equals(present1.getId())
                        || relecture.getRelecteur().getId().equals(present2.getId()),
                "le relecteur doit faire partie des présents");
        assertEquals(StatutRelecture.EN_ATTENTE, relecture.getStatut());
    }

    @Test
    @DisplayName("RG5 : un seul relecteur par exercice — la seconde création répond 409")
    void unSeulRelecteurParExercice() {
        List<Etudiant> monde = etudiants(2);
        var session = sessions.creer("Session unicité", promotion().getId());
        presences.save(new Presence(session, monde.get(1), SourcePresence.ETUDIANT));

        Exercice exercice = exercices.deposer(session.getId(), monde.get(0).getId(),
                "https://example.com/devoir-2");

        ApiException erreur = assertThrows(ApiException.class, () -> relectures.creer(exercice.getId()));
        assertEquals("RELECTURE_DEJA_CREE", erreur.getCode());
        assertEquals(409, erreur.getStatus().value());
    }

    @Test
    @DisplayName("Cahier §7.2 : sans pair présent, la relecture existe avec relecteurId null")
    void aucunPairPresentLaisseLaRelectureVide() {
        Etudiant seul = etudiants(1).get(0);
        var session = sessions.creer("Session sans pair", promotion().getId());

        Exercice exercice = exercices.deposer(session.getId(), seul.getId(),
                "https://example.com/devoir-3");

        Relecture relecture = relectureRepository.findByExerciceId(exercice.getId()).orElseThrow();
        assertNull(relecture.getRelecteur(), "aucun pair n'était présent à la session");
        assertEquals(StatutRelecture.EN_ATTENTE, relecture.getStatut());
    }
}
