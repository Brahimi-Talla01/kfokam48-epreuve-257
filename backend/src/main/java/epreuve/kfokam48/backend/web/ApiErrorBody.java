package epreuve.kfokam48.backend.web;

/**
 * Format d'erreur imposé pour TOUTES les réponses en erreur, sans exception (B4).
 * Une stack trace, un corps vide ou la page par défaut de Spring valent zéro.
 */
public record ApiErrorBody(String code, String message) {
}
