package epreuve.kfokam48.backend.web.dto;

/**
 * Ligne du tableau du formateur (Q16 / RG15).
 * {@code moyenne} vient DÉJÀ arrondie de la base — le client ne la recalcule jamais (RG17).
 * {@code relecturesEnAttente} = exercices de l'étudiant encore non relus (RG10 / Q11).
 */
public record TableauLigneResponse(Long etudiantId, String nom,
                                   Long presences, Long exercicesDeposes,
                                   Double moyenne, Long relecturesEnAttente) {
}
