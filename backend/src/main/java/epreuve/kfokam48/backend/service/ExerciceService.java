package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Session;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.ExerciceRepository;
import epreuve.kfokam48.backend.repository.SessionRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Dépôt et remplacement du lien d'exercice (Q13 : un lien, jamais un fichier).
 * RG11 / Q12 : le dépôt reste ouvert jusqu'à la CLÔTURE de la session — distincte
 * de l'expiration du code (RG1).
 */
@Service
public class ExerciceService {

    private final ExerciceRepository exercices;
    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final RelectureService relectures;

    public ExerciceService(ExerciceRepository exercices, SessionRepository sessions,
                           EtudiantRepository etudiants, RelectureService relectures) {
        this.exercices = exercices;
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.relectures = relectures;
    }

    @Transactional
    public Exercice deposer(Long sessionId, Long etudiantId, String lien) {
        String lienValide = validerLien(lien);

        Session session = sessions.findById(sessionId)
                .orElseThrow(() -> ApiException.badRequest("SESSION_INCONNUE",
                        "La session demandée n'existe pas."));

        Etudiant etudiant = etudiants.findById(etudiantId)
                .orElseThrow(() -> ApiException.badRequest("ETUDIANT_INCONNU",
                        "L'étudiant demandé n'existe pas."));

        if (!session.estOuverte()) {
            throw ApiException.conflict("SESSION_TERMINEE",
                    "La session est clôturée : le dépôt d'exercice est fermé.");
        }

        if (exercices.existsBySessionIdAndEtudiantId(sessionId, etudiantId)) {
            throw ApiException.conflict("EXERCICE_DEJA_DEPOSE",
                    "Vous avez déjà déposé un exercice pour cette session. Remplacez le lien plutôt que d'en déposer un autre.");
        }

        Exercice sauvegarde = exercices.save(new Exercice(session, etudiant, lienValide));
        relectures.affecter(sauvegarde);
        return sauvegarde;
    }

    @Transactional
    public Exercice remplacerLien(Long exerciceId, String lien) {
        String lienValide = validerLien(lien);
        Exercice exercice = trouver(exerciceId);

        if (!exercice.getSession().estOuverte()) {
            throw ApiException.conflict("SESSION_TERMINEE",
                    "La session est clôturée : le lien n'est plus modifiable.");
        }

        if (!exercice.estModifiable()) {
            throw ApiException.conflict("RELECTURE_COMMENCEE",
                    "L'exercice a déjà été relu : le lien n'est plus modifiable.");
        }

        exercice.remplacerLien(lienValide);
        return exercices.save(exercice);
    }

    @Transactional(readOnly = true)
    public Exercice trouver(Long id) {
        return exercices.findById(id)
                .orElseThrow(() -> ApiException.notFound("EXERCICE_INCONNU",
                        "L'exercice demandé n'existe pas."));
    }

    private String validerLien(String lien) {
        try {
            URI uri = new URI(lien.trim());
            boolean estHttp = "http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme());
            if (!estHttp || uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException("scheme");
            }
            return uri.toString();
        } catch (URISyntaxException | IllegalArgumentException e) {
            throw ApiException.badRequest("LIEN_INVALIDE",
                    "Le lien doit être une URL valide commençant par http:// ou https://");
        }
    }
}
