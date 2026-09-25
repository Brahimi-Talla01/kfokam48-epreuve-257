package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.service.SessionService;
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
 * POST /api/sessions/{id}/cloturer — opération ajoutée au contrat (issue #7 / RG16).
 * Rappel : clôturer ≠ expirer — l'expiration du code (RG1, 15 min) reste gérée séparément.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClotureSessionTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessions;

    @Autowired
    private PromotionRepository promotions;

    private Long nouvelleSession() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        return sessions.creer("Session à clôturer", promotion.getId()).getId();
    }

    @Test
    @DisplayName("200 — le formateur clôture et clotureAt est renseigné (Q3 / Q12 / RG16)")
    void clotureValide() throws Exception {
        Long id = nouvelleSession();

        mockMvc.perform(post("/api/sessions/" + id + "/cloturer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.statut").value("CLOTUREE"))
                .andExpect(jsonPath("$.clotureAt").isNotEmpty());
    }

    @Test
    @DisplayName("409 — une session déjà clôturée ne se clôture pas deux fois")
    void doubleCloture() throws Exception {
        Long id = nouvelleSession();

        mockMvc.perform(post("/api/sessions/" + id + "/cloturer"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/sessions/" + id + "/cloturer"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_CLOTUREE"));
    }

    @Test
    @DisplayName("404 — session inconnue")
    void sessionInconnue() throws Exception {
        mockMvc.perform(post("/api/sessions/999999/cloturer"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    @DisplayName("RG2 — après clôture, une présence répond 410 SESSION_TERMINEE")
    void presenceRefuseeApresCloture() throws Exception {
        Long id = nouvelleSession();
        String code = sessions.trouver(id).getCode();
        mockMvc.perform(post("/api/sessions/" + id + "/cloturer"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":1}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("SESSION_TERMINEE"));
    }
}
