package epreuve.kfokam48.backend.web.dto;

/**
 * Étudiant tel que le voit l'écran de choix de nom — {id, nom}.
 * Q1 : ni compte, ni mot de passe, ni rôle : un id et un nom suffisent.
 */
public record EtudiantResponse(Long id, String nom) {
}
