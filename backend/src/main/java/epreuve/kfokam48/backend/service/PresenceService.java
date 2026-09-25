package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Presence;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.domain.SourcePresence;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
import epreuve.kfokam48.backend.repository.SessionRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Marquage d'une présence. Ordre des contrôles, identique à D3 et au contrat :
 * 400 code inconnu → 410 expiré (RG1) → 410 session terminée (RG2/RG16) →
 * 409 déjà présent (RG14) → 201.
 */
@Service
public class PresenceService {

    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;

    public PresenceService(SessionRepository sessions, EtudiantRepository etudiants,
                           PresenceRepository presences) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
    }

    @Transactional
    public Presence marquer(String code, Long etudiantId, SourcePresence source) {
        LocalDateTime maintenant = LocalDateTime.now();

        Session session = sessions.findByCode(code)
                .orElseThrow(() -> ApiException.badRequest("CODE_INCONNU",
                        "Le code de présence est inconnu. Vérifiez le code affiché au tableau."));

        if (session.estExpire(maintenant)) {
            throw ApiException.gone("CODE_EXPIRE",
                    "Le code de présence a expiré. Demandez un nouveau code au formateur.");
        }

        if (!session.estOuverte()) {
            throw ApiException.gone("SESSION_TERMINEE",
                    "La session est terminée : il n'est plus possible de pointer sa présence.");
        }

        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiantId)) {
            throw ApiException.conflict("DEJA_PRESENT",
                    "Votre présence est déjà enregistrée pour cette session.");
        }

        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> ApiException.badRequest("ETUDIANT_INCONNU",
                        "L'étudiant demandé n'existe pas."));

        return presences.save(new Presence(session, etudiant, source));
    }
}
