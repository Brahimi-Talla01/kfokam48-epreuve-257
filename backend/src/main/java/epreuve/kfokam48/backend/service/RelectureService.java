package epreuve.kfokam48.backend.service;

import epreuve.kfokam48.backend.domain.Etudiant;
import epreuve.kfokam48.backend.domain.Exercice;
import epreuve.kfokam48.backend.domain.Relecture;
import epreuve.kfokam48.backend.domain.StatutRelecture;
import epreuve.kfokam48.backend.repository.EtudiantRepository;
import epreuve.kfokam48.backend.repository.ExerciceRepository;
import epreuve.kfokam48.backend.repository.PresenceRepository;
import epreuve.kfokam48.backend.repository.RelectureRepository;
import epreuve.kfokam48.backend.web.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Affectation des relectures.
 * RG6 / Q7 : le relecteur est tiré au hasard parmi les étudiants PRÉSENTS à la session,
 * auteur de l'exercice exclu. RG5 : un seul relecteur par exercice.
 * Si aucun pair n'était présent → relecteur null, relecture EN_ATTENTE (cahier §7.2).
 */
@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final PresenceRepository presences;
    private final EtudiantRepository etudiants;
    private final ExerciceRepository exercices;

    public RelectureService(RelectureRepository relectures, PresenceRepository presences,
                            EtudiantRepository etudiants, ExerciceRepository exercices) {
        this.relectures = relectures;
        this.presences = presences;
        this.etudiants = etudiants;
        this.exercices = exercices;
    }

    /** Création / affectation explicite — POST /api/relectures. */
    @Transactional
    public Relecture creer(Long exerciceId) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> ApiException.badRequest("EXERCICE_INCONNU",
                        "L'exercice demandé n'existe pas."));
        if (relectures.existsByExerciceId(exerciceId)) {
            throw ApiException.conflict("RELECTURE_DEJA_CREE",
                    "Cet exercice a déjà un relecteur assigné.");
        }
        return affecter(exercice);
    }

    /** Appelé au dépôt de l'exercice (issue #3) : jamais deux relecteurs (RG5). */
    @Transactional
    public Relecture affecter(Exercice exercice) {
        if (relectures.existsByExerciceId(exercice.getId())) {
            return relectures.findByExerciceId(exercice.getId()).orElseThrow();
        }

        List<Long> candidats = presences.idsPresentsSaufAuteur(
                exercice.getSession().getId(), exercice.getEtudiant().getId());

        Etudiant relecteur = null;
        if (!candidats.isEmpty()) {
            Long idTire = candidats.get(ThreadLocalRandom.current().nextInt(candidats.size()));
            relecteur = etudiants.findById(idTire).orElse(null);
        }
        return relectures.save(new Relecture(exercice, relecteur));
    }

    @Transactional(readOnly = true)
    public List<Relecture> lister(Long relecteurId, StatutRelecture statut) {
        Etudiant relecteur = etudiants.findById(relecteurId)
                .orElseThrow(() -> ApiException.notFound("ETUDIANT_INCONNU",
                        "L'étudiant demandé n'existe pas."));
        if (statut == null) {
            return relectures.findByRelecteurIdOrderByIdDesc(relecteur.getId());
        }
        return relectures.findByRelecteurIdAndStatutOrderByIdDesc(relecteur.getId(), statut);
    }

    /**
     * Notes et commentaires reçus sur un exercice — GET /api/exercices/{id}/relectures.
     * RG7 / Q8 : l'identité du relecteur n'est jamais exposée à l'auteur de l'exercice.
     */
    @Transactional(readOnly = true)
    public List<Relecture> listerPourExercice(Long exerciceId) {
        if (exerciceId == null || !exercices.existsById(exerciceId)) {
            throw ApiException.notFound("EXERCICE_INCONNU", "L'exercice demandé n'existe pas.");
        }
        return relectures.findAllByExerciceIdOrderByIdAsc(exerciceId);
    }

    @Transactional(readOnly = true)
    public Relecture trouver(Long id) {
        return relectures.findById(id)
                .orElseThrow(() -> ApiException.badRequest("RELECTURE_INCONNUE",
                        "La relecture demandée n'existe pas."));
    }

    /**
     * Rendu d'une note — POST /api/relectures/{id} (opération imposée).
     * Ordre des contrôles : 400 NOTE_INVALIDE → 403 AUTO_RELECTURE / relecteur étranger →
     * 409 RELECTURE_DEJA_RENDUE → 200.
     * RG8 : note entière entre 0 et 20. RG9 / Q15 : définitive une fois rendue.
     */
    @Transactional
    public Relecture rendre(Long id, Integer note, String commentaire, Long relecteurId) {
        Relecture relecture = trouver(id);

        if (note == null || note < 0 || note > 20) {
            throw ApiException.badRequest("NOTE_INVALIDE", "La note doit être un entier entre 0 et 20.");
        }

        Long auteurExercice = relecture.getExercice().getEtudiant().getId();
        if (relecteurId != null && relecteurId.equals(auteurExercice)) {
            throw ApiException.forbidden("AUTO_RELECTURE",
                    "Vous ne pouvez pas relire votre propre exercice.");
        }

        Long relecteurAffecte = relecture.getRelecteur() == null ? null : relecture.getRelecteur().getId();
        if (relecteurId != null && relecteurAffecte != null && !relecteurAffecte.equals(relecteurId)) {
            throw ApiException.forbidden("RELECTURE_ETRANGERE",
                    "Cette relecture est affectée à un autre relecteur.");
        }

        if (relecture.estRendue()) {
            throw ApiException.conflict("RELECTURE_DEJA_RENDUE",
                    "Cette note est déjà enregistrée : elle est définitive.");
        }

        relecture.rendre(note, commentaire);
        Exercice exercice = relecture.getExercice();
        exercice.marquerRendu();
        exercices.save(exercice);
        return relectures.save(relecture);
    }
}
