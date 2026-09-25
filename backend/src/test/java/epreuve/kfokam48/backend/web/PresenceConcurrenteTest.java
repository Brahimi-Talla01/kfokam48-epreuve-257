package epreuve.kfokam48.backend.web;

import epreuve.kfokam48.backend.domain.Promotion;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
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
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Issue #31 (ENVELOPPE etape 3) : deux POST /api/presences quasi simultanes sur la meme
 * presence ne doivent jamais produire de 500 — au plus une ligne en base, une reponse 201,
 * l'autre 409 DEJA_PRESENT. Avant correctif, la verification d'existence puis l'ecriture
 * n'etaient pas atomiques : sous course, la seconde ecriture viole la contrainte UNIQUE en
 * base et l'exception n'etait pas mappee, ce qui remontait en 500 (perte apparente pour
 * l'utilisateur).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PresenceConcurrenteTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SessionRepository sessions;

    @Autowired
    private PromotionRepository promotions;

    @Autowired
    private EtudiantRepository etudiants;

    @Autowired
    private PresenceRepository presences;

    @Test
    @DisplayName("Deux POST /api/presences concurrents sur la meme presence : jamais de 500, jamais deux 201 (issue #31)")
    void deuxRequetesConcurrentesNeProduisentJamaisDeuxCent() throws Exception {
        Promotion promotion = promotions.findAllByOrderByIdAsc().get(0);
        Long etudiantId = etudiants.findByPromotionIdOrderByNomAsc(promotion.getId()).get(0).getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            for (int iteration = 0; iteration < 15; iteration++) {
                LocalDateTime ouverture = LocalDateTime.now();
                Session session = sessions.save(new Session("Session concurrence " + iteration, promotion,
                        codeUnique(), ouverture, ouverture.plusMinutes(15)));
                String corps = "{\"code\":\"" + session.getCode() + "\",\"etudiantId\":" + etudiantId + "}";

                CyclicBarrier depart = new CyclicBarrier(2);
                Callable<Integer> requete = () -> {
                    depart.await();
                    return mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                    .post("/api/presences")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(corps))
                            .andReturn().getResponse().getStatus();
                };

                Future<Integer> f1 = executor.submit(requete);
                Future<Integer> f2 = executor.submit(requete);
                int statut1 = f1.get();
                int statut2 = f2.get();

                List<Integer> statuts = List.of(statut1, statut2);
                assertThat(statuts)
                        .as("iteration %d : statuts obtenus %s (jamais de 500)", iteration, statuts)
                        .doesNotContain(500);
                assertThat(statuts).as("iteration %d : exactement un succes et un conflit", iteration)
                        .containsExactlyInAnyOrder(201, 409);
                assertThat(presences.countBySessionIdAndEtudiantId(session.getId(), etudiantId))
                        .as("iteration %d : une seule presence enregistree en base", iteration)
                        .isEqualTo(1L);
            }
        } finally {
            executor.shutdown();
        }
    }

    private String codeUnique() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }
}
