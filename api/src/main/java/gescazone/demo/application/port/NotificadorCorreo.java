package gescazone.demo.application.port;

/**
 * Puerto de salida para avisos por correo. La capa de aplicación define QUÉ
 * necesita avisar; la infraestructura decide CÓMO (hoy SMTP con
 * infrastructure.mail.MailService). Antes UsuarioService importaba MailService
 * directamente, una dependencia de aplicación hacia infraestructura.
 */
public interface NotificadorCorreo {

    /**
     * @throws gescazone.demo.application.exception.MailEnvioException si el
     *         correo no se pudo enviar (SMTP sin configurar, credenciales…).
     */
    void enviarContrasenaReseteada(String correoDestino, String nombre, String contrasenaNueva);

    /**
     * Aviso de que llegó un paquete a portería (Fase 7). Es de cortesía: nunca
     * lanza error ni hace esperar a portería; si el SMTP no está configurado
     * simplemente no se envía.
     */
    void avisarPaqueteLlegado(String correoDestino, String nombre, String numeroApartamento, String descripcion);
}
