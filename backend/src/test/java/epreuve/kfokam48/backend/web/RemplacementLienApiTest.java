package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PromotionRepository;
import epreuve.kfokam48.backend.repository.RelectureRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PUT /api/exercices/{id}/lien — issue #10, règles RG12 / RG16 / Q13.
 * La règle métier « modifiable seulement si EN_ATTENTE » vit dans Exercice.estModifiable(),
 * testée unitairement dans RemplacementLienTest.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RemplacementLienApiTest {

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
    private RelectureRepository relectures;

    /** Un cas = un exercice neuf et l'id de sa session (pour la clôture). */
    private record Cas(Long exerciceId, Long sessionId) {
    }

    private Cas nouvelExercice() {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        List<Etudiant> monde = etudiants.findByPromotionIdOrderByNomAsc(promotion.getId());
        Session session = sessions.creer("Session lien " + UUID.randomUUID().toString().substring(0, 8),
                promotion.getId());
        Exercice exercice = exercices.deposer(session.getId(), monde.get(0).getId(),
                "https://example.com/" + UUID.randomUUID().toString().substring(0, 8));
        return new Cas(exercice.getId(), session.getId());
    }

    @Test
    @DisplayName("200 — le lien se remplace tant qu'aucune note n'est rendue (RG12 / Q13)")
    void remplacementValide() throws Exception {
        Long id = nouvelExercice().exerciceId();

        mockMvc.perform(put("/api/exercices/" + id + "/lien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lien\":\"https://example.com/devoir-corrige\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andExpect(jsonPath("$.misAJourLe").isNotEmpty());
    }

    @Test
    @DisplayName("400 LIEN_INVALIDE — URL malformée ou hors http(s)")
    void lienInvalide() throws Exception {
        Long id = nouvelExercice().exerciceId();

        mockMvc.perform(put("/api/exercices/" + id + "/lien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lien\":\"javascript:alert(1)\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    @DisplayName("404 — l'exercice n'existe pas")
    void exerciceInconnu() throws Exception {
        mockMvc.perform(put("/api/exercices/999999/lien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lien\":\"https://example.com/devoir\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EXERCICE_INCONNU"));
    }

    @Test
    @DisplayName("409 RELECTURE_COMMENCEE — une note est déjà rendue (Q13)")
    void plusModifiableApresRendu() throws Exception {
        Long id = nouvelExercice().exerciceId();
        Long relectureId = relectures.findAllByExerciceIdOrderByIdAsc(id).get(0).getId();

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":16,\"commentaire\":\"Travail propre.\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/exercices/" + id + "/lien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lien\":\"https://example.com/apres-rendu\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_COMMENCEE"));
    }

    @Test
    @DisplayName("409 SESSION_TERMINEE — la session est clôturée (RG16)")
    void sessionCloturee() throws Exception {
        Cas cas = nouvelExercice();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/sessions/" + cas.sessionId() + "/cloturer"))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/exercices/" + cas.exerciceId() + "/lien")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lien\":\"https://example.com/pendant-la-cloture\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_TERMINEE"));
    }
}
