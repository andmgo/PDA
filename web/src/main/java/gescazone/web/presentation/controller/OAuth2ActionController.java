package gescazone.web.presentation.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Callback de Google OAuth2 tras verificar identidad para una acción interna
 * (hoy: agregar método de pago, ver pagosYCartera.js). Las reservas ya NO
 * pasan por aquí — el usuario del proyecto pidió quitar ese paso, ver
 * ReservaSalonSocialRestController.crearPropia().
 */
@Controller
@RequestMapping("/oauth2")
public class OAuth2ActionController {

    /**
     * "Continuar con Google" desde /login — sin sesión previa, a diferencia
     * de iniciarOAuth2() (que verifica identidad de alguien ya logueado).
     * oauth2SuccessHandler (SecurityConfig) revisa oauth2Proposito para
     * distinguir este flujo del de verificación de acción.
     */
    @GetMapping("/iniciar-login")
    public String iniciarLogin(HttpServletRequest request) {
        request.getSession().setAttribute("oauth2Proposito", "LOGIN");
        return "redirect:/oauth2/authorization/google";
    }

    /** "Registrarte con Google" desde /registro — mismo criterio que iniciarLogin(). */
    @GetMapping("/iniciar-registro")
    public String iniciarRegistro(HttpServletRequest request) {
        request.getSession().setAttribute("oauth2Proposito", "REGISTRO");
        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/iniciar")
    public String iniciarOAuth2(
            HttpServletRequest request,
            @RequestParam String accion,
            @RequestParam(required = false) String datos) {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("usuarioLogueado") == null) {
            return "redirect:/login";
        }

        session.setAttribute("oauth2AccionPendiente", accion);
        if (datos != null && !datos.isBlank()) {
            session.setAttribute("oauth2DatosPendientes", datos);
        }

        org.springframework.security.core.Authentication authActual =
                org.springframework.security.core.context.SecurityContextHolder
                        .getContext().getAuthentication();
        if (authActual != null && authActual.isAuthenticated()) {
            session.setAttribute("authOriginalAntesDeOAuth2", authActual);
        }

        return "redirect:/oauth2/authorization/google";
    }

    @GetMapping("/callback/accion")
    public String procesarCallback(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return "redirect:/login";
        }

        String usuarioLogueado  = (String) session.getAttribute("usuarioLogueado");
        String correoVerificado = (String) session.getAttribute("oauth2CorreoVerificado");

        if (usuarioLogueado == null) {
            limpiarAtributosOAuth2(session);
            return "redirect:/login";
        }

        if (correoVerificado == null || correoVerificado.isBlank()) {
            limpiarAtributosOAuth2(session);
            return "redirect:/pagosYCartera?oauth=error&motivo=no_correo";
        }

        session.removeAttribute("oauth2CorreoVerificado");
        session.removeAttribute("oauth2AccionPendiente");

        return "redirect:/pagosYCartera?oauth=ok";
    }

    @GetMapping("/datos-pendientes")
    @ResponseBody
    public ResponseEntity<String> obtenerDatosPendientes(HttpServletRequest request) {

        HttpSession session = request.getSession(false);
        if (session == null) {
            return ResponseEntity.status(401)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Sin sesión activa\"}");
        }

        String datos = (String) session.getAttribute("oauth2DatosPendientes");
        session.removeAttribute("oauth2DatosPendientes");

        if (datos == null || datos.isBlank()) {
            return ResponseEntity.status(404)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Sin datos pendientes\"}");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(datos);
    }

    private void limpiarAtributosOAuth2(HttpSession session) {
        session.removeAttribute("oauth2CorreoVerificado");
        session.removeAttribute("oauth2AccionPendiente");
        session.removeAttribute("oauth2DatosPendientes");
    }
}
