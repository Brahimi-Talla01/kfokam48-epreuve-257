package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Presence;
import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.domain.SourcePresence;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** POST /api/relectures/{id} — opération imposée, règles RG4, RG8, RG9 (issues #5 et #6). */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RenduRelectureTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionService sessions;

    @Autowired
    private ExerciceService exercices;

    @Autowired
    private PresenceRepository presences;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private RelectureRepository relectures;

    /** Un cas = relecture fraîchement créée + identifiant de l'auteur de l'exercice. */
    private record Cas(Long relectureId, Long auteurId) {
    }

    private Promotion promotion() {
        return promotions.findAllByOrderByIdAsc().get(0);
    }

    private List<Etudiant> etudiants(int nombre) {
        return etudiants.findByPromotionIdOrderByNomAsc(promotion().getId()).subList(0, nombre);
    }

    /** Session + deux présents + un dépôt → la relecture est affectée au dépôt (RG6). */
    private Cas nouveauCas() {
        List<Etudiant> monde = etudiants(3);
        Session ouverte = sessions.creer("Session rendu " + UUID.randomUUID().toString().substring(0, 8),
                promotion().getId());

        presences.save(new Presence(ouverte, monde.get(1), SourcePresence.ETUDIANT));
        presences.save(new Presence(ouverte, monde.get(2), SourcePresence.ETUDIANT));

        Exercice exercice = exercices.deposer(ouverte.getId(), monde.get(0).getId(),
                "https://example.com/" + UUID.randomUUID().toString().substring(0, 8));
        Relecture relecture = relectures.findAllByExerciceIdOrderByIdAsc(exercice.getId()).get(0);
        return new Cas(relecture.getId(), monde.get(0).getId());
    }

    private String corps(Integer note, Long relecteurId) {
        String champ = relecteurId == null ? "" : ",\"relecteurId\":" + relecteurId;
        return "{\"note\":" + note + ",\"commentaire\":\"Correction claire, bon travail.\"" + champ + "}";
    }

    @Test
    @DisplayName("200 — la note est enregistrée (EF5, RG8) et l'exercice passe à RENDU")
    void renduValide() throws Exception {
        Cas cas = nouveauCas();

        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(15, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(15))
                .andExpect(jsonPath("$.statut").value("RENDUE"));
    }

    @Test
    @DisplayName("409 RELECTURE_DEJA_RENDUE — la note est définitive (RG9 / Q15)")
    void secondRenduRefuse() throws Exception {
        Cas cas = nouveauCas();
        String corps = corps(12, null);

        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON).content(corps))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    @DisplayName("403 AUTO_RELECTURE — on ne relit jamais son propre exercice (RG4 / Q5)")
    void autoRelectureRefusee() throws Exception {
        Cas cas = nouveauCas();

        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(18, cas.auteurId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
    }

    @Test
    @DisplayName("400 NOTE_INVALIDE — note hors 0-20 (RG8 / Q9)")
    void noteHorsBornesRefusee() throws Exception {
        Cas cas = nouveauCas();

        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON).content(corps(21, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));

        mockMvc.perform(post("/api/relectures/" + cas.relectureId())
                        .contentType(MediaType.APPLICATION_JSON).content(corps(-1, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    @DisplayName("400 RELECTURE_INCONNUE — la relecture n'existe pas (le contrat n'autorise pas de 404 ici)")
    void relectureInconnue() throws Exception {
        mockMvc.perform(post("/api/relectures/999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corps(15, null)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("RELECTURE_INCONNUE"));
    }
}
