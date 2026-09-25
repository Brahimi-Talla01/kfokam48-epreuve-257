package epreuve.kfokam48.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Le contexte démarre sur H2 avec les migrations Flyway : c'est le premier contrôle
 * que le schéma V1 correspond bien aux entités (B5) et que V2 charge sans erreur.
 */
@SpringBootTest
@ActiveProfiles("test")
class BackendApplicationTests {

    @Test
    void contexteDeMarqueEtMigrationsAppliquees() {
    }
}
