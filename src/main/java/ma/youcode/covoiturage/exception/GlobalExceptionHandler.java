package ma.youcode.covoiturage.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;
import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ProblemDetail creerErreur(
            HttpStatus status,
            String message,
            HttpServletRequest request) {

        ProblemDetail erreur =
                ProblemDetail.forStatusAndDetail(status, message);

        erreur.setTitle(status.getReasonPhrase());
        erreur.setType(
                URI.create("urn:covoiturage:error:" + status.value())
        );
        erreur.setInstance(URI.create(request.getRequestURI()));
        erreur.setProperty("timestamp", Instant.now());
        erreur.setProperty("path", request.getRequestURI());

        return erreur;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {

        log.warn("Ressource introuvable : {}", exception.getMessage());

        return creerErreur(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(
            BadRequestException exception,
            HttpServletRequest request) {

        log.warn("Requête refusée : {}", exception.getMessage());

        return creerErreur(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbidden(
            ForbiddenException exception,
            HttpServletRequest request) {

        log.warn("Action interdite : {}", exception.getMessage());

        return creerErreur(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(
            ConflictException exception,
            HttpServletRequest request) {

        log.warn("Conflit métier : {}", exception.getMessage());

        return creerErreur(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        log.error("Erreur interne", exception);

        return creerErreur(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Une erreur interne est survenue",
                request
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + " : " + error.getDefaultMessage()
                )
                .collect(Collectors.joining("; "));

        log.warn("Validation refusée : {}", message);

        return creerErreur(
                HttpStatus.BAD_REQUEST,
                message,
                request
        );
    }
}