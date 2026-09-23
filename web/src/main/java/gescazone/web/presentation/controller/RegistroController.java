package gescazone.web.presentation.controller;

import gescazone.web.dto.UsuarioRegistroForm;
import gescazone.web.infrastructure.client.AuthApiClient;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Autoregistro público — ya no crea el Usuario directo: llama a
 * POST /api/solicitudes-registro en gescazone-api, que guarda una solicitud
 * PENDIENTE. Un administrador la aprueba (asignando el rol) o la rechaza
 * desde el tab "Registros" de Gestión de Datos.
 */
@Controller
public class RegistroController {

    @Autowired
    private AuthApiClient authApiClient;

    private static final String[] TIPOS_DOCUMENTO = {
        "Cédula de Ciudadanía",
        "Cédula de Extranjería",
        "Pasaporte",
        "Tarjeta de Identidad"
    };

    @GetMapping("/registro")
    public String mostrarFormulario(@RequestParam(required = false) String error, Model model) {
        model.addAttribute("usuario", new UsuarioRegistroForm());
        model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
        if ("google".equals(error)) {
            model.addAttribute("errorDocumento", "No se pudo verificar tu cuenta de Google. Intenta de nuevo.");
        }
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            @Valid @ModelAttribute("usuario") UsuarioRegistroForm usuario,
            BindingResult result,
            @RequestParam(required = false) String nombreTipoDocumento,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
            return "registro";
        }

        if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty()) {
            model.addAttribute("errorDocumento", "Debe seleccionar un tipo de documento.");
            model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
            return "registro";
        }

        try {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("numeroDocumento", usuario.getNumeroDocumento().trim());
            datos.put("nombre", usuario.getNombre().trim());
            datos.put("apellido", usuario.getApellido().trim());
            datos.put("correo", usuario.getCorreo().trim());
            datos.put("contrasena", usuario.getContrasena());
            datos.put("nombreTipoDocumento", nombreTipoDocumento.trim());

            authApiClient.registro(datos);

            redirectAttributes.addFlashAttribute("registroExitoso",
                "Tu solicitud fue enviada. Un administrador la revisará y podrás iniciar sesión cuando sea aprobada.");
            return "redirect:/login";

        } catch (HttpClientErrorException.BadRequest e) {
            model.addAttribute("errorDocumento", e.getResponseBodyAsString());
            model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
            return "registro";
        } catch (Exception e) {
            model.addAttribute("errorDocumento", "Error al registrar. Intente más tarde.");
            model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
            return "registro";
        }
    }

    /**
     * Paso final de "Registrarte con Google" (y de "Continuar con Google"
     * cuando el correo no corresponde a ningún Usuario todavía) — Google
     * verificó el correo (guardado en sesión por
     * SecurityConfig.oauth2SuccessHandler); nombre/apellido llegan
     * pre-rellenados como sugerencia pero son editables (Google no siempre
     * entrega el apellido), y solo falta el documento, que Google no
     * entrega. Termina en la misma cola de aprobación de siempre.
     */
    @GetMapping("/registro/completar-google")
    public String mostrarCompletarGoogle(HttpSession session, Model model) {
        String correo = (String) session.getAttribute("googleCorreo");
        if (correo == null) {
            return "redirect:/registro";
        }
        model.addAttribute("nombre", session.getAttribute("googleNombre"));
        model.addAttribute("apellido", session.getAttribute("googleApellido"));
        model.addAttribute("correo", correo);
        model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);
        return "registroCompletarGoogle";
    }

    @PostMapping("/registro/completar-google")
    public String procesarCompletarGoogle(
            HttpSession session,
            @RequestParam String nombre,
            @RequestParam String apellido,
            @RequestParam String numeroDocumento,
            @RequestParam(required = false) String nombreTipoDocumento,
            RedirectAttributes redirectAttributes,
            Model model) {

        // El correo sí viene fijo de la sesión: es lo único que Google
        // realmente verificó. Nombre/apellido los completa/corrige la
        // persona misma (Google no siempre entrega el apellido), igual que
        // en el registro normal — la solicitud sigue pasando por el mismo
        // filtro de aprobación de un administrador.
        String correo = (String) session.getAttribute("googleCorreo");

        if (correo == null) {
            return "redirect:/registro";
        }

        model.addAttribute("nombre", nombre);
        model.addAttribute("apellido", apellido);
        model.addAttribute("correo", correo);
        model.addAttribute("tiposDocumento", TIPOS_DOCUMENTO);

        if (nombre == null || nombre.trim().isEmpty()) {
            model.addAttribute("errorDocumento", "El nombre es obligatorio.");
            return "registroCompletarGoogle";
        }
        if (apellido == null || apellido.trim().isEmpty()) {
            model.addAttribute("errorDocumento", "El apellido es obligatorio.");
            return "registroCompletarGoogle";
        }
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            model.addAttribute("errorDocumento", "El número de documento es obligatorio.");
            return "registroCompletarGoogle";
        }
        if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty()) {
            model.addAttribute("errorDocumento", "Debe seleccionar un tipo de documento.");
            return "registroCompletarGoogle";
        }

        try {
            Map<String, String> datos = new LinkedHashMap<>();
            datos.put("numeroDocumento", numeroDocumento.trim());
            datos.put("nombre", nombre.trim());
            datos.put("apellido", apellido.trim());
            datos.put("correo", correo);
            // Contraseña que la persona nunca ve ni usa — siempre entra por
            // "Continuar con Google". No se re-hashea nada especial: es el
            // mismo POST /api/solicitudes-registro de siempre, que hashea
            // con BCrypt como cualquier otra solicitud.
            datos.put("contrasena", UUID.randomUUID().toString() + UUID.randomUUID());
            datos.put("nombreTipoDocumento", nombreTipoDocumento.trim());

            authApiClient.registro(datos);

            session.removeAttribute("googleNombre");
            session.removeAttribute("googleApellido");
            session.removeAttribute("googleCorreo");

            redirectAttributes.addFlashAttribute("registroExitoso",
                "Tu solicitud fue enviada. Un administrador la revisará y podrás iniciar sesión cuando sea aprobada.");
            return "redirect:/login";

        } catch (HttpClientErrorException.BadRequest e) {
            model.addAttribute("errorDocumento", e.getResponseBodyAsString());
            return "registroCompletarGoogle";
        } catch (Exception e) {
            model.addAttribute("errorDocumento", "Error al registrar. Intente más tarde.");
            return "registroCompletarGoogle";
        }
    }
}
