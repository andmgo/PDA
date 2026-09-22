package gescazone.demo.application.exception;

/**
 * Falla de validación o regla de negocio. Extiende IllegalArgumentException
 * a propósito: el ApiExceptionHandler solo necesita un handler para
 * IllegalArgumentException para cubrir ambas, y el código/tests existentes
 * que ya esperan IllegalArgumentException no se rompen.
 */
public class ValidationException extends IllegalArgumentException {
    public ValidationException(String mensaje) {
        super(mensaje);
    }
}
