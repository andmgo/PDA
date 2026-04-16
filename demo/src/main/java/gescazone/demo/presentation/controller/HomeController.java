package gescazone.demo.presentation.controller;

import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.application.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/")
    public String index() {
        return "login";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        // Por si acaso session.rolUsuario no está, lo reconstruimos desde Spring Security
        if (session.getAttribute("rolUsuario") == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String rolCompleto = auth.getAuthorities().stream()
                        .findFirst()
                        .map(a -> a.getAuthority())
                        .orElse("");

                String rolParaVista = switch (rolCompleto) {
                    case "ROLE_ADMINISTRADOR" -> "Administrador";
                    case "ROLE_PROPIETARIO"   -> "Propietario";
                    case "ROLE_FUNCIONARIO"   -> "Funcionario";
                    default                   -> "";
                };

                session.setAttribute("rolUsuario", rolParaVista);
                session.setAttribute("usuarioLogueado", auth.getName());
            }
        }
        return "inicio";
    }

    @GetMapping("/gestionDeDatos")
    public String gestionDeDatos() {
        return "gestionDeDatos";
    }

    @GetMapping("/pagosYCartera")
    public String pagosYCartera() {
        return "pagosYCartera";
    }

    @GetMapping("/reservas")
    public String reservas() {
        return "reservas";
    }

    @GetMapping("/controlDeAccesos")
    public String controlDeAccesos() {
        return "controlDeAccesos";
    }

    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        String documentoUsuario = (String) session.getAttribute("usuarioLogueado");

        // Por si acaso usuarioLogueado no está en sesión
        if (documentoUsuario == null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                documentoUsuario = auth.getName();
                session.setAttribute("usuarioLogueado", documentoUsuario);
            } else {
                return "redirect:/login";
            }
        }

        UsuarioModel usuario = usuarioService.consultar(documentoUsuario);
        if (usuario == null)
            return "redirect:/login";

        model.addAttribute("usuario", usuario);
        return "profile";
    }
}