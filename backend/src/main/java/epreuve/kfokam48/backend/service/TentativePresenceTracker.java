package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RG3 / Q4 : après 5 codes erronés, l'étudiant est bloqué 2 minutes.
 * Décision du cahier des charges §7.2 : réponse {@code 400 TROP_DE_TENTATIVES} plutôt
 * que d'ajouter un code de statut (ex. 429) à l'opération imposée {@code POST
 * /api/presences} (B2).
 * <p>
 * Suivi en mémoire, par étudiant : cette protection anti-abus n'a pas besoin de
 * survivre à un redémarrage ni d'être partagée entre plusieurs instances pour cette
 * application (une seule instance, `docker compose up`) — inutile d'ajouter une table
 * et une migration pour un compteur volatile par nature.
 */
@Component
public class TentativePresenceTracker {

    public static final int MAX_TENTATIVES = 5;
    public static final Duration DUREE_BLOCAGE = Duration.ofMinutes(2);

    /** Paquet-privée, non finale : un test du même paquet avance le temps sans horloge réelle. */
    Clock horloge;
    private final ConcurrentHashMap<Long, Etat> etats = new ConcurrentHashMap<>();

    public TentativePresenceTracker() {
        this(Clock.systemDefaultZone());
    }

    TentativePresenceTracker(Clock horloge) {
        this.horloge = horloge;
    }

    /** À appeler avant tout traitement de {@code POST /api/presences}. */
    public void verifierNonBloque(Long etudiantId) {
        Etat etat = etats.get(etudiantId);
        if (etat != null && etat.bloqueJusqua() != null
                && LocalDateTime.now(horloge).isBefore(etat.bloqueJusqua())) {
            throw ApiException.badRequest("TROP_DE_TENTATIVES",
                    "Trop de tentatives échouées. Réessayez dans 2 minutes.");
        }
    }

    /** Un code inconnu vient d'être saisi (RG3) : incrémente, bloque au 5e échec. */
    public void enregistrerEchec(Long etudiantId) {
        etats.compute(etudiantId, (id, etat) -> {
            int echecs = (etat == null ? 0 : etat.echecs()) + 1;
            LocalDateTime bloqueJusqua = echecs >= MAX_TENTATIVES
                    ? LocalDateTime.now(horloge).plus(DUREE_BLOCAGE)
                    : null;
            return new Etat(echecs, bloqueJusqua);
        });
    }

    /** Présence enregistrée avec succès : on oublie les échecs précédents. */
    public void reinitialiser(Long etudiantId) {
        etats.remove(etudiantId);
    }

    private record Etat(int echecs, LocalDateTime bloqueJusqua) {
    }
}
