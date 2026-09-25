package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.service.TentativePresenceTracker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * POST /api/presences — issue #15, RG3 / Q4 : après 5 codes erronés, l'étudiant est
 * bloqué 2 minutes. Décision cahier §7.2 : {@code 400 TROP_DE_TENTATIVES}, jamais un
 * nouveau code de statut sur l'opération imposée (B2).
 * <p>
 * Le tracker est un singleton Spring dont l'état persiste entre les méthodes de test
 * (contexte partagé) : chaque test crée son propre étudiant pour ne jamais partager
 * de compteur avec un autre test.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BlocageTentativesApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    private Long nouvelEtudiant() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        return etudiants.save(new Etudiant("Test blocage " + System.nanoTime(), promotion)).getId();
    }

    private void codeInconnu(Long etudiantId) throws Exception {
        mockMvc.perform(post("/api/presences")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"code\":\"ZZZZZZZZ\",\"etudiantId\":" + etudiantId + "}"));
    }

    @Test
    @DisplayName("400 TROP_DE_TENTATIVES — bloqué après le 5e code erroné, même avec un code valide")
    void bloqueApresCinqCodesErrones() throws Exception {
        Long etudiantId = nouvelEtudiant();

        for (int i = 0; i < TentativePresenceTracker.MAX_TENTATIVES; i++) {
            codeInconnu(etudiantId);
        }

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ZZZZZZZZ\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("TROP_DE_TENTATIVES"));
    }

    @Test
    @DisplayName("Quatre codes erronés ne bloquent pas encore")
    void quatreEchecsNeBloquentPas() throws Exception {
        Long etudiantId = nouvelEtudiant();

        for (int i = 0; i < TentativePresenceTracker.MAX_TENTATIVES - 1; i++) {
            codeInconnu(etudiantId);
        }

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ZZZZZZZZ\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"));
    }
}
