package epreuve.kfokam48.backend.web;

import com.jayway.jsonpath.JsonPath;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/promotions et GET /api/promotions/{id}/etudiants — issue #11 / Q1.
 * La « session » de l'étudiant n'est qu'un id choisi dans une liste : aucun mot de passe.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PromotionsApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    private Long promotionA() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        assertEquals("KFOKAM48 Promotion A", promotion.getNom());
        return promotion.getId();
    }

    @Test
    @DisplayName("200 — la liste des promotions du jeu de démonstration")
    void listeDesPromotions() throws Exception {
        String json = mockMvc.perform(get("/api/promotions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].nom").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        List<?> liste = JsonPath.read(json, "$");
        assertEquals(2, liste.size(), "le seed V2 crée deux promotions");
    }

    @Test
    @DisplayName("200 — les étudiants de la promotion, triés, sans compte ni mot de passe (Q1)")
    void listeDesEtudiants() throws Exception {
        String json = mockMvc.perform(get("/api/promotions/" + promotionA() + "/etudiants"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<Map<String, Object>> etudiants = JsonPath.read(json, "$");
        assertEquals(8, etudiants.size(), "8 étudiants dans la promotion A (seed V2)");
        assertEquals("Bamba Awa", etudiants.get(0).get("nom"), "tri par nom");
        assertEquals(2, etudiants.get(0).keySet().size(), "exactement {id, nom}");

        assertFalse(json.contains("motDePasse"), "aucun mot de passe (Q1)");
        assertFalse(json.contains("email"), "aucun compte (Q1)");
    }

    @Test
    @DisplayName("404 PROMOTION_INCONNUE — message français lisible, pas de stack trace")
    void promotionInconnue() throws Exception {
        mockMvc.perform(get("/api/promotions/999999/etudiants"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"))
                .andExpect(jsonPath("$.message").value("La promotion demandée n'existe pas."))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }
}
