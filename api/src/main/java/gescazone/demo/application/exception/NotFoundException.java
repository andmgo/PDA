package gescazone.demo.application.exception;

/** El recurso solicitado no existe — el ApiExceptionHandler la mapea a 404. */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String mensaje) {
        super(mensaje);
    }
}
