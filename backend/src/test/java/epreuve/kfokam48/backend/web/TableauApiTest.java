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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/tableau — opération imposée (Q16 / RG15 / RG17).
 * Les valeurs sont celles du jeu de démonstration V2 : c'est en même temps une vérification
 * que le seed et la requête du tableau restent cohérents.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TableauApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PromotionRepository promotions;

    private Long promotionA() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        assertEquals("KFOKAM48 Promotion A", promotion.getNom());
        return promotion.getId();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ligne(String json, String nom) {
        List<Map<String, Object>> lignes = JsonPath.read(json, "$");
        return lignes.stream()
                .filter(ligne -> nom.equals(ligne.get("nom")))
                .findFirst()
                .orElseGet(() -> fail("aucune ligne pour " + nom));
    }

    @Test
    @DisplayName("200 — présences, dépôts, moyenne arrondie et relectures en attente (Q16)")
    void tableauDeLaPromotion() throws Exception {
        String json = mockMvc.perform(get("/api/tableau").param("promotionId", String.valueOf(promotionA())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<?> toutes = JsonPath.read(json, "$");
        assertEquals(8, toutes.size(), "une ligne par étudiant de la promotion");

        Map<String, Object> koffi = ligne(json, "Koffi Aya");
        assertEquals(2L, ((Number) koffi.get("presences")).longValue(), "2 sessions suivies");
        assertEquals(2L, ((Number) koffi.get("exercicesDeposes")).longValue(), "1 dépôt par session");
        assertEquals(15.0, ((Number) koffi.get("moyenne")).doubleValue(), 0.001, "RG17 : note unique");
        assertEquals(1L, ((Number) koffi.get("relecturesEnAttente")).longValue(), "RG10 / Q11");

        Map<String, Object> kouassi = ligne(json, "Kouassi Serge");
        assertEquals(2L, ((Number) kouassi.get("presences")).longValue());
        assertEquals(0L, ((Number) kouassi.get("exercicesDeposes")).longValue());
        assertNull(kouassi.get("moyenne"), "RG17 : null tant qu'aucune note n'est rendue");
        assertEquals(0L, ((Number) kouassi.get("relecturesEnAttente")).longValue());
    }

    @Test
    @DisplayName("200 — aucune note : moyenne absente, jamais 0 (RG17)")
    void moyenneAbsenteSansNote() throws Exception {
        String json = mockMvc.perform(get("/api/tableau").param("promotionId", String.valueOf(promotionA())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> diabate = ligne(json, "Diabate Fatoumata");
        assertNull(diabate.get("moyenne"), "ses deux relectures sont encore EN_ATTENTE");
        assertEquals(2L, ((Number) diabate.get("relecturesEnAttente")).longValue());
    }

    @Test
    @DisplayName("404 PROMOTION_INCONNUE — promotion inexistante ou paramètre absent")
    void promotionInconnue() throws Exception {
        mockMvc.perform(get("/api/tableau").param("promotionId", "999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));

        mockMvc.perform(get("/api/tableau"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
