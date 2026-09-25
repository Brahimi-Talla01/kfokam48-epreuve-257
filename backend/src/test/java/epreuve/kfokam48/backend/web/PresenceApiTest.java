package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.repository.SessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test d'intégration sur un endpoint réel — B6. Tourne sur H2 en mémoire avec les
 * migrations Flyway, donc sur un poste vierge, sans Docker ni base locale.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresenceApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessions;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    private String premierEtudiantId() {
        return String.valueOf(etudiants.findByPromotionIdOrderByNomAsc(premierPromotion().getId()).get(0).getId());
    }

    private Promotion premierPromotion() {
        return promotions.findAllByOrderByIdAsc().get(0);
    }

    private Session nouvelleSessionOuverte() {
        LocalDateTime ouverture = LocalDateTime.now();
        return sessions.save(new Session("Session de test", premierPromotion(),
                codeUnique(), ouverture, ouverture.plusMinutes(15)));
    }

    private String codeUnique() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    @Test
    @DisplayName("201 — un code valide enregistre la présence (EF2)")
    void codeValideCreeUnePresence() throws Exception {
        String code = nouvelleSessionOuverte().getCode();

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + premierEtudiantId() + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.sessionId").isNumber())
                .andExpect(jsonPath("$.etudiantId").isNumber())
                .andExpect(jsonPath("$.source").value("ETUDIANT"));
    }

    @Test
    @DisplayName("400 CODE_INCONNU — code absent de la base")
    void codeInconnuRenvoie400() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"ZZZZZZZZ\",\"etudiantId\":" + premierEtudiantId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CODE_INCONNU"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @DisplayName("409 DEJA_PRESENT — une seule présence par étudiant et par session (RG14)")
    void doublePresenceRenvoie409() throws Exception {
        String code = nouvelleSessionOuverte().getCode();
        String corps = "{\"code\":\"" + code + "\",\"etudiantId\":" + premierEtudiantId() + "}";

        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DEJA_PRESENT"));
    }

    @Test
    @DisplayName("410 CODE_EXPIRE — le code a plus de 15 minutes (RG1 / Q2)")
    void codeExpireRenvoie410() throws Exception {
        LocalDateTime ouverture = LocalDateTime.now().minusMinutes(30);
        Session expiree = sessions.save(new Session("Session expirée", premierPromotion(),
                codeUnique(), ouverture, ouverture.plusMinutes(15)));

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + expiree.getCode() + "\",\"etudiantId\":" + premierEtudiantId() + "}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("CODE_EXPIRE"));
    }

    @Test
    @DisplayName("410 SESSION_TERMINEE — session clôturée par le formateur (RG2 / RG16)")
    void sessionClotureeRenvoie410() throws Exception {
        LocalDateTime ouverture = LocalDateTime.now().minusMinutes(1);
        Session cloturee = new Session("Session clôturée", premierPromotion(), codeUnique(),
                ouverture, ouverture.plusMinutes(15));
        cloturee.cloturer(LocalDateTime.now());
        sessions.save(cloturee);

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + cloturee.getCode() + "\",\"etudiantId\":" + premierEtudiantId() + "}"))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("SESSION_TERMINEE"));
    }

    @Test
    @DisplayName("201 avec source FORMATEUR — présence relevée par le formateur (Q14 / RG13)")
    void presenceAjouteeParLeFormateur() throws Exception {
        String code = nouvelleSessionOuverte().getCode();

        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + premierEtudiantId()
                                + ",\"source\":\"FORMATEUR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"));
    }

    @Test
    @DisplayName("400 VALIDATION — corps incomplet, message lisible, jamais de stack trace (B4)")
    void corpsIncompletRenvoie400Lisible() throws Exception {
        mockMvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + nouvelleSessionOuverte().getCode() + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.message", not(containsString("at epreuve"))));
    }
}
