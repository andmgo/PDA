package gescazone.demo.infrastructure.mail;

import gescazone.demo.application.exception.MailEnvioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Envío de correo por SMTP (ver spring.mail.* en application.properties).
 * Hoy el único uso es avisar la contraseña nueva cuando un administrador
 * resetea la de otro usuario.
 */
@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * @throws MailEnvioException si el correo no se pudo enviar (SMTP sin
     *         configurar, credenciales inválidas, etc.) — el llamador decide
     *         cómo mostrarlo (ver UsuarioService.resetearContrasena).
     */
    public void enviarContrasenaReseteada(String correoDestino, String nombre, String contrasenaNueva) {
        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(correoDestino);
        mensaje.setSubject("Aurum Condominios — Tu contraseña fue restablecida");
        mensaje.setText(
                "Hola " + nombre + ",\n\n" +
                "Un administrador restableció tu contraseña de Aurum Condominios.\n\n" +
                "Tu contraseña nueva es: " + contrasenaNueva + "\n\n" +
                "Te recomendamos iniciar sesión y cambiarla por una que solo vos conozcas " +
                "(Perfil -> Seguridad).\n\n" +
                "Si no esperabas este correo, contactá a la administración del conjunto."
        );
        try {
            mailSender.send(mensaje);
        } catch (MailException e) {
            log.error("No se pudo enviar el correo de reseteo de contraseña a {}", correoDestino, e);
            throw new MailEnvioException("No se pudo enviar el correo con la contraseña nueva. Verifica la configuración de correo del servidor.");
        }
    }
}
