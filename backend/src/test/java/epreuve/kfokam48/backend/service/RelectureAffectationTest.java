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
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Affectation des relecteurs — issue #5 (RG6 / Q7) et issue #33, RG18 (étape 3,
 * ENVELOPPE §2, ex-RG5) : deux relecteurs distincts tirés parmi les PRÉSENTS à la
 * session, auteur exclu ; un seul si un seul pair est éligible ; aucun sans pair
 * présent — la relecture existe quand même avec {@code relecteurId} null (cahier §7.2).
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
    @DisplayName("RG18 : avec au moins deux pairs présents, deux relecteurs distincts sont affectés")
    void deuxRelecteursDistinctsSiAuMoinsDeuxPresents() {
        // Se limite aux 3 premiers étudiants (ordre alphabétique) pour ne pas fausser
        // les compteurs de présence des autres étudiants du seed, vérifiés ailleurs
        // (PresenceFormateurTest, TableauApiTest) sur la même promotion partagée.
        List<Etudiant> monde = etudiants(3);
        Etudiant auteur = monde.get(0);
        Etudiant present1 = monde.get(1);
        Etudiant present2 = monde.get(2);

        var session = sessions.creer("Session affectation", promotion().getId());
        presences.save(new Presence(session, present1, SourcePresence.ETUDIANT));
        presences.save(new Presence(session, present2, SourcePresence.ETUDIANT));

        Exercice exercice = exercices.deposer(session.getId(), auteur.getId(),
                "https://example.com/devoir");

        List<Relecture> creees = relectureRepository.findAllByExerciceIdOrderByIdAsc(exercice.getId());
        assertEquals(2, creees.size(), "deux relecteurs doivent être affectés (RG18)");

        Set<Long> idsRelecteurs = creees.stream()
                .map(r -> r.getRelecteur().getId())
                .collect(Collectors.toSet());
        assertEquals(2, idsRelecteurs.size(), "les deux relecteurs doivent être distincts");
        assertFalse(idsRelecteurs.contains(auteur.getId()), "on ne se relit jamais soi-même (RG4 / Q5)");
        assertTrue(Set.of(present1.getId(), present2.getId()).containsAll(idsRelecteurs),
                "les relecteurs doivent faire partie des présents");
        creees.forEach(r -> assertEquals(StatutRelecture.EN_ATTENTE, r.getStatut()));
    }

    @Test
    @DisplayName("RG18 : un seul pair éligible → une seule relecture affectée, et la seconde création répond 409")
    void unSeulRelecteurSiUnSeulPairEligible() {
        List<Etudiant> monde = etudiants(2);
        var session = sessions.creer("Session un seul pair", promotion().getId());
        presences.save(new Presence(session, monde.get(1), SourcePresence.ETUDIANT));

        Exercice exercice = exercices.deposer(session.getId(), monde.get(0).getId(),
                "https://example.com/devoir-2");

        List<Relecture> creees = relectureRepository.findAllByExerciceIdOrderByIdAsc(exercice.getId());
        assertEquals(1, creees.size(), "un seul pair éligible => une seule relecture (RG18)");
        assertEquals(monde.get(1).getId(), creees.get(0).getRelecteur().getId());

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

        List<Relecture> creees = relectureRepository.findAllByExerciceIdOrderByIdAsc(exercice.getId());
        assertEquals(1, creees.size());
        assertNull(creees.get(0).getRelecteur(), "aucun pair n'était présent à la session");
        assertEquals(StatutRelecture.EN_ATTENTE, creees.get(0).getStatut());
    }
}
