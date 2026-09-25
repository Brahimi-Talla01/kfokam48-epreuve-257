package epreuve.kfokam48.backend.domain;

/**
 * Cycle de vie de la session (RG16 — trou principal du cahier des charges §7.2).
 * L'expiration du code (RG1) et la clôture (RG16) restent deux événements distincts.
 */
public enum StatutSession {
    OUVERTE,
    CLOTUREE
}
