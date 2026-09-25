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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Affectation des relectures.
 * RG6 / Q7 : les relecteurs sont tirés au hasard parmi les étudiants PRÉSENTS à la
 * session, auteur de l'exercice exclu.
 * RG18 (étape 3, ex-RG5) : **deux** relecteurs distincts par exercice — un seul si un
 * seul pair est éligible, aucun si personne n'était présent (cahier §7.2/§7.3).
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

    /** Nombre de relecteurs visés par exercice depuis l'étape 3 (RG18, ex-RG5 = 1). */
    private static final int RELECTEURS_PAR_EXERCICE = 2;

    /** Création / affectation explicite — POST /api/relectures. */
    @Transactional
    public List<Relecture> creer(Long exerciceId) {
        Exercice exercice = exercices.findById(exerciceId)
                .orElseThrow(() -> ApiException.badRequest("EXERCICE_INCONNU",
                        "L'exercice demandé n'existe pas."));
        if (relectures.existsByExerciceId(exerciceId)) {
            throw ApiException.conflict("RELECTURE_DEJA_CREE",
                    "Cet exercice a déjà ses relecteurs assignés.");
        }
        return affecter(exercice);
    }

    /**
     * Appelé au dépôt de l'exercice (issue #3). RG18 (étape 3) : jusqu'à deux
     * relecteurs distincts tirés parmi les présents, auteur exclu ; un seul si un
     * seul pair est éligible ; aucun si personne (relecture placeholder EN_ATTENTE,
     * cahier §7.2).
     */
    @Transactional
    public List<Relecture> affecter(Exercice exercice) {
        if (relectures.existsByExerciceId(exercice.getId())) {
            return relectures.findAllByExerciceIdOrderByIdAsc(exercice.getId());
        }

        List<Long> candidats = new ArrayList<>(presences.idsPresentsSaufAuteur(
                exercice.getSession().getId(), exercice.getEtudiant().getId()));
        Collections.shuffle(candidats, ThreadLocalRandom.current());

        if (candidats.isEmpty()) {
            return List.of(relectures.save(new Relecture(exercice, null)));
        }

        List<Long> retenus = candidats.subList(0, Math.min(RELECTEURS_PAR_EXERCICE, candidats.size()));
        List<Relecture> creees = new ArrayList<>();
        for (Long idTire : retenus) {
            Etudiant relecteur = etudiants.findById(idTire).orElse(null);
            creees.add(relectures.save(new Relecture(exercice, relecteur)));
        }
        return creees;
    }

    /**
     * Note retenue pour un exercice — RG19 (étape 3) : moyenne des deux si les deux
     * sont rendues (définitive), la note seule si une seule est rendue (provisoire),
     * rien si aucune n'est rendue.
     */
    @Transactional(readOnly = true)
    public NoteAgregee noteAgregee(Long exerciceId) {
        List<Relecture> toutes = listerPourExercice(exerciceId);
        List<Relecture> rendues = toutes.stream().filter(Relecture::estRendue).toList();

        if (rendues.isEmpty()) {
            return new NoteAgregee(null, false, toutes);
        }
        if (rendues.size() == toutes.size()) {
            double moyenne = rendues.stream().mapToInt(Relecture::getNote).average().orElseThrow();
            return new NoteAgregee(Math.round(moyenne * 100.0) / 100.0, false, toutes);
        }
        return new NoteAgregee(rendues.get(0).getNote().doubleValue(), true, toutes);
    }

    /** Note retenue d'un exercice : {@code null} tant qu'aucune relecture n'est rendue. */
    public record NoteAgregee(Double noteRetenue, boolean provisoire, List<Relecture> relectures) {
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
