package epreuve.kfokam48.backend.web;

import com.jayway.jsonpath.JsonPath;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Présence relevée par le formateur — issue #9, règles RG13 / RG14 / Q14.
 * Le champ {@code source} est facultatif (défaut ETUDIANT) : chemin, verbe, codes de statut
 * et format d'erreur de l'opération imposée restent strictement identiques.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresenceFormateurTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessions;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    private String sessionOuverte() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        return sessions.creer("Session relevé " + UUID.randomUUID().toString().substring(0, 8),
                promotion.getId()).getCode();
    }

    private Long premierEtudiant() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        return etudiants.findByPromotionIdOrderByNomAsc(promotion.getId()).get(0).getId();
    }

    @Test
    @DisplayName("201 source ETUDIANT — valeur par défaut, champ facultatif (Q14)")
    void sourceParDefaut() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + sessionOuverte() + "\",\"etudiantId\":" + premierEtudiant() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    @DisplayName("201 source FORMATEUR — relevé manuel tracé comme tel (RG13)")
    void sourceFormateurConservee() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + sessionOuverte() + "\",\"etudiantId\":" + premierEtudiant()
                                + ",\"source\":\"FORMATEUR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"));
    }

    @Test
    @DisplayName("409 DEJA_PRESENT — la source ne change rien à l'unicité (RG14)")
    void uniciteIndependanteDeLaSource() throws Exception {
        String code = sessionOuverte();
        String corps = "{\"code\":\"" + code + "\",\"etudiantId\":" + premierEtudiant();

        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content(corps + ",\"source\":\"ETUDIANT\"}"))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content(corps + ",\"source\":\"FORMATEUR\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    @DisplayName("RG13 — le tableau distingue les présences relevées par le formateur")
    void tableauDistingueLesSources() throws Exception {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        String json = mockMvc.perform(get("/api/tableau").param("promotionId", String.valueOf(promotion.getId())))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        Map<String, Object> nguessan = ligne(json, "Nguessan Eric");
        // Seed V2 : présent en session clôturée (ETUDIANT) + relevé manuel en session ouverte (FORMATEUR).
        assertEquals(2L, ((Number) nguessan.get("presences")).longValue());
        assertEquals(1L, ((Number) nguessan.get("presencesFormateur")).longValue(),
                "sa présence relevée par le formateur est identifiable");

        Map<String, Object> koffi = ligne(json, "Koffi Aya");
        assertEquals(0L, ((Number) koffi.get("presencesFormateur")).longValue(),
                "aucune présence relevée par le formateur pour lui");
        assertEquals(2L, ((Number) koffi.get("presences")).longValue(),
                "ses deux présences ont été saisies par lui-même");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ligne(String json, String nom) {
        List<Map<String, Object>> lignes = JsonPath.read(json, "$");
        return lignes.stream()
                .filter(ligne -> nom.equals(ligne.get("nom")))
                .findFirst()
                .orElseGet(() -> fail("aucune ligne pour " + nom));
    }
}
