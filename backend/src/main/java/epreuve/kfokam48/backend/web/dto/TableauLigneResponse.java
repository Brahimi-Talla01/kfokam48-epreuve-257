package epreuve.kfokam48.backend.web.dto;

/**
 * Ligne du tableau du formateur (Q16 / RG15).
 * {@code moyenne} vient DÉJÀ arrondie de la base — le client ne la recalcule jamais (RG17).
 * {@code relecturesEnAttente} = exercices de l'étudiant encore non relus (RG10 / Q11).
 *
 * {@code presencesFormateur} est une propriété <b>facultative ajoutée</b> par le candidat
 * pour satisfaire RG13 / Q14 : les six champs imposés par le contrat restent inchangés,
 * mêmes noms et mêmes types.
 */
public record TableauLigneResponse(Long etudiantId, String nom,
                                   Long presences, Long presencesFormateur,
                                   Long exercicesDeposes,
                                   Double moyenne, Long relecturesEnAttente) {
}
