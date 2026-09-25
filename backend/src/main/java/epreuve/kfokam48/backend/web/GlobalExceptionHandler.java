package epreuve.kfokam48.backend.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Toutes les erreurs convergent vers {code, message} (B4).
 * La stack trace est journalisée côté serveur, jamais renvoyée au client.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorBody> apiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus()).body(new ApiErrorBody(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorBody> champsInvalides(MethodArgumentNotValidException ex) {
        FieldError erreur = ex.getBindingResult().getFieldError();
        String message = erreur == null
                ? "Les données envoyées sont invalides."
                : erreur.getDefaultMessage();
        return ResponseEntity.badRequest().body(new ApiErrorBody("VALIDATION", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorBody> corpsIllisible(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody("CORPS_INVALIDE", "Le corps de la requête est illisible ou incomplet."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorBody> parametreInvalide(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ApiErrorBody("PARAMETRE_INVALIDE",
                        "Le paramètre « " + ex.getName() + " » est invalide."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorBody> erreurInterne(Exception ex) {
        log.error("Erreur interne non gérée", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorBody("ERREUR_INTERNE", "Une erreur interne est survenue. Réessayez plus tard."));
    }
}
