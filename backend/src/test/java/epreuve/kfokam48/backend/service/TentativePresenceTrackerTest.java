package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.web.ApiException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Issue #15 — RG3 / Q4 : après 5 codes erronés, l'étudiant est bloqué 2 minutes.
 * Décision cahier §7.2 : {@code 400 TROP_DE_TENTATIVES}, jamais un nouveau code de
 * statut sur l'opération imposée (B2).
 */
class TentativePresenceTrackerTest {

    private static final Long ETUDIANT = 42L;
    private static final ZoneId ZONE = ZoneId.of("UTC");

    private Clock horlogeA(Instant instant) {
        return Clock.fixed(instant, ZONE);
    }

    @Test
    void quatreEchecsNeBloquentPas() {
        Instant maintenant = Instant.parse("2026-01-01T10:00:00Z");
        TentativePresenceTracker tracker = new TentativePresenceTracker(horlogeA(maintenant));

        for (int i = 0; i < 4; i++) {
            tracker.enregistrerEchec(ETUDIANT);
        }

        assertDoesNotThrow(() -> tracker.verifierNonBloque(ETUDIANT));
    }

    @Test
    void cinquiemeEchecBloqueDeuxMinutes() {
        Instant maintenant = Instant.parse("2026-01-01T10:00:00Z");
        TentativePresenceTracker tracker = new TentativePresenceTracker(horlogeA(maintenant));

        for (int i = 0; i < 5; i++) {
            tracker.enregistrerEchec(ETUDIANT);
        }

        ApiException erreur = assertThrows(ApiException.class, () -> tracker.verifierNonBloque(ETUDIANT));
        assertEquals("TROP_DE_TENTATIVES", erreur.getCode());
        assertEquals(400, erreur.getStatus().value());
    }

    @Test
    void blocageLeveApresDeuxMinutes() {
        Instant depart = Instant.parse("2026-01-01T10:00:00Z");
        TentativePresenceTracker tracker = new TentativePresenceTracker(horlogeA(depart));
        for (int i = 0; i < 5; i++) {
            tracker.enregistrerEchec(ETUDIANT);
        }
        assertThrows(ApiException.class, () -> tracker.verifierNonBloque(ETUDIANT));

        // Même instance, même état ; seule l'horloge avance de 2 min 1 s (paquet-privée).
        tracker.horloge = horlogeA(depart.plusSeconds(121));
        assertDoesNotThrow(() -> tracker.verifierNonBloque(ETUDIANT));
    }

    @Test
    void succesReinitialiseLeCompteur() {
        Instant maintenant = Instant.parse("2026-01-01T10:00:00Z");
        TentativePresenceTracker tracker = new TentativePresenceTracker(horlogeA(maintenant));

        for (int i = 0; i < 4; i++) {
            tracker.enregistrerEchec(ETUDIANT);
        }
        tracker.reinitialiser(ETUDIANT);
        for (int i = 0; i < 4; i++) {
            tracker.enregistrerEchec(ETUDIANT);
        }

        // 4 + 4 échecs séparés par une réinitialisation : jamais 5 consécutifs, donc pas de blocage.
        assertDoesNotThrow(() -> tracker.verifierNonBloque(ETUDIANT));
    }
}
