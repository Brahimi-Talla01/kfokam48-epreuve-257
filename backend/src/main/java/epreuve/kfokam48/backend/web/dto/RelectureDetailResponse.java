package epreuve.kfokam48.backend.web.dto;

/**
 * Relecture vue par le relecteur : le lien de l'exercice à relire.
 * Le nom de l'auteur n'est volontairement pas exposé ici (Q8 / RG7 : le sens interdit
 * c'est l'étudiant relu qui voit qui l'a relu, pas l'inverse).
 */
public record RelectureDetailResponse(Long id, Long exerciceId, Long relecteurId, String statut,
                                      String lien, Integer note, String commentaire) {
}
