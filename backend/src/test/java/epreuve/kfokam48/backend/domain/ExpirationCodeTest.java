package epreuve.kfokam48.backend.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test unitaire sur une règle réelle du cahier des charges — B6.
 * Règle : RG1 / Q2 — « le code expire 15 minutes après l'ouverture »,
 * et l'expiration n'est PAS la clôture (RG16, distinction explicitée au §7.2).
 */
class ExpirationCodeTest {

    private static final LocalDateTime OUVERTURE = LocalDateTime.of(2026, 9, 25, 8, 0);

    private Session sessionCodeValide() {
        return new Session("Cours", new Promotion("Promotion test"), "K7F2M48",
                OUVERTURE, OUVERTURE.plusMinutes(15));
    }

    @Test
    @DisplayName("Le code est valable pendant 14 minutes et 59 secondes (RG1)")
    void codeValidePendantQuinzeMinutes() {
        Session session = sessionCodeValide();
        assertFalse(session.estExpire(OUVERTURE), "le code est valable dès l'ouverture");
        assertFalse(session.estExpire(OUVERTURE.plusMinutes(14)), "le code est valable à 14 min");
        assertFalse(session.estExpire(OUVERTURE.plusMinutes(14).plusSeconds(59)),
                "le code est valable jusqu'à 14 min 59 s");
    }

    @Test
    @DisplayName("Le code expire dès la 16e minute (RG1 / Q2)")
    void codeExpireApresQuinzeMinutes() {
        Session session = sessionCodeValide();
        assertTrue(session.estExpire(OUVERTURE.plusMinutes(15).plusSeconds(1)),
                "le code est expiré à 15 min 1 s");
        assertTrue(session.estExpire(OUVERTURE.plusHours(3)),
                "le code reste expiré ensuite");
    }

    @Test
    @DisplayName("Expiration du code et clôture de la session sont deux événements distincts (RG1 vs RG16)")
    void expirationNeCloturePasLaSession() {
        Session session = sessionCodeValide();

        assertTrue(session.estOuverte(), "la session est encore ouverte après expiration du code");
        assertFalse(session.getClotureAt() != null, "aucune clôture n'a eu lieu");

        session.cloturer(OUVERTURE.plusMinutes(45));
        assertFalse(session.estOuverte(), "le formateur a clôturé la session");
        assertTrue(session.estExpire(OUVERTURE.plusMinutes(46)),
                "le code était déjà expiré avant la clôture");
    }
}
