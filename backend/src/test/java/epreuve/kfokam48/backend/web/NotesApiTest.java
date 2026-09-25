package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Presence;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.domain.SourcePresence;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.service.ExerciceService;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * GET /api/exercices/{id}/relectures — issue #8, règles RG7 / Q8 / Q11.
 * L'étudiant voit sa note et le commentaire, jamais le nom de son relecteur.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotesApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessions;

    @Autowired
    private ExerciceService exercices;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private PresenceRepository presenceRepository;

    private Long exerciceSansPair() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        List<Etudiant> monde = etudiants.findByPromotionIdOrderByNomAsc(promotion.getId());
        Session session = sessions.creer("Session notes " + UUID.randomUUID().toString().substring(0, 8),
                promotion.getId());
        // Aucun pair présent : la relecture existe avec relecteurId null (cahier §7.2).
        Exercice exercice = exercices.deposer(session.getId(), monde.get(0).getId(),
                "https://example.com/" + UUID.randomUUID().toString().substring(0, 8));
        return exercice.getId();
    }

    @Test
    @DisplayName("200 — la note, le commentaire et la date, sans identité du relecteur (RG7 / Q8)")
    void notesDeMonExercice() throws Exception {
        Long exerciceId = exerciceSansPair();

        mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteRetenue").value((Object) null))
                .andExpect(jsonPath("$.provisoire").value(false))
                .andExpect(jsonPath("$.relectures[0].statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.relectures[0].note").value((Object) null))
                .andExpect(jsonPath("$.relectures[0].commentaire").value((Object) null))
                .andExpect(jsonPath("$.relectures[0].relecteurId").doesNotExist())
                .andExpect(jsonPath("$.relectures[0].relecteur").doesNotExist())
                .andExpect(jsonPath("$.relectures[0].nomRelecteur").doesNotExist());
    }

    @Test
    @DisplayName("200 — après rendu (seul pair éligible) : note retenue définitive, toujours sans relecteur")
    void notesApresRendu() throws Exception {
        Long exerciceId = exerciceSansPair();

        // La relecture est déjà créée au dépôt (issues #3 et #5) : on la relit directement.
        String corps = mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertFalse(corps.contains("relecteurId"),
                "le corps des notes ne doit jamais contenir relecteurId");

        Long id = ((Number) com.jayway.jsonpath.JsonPath.read(corps, "$.relectures[0].id")).longValue();
        mockMvc.perform(post("/api/relectures/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":14,\"commentaire\":\"Bon travail, pensez à tester vos bornes.\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteRetenue").value(14.0))
                .andExpect(jsonPath("$.provisoire").value(false))
                .andExpect(jsonPath("$.relectures[0].statut").value("RENDUE"))
                .andExpect(jsonPath("$.relectures[0].note").value(14))
                .andExpect(jsonPath("$.relectures[0].commentaire").value("Bon travail, pensez à tester vos bornes."))
                .andExpect(jsonPath("$.relectures[0].rendueLe").isNotEmpty())
                .andExpect(jsonPath("$.relectures[0].relecteurId").doesNotExist());
    }

    private Long exerciceAvecDeuxPairs() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        List<Etudiant> monde = etudiants.findByPromotionIdOrderByNomAsc(promotion.getId());
        Session session = sessions.creer("Session deux pairs " + UUID.randomUUID().toString().substring(0, 8),
                promotion.getId());
        presenceRepository.save(new Presence(session, monde.get(1), SourcePresence.ETUDIANT));
        presenceRepository.save(new Presence(session, monde.get(2), SourcePresence.ETUDIANT));
        Exercice exercice = exercices.deposer(session.getId(), monde.get(0).getId(),
                "https://example.com/" + UUID.randomUUID().toString().substring(0, 8));
        return exercice.getId();
    }

    @Test
    @DisplayName("RG19 (étape 3) — un seul des deux relecteurs a rendu : note affichée, marquée provisoire")
    void uneSeuleDesDeuxNotesEstProvisoire() throws Exception {
        Long exerciceId = exerciceAvecDeuxPairs();
        String corps = mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andReturn().getResponse().getContentAsString();
        Long premiereRelectureId = ((Number) com.jayway.jsonpath.JsonPath.read(corps, "$.relectures[0].id")).longValue();

        mockMvc.perform(post("/api/relectures/" + premiereRelectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":10,\"commentaire\":\"Correct.\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteRetenue").value(10.0))
                .andExpect(jsonPath("$.provisoire").value(true));
    }

    @Test
    @DisplayName("RG19 (étape 3) — les deux relecteurs ont rendu : note retenue = moyenne, définitive")
    void moyenneDesDeuxQuandLesDeuxOntRendu() throws Exception {
        Long exerciceId = exerciceAvecDeuxPairs();
        String corps = mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andReturn().getResponse().getContentAsString();
        Long id1 = ((Number) com.jayway.jsonpath.JsonPath.read(corps, "$.relectures[0].id")).longValue();
        Long id2 = ((Number) com.jayway.jsonpath.JsonPath.read(corps, "$.relectures[1].id")).longValue();

        mockMvc.perform(post("/api/relectures/" + id1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":10,\"commentaire\":\"Correct.\"}"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/relectures/" + id2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":16,\"commentaire\":\"Tres bien.\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/exercices/" + exerciceId + "/relectures"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.noteRetenue").value(13.0))
                .andExpect(jsonPath("$.provisoire").value(false));
    }

    @Test
    @DisplayName("404 EXERCICE_INCONNU — exercice inexistant")
    void exerciceInconnu() throws Exception {
        mockMvc.perform(get("/api/exercices/999999/relectures"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCICE_INCONNU"));
    }
}
