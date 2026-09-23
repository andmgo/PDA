package gescazone.demo.application.exception;

/**
 * El correo no se pudo enviar (SMTP sin configurar, credenciales inválidas,
 * etc.). Extiende IllegalStateException para que el ApiExceptionHandler ya
 * existente la mapee a 400 con el mensaje específico, en vez del 500
 * genérico que oculta la causa.
 */
public class MailEnvioException extends IllegalStateException {
    public MailEnvioException(String mensaje) {
        super(mensaje);
    }
}
