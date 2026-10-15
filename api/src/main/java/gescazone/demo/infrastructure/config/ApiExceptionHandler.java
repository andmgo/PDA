package gescazone.demo.infrastructure.config;

import gescazone.demo.application.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores centralizado para /api/**. Reemplaza los bloques
 * catch(IllegalArgumentException)/catch(Exception) repetidos en cada
 * controller REST (hallazgo #5 de la auditoría). Devuelve texto plano con
 * el mensaje, igual que hacían los controllers antes — así el JS del Web
 * (que lee el error con response.text()) no necesita cambios.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> handleNotFound(NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleValidation(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    /**
     * Una validación lanzada DENTRO de un @Repository (p. ej. CatalogoResolver
     * rechazando un rol que no existe) llega envuelta: Spring traduce las
     * excepciones de los repositorios a InvalidDataAccessApiUsageException.
     * Si por dentro es una validación, se responde 400 con su mensaje.
     */
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<String> handleValidationEnRepositorio(InvalidDataAccessApiUsageException e) {
        if (e.getCause() instanceof IllegalArgumentException causa) {
            return handleValidation(causa);
        }
        return handleGeneral(e);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneral(Exception e) {
        log.error("Error inesperado en el API", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ha ocurrido un error inesperado");
    }
}
