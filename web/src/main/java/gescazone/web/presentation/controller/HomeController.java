package gescazone.web.presentation.controller;

import gescazone.web.infrastructure.client.UsuarioApiClient;
import gescazone.web.infrastructure.security.SesionUsuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private SesionUsuario sesionUsuario;

    @Autowired
    private UsuarioApiClient usuarioApiClient;

    /**
     * Landing pública: lo primero que se ve al entrar (antes era directo el
     * login). Si ya hay sesión, el encabezado ofrece "Ir a mi panel" en vez
     * de iniciar sesión / registrarse.
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("autenticado", sesionUsuario.estaAutenticado());
        return "landing";
    }

    @GetMapping("/inicio")
    public String inicio(HttpSession session) {
        // Por si acaso session.rolUsuario no está pero SesionUsuario sí lo tiene
        // (misma sesión HTTP, distinto bean) — lo reconstruimos.
        if (session.getAttribute("rolUsuario") == null && sesionUsuario.estaAutenticado()) {
            session.setAttribute("rolUsuario", sesionUsuario.getNombreRolParaVista());
            session.setAttribute("usuarioLogueado", sesionUsuario.getNumeroDocumento());
        }
        return "inicio";
    }

    @GetMapping("/gestionDeDatos")
    public String gestionDeDatos() {
        return "gestionDeDatos";
    }

    @GetMapping("/rolesYPermisos")
    public String rolesYPermisos() {
        return "rolesYPermisos";
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

    @GetMapping("/misVisitas")
    public String misVisitas() {
        return "misVisitas";
    }

    @GetMapping("/paquetes")
    public String paquetes() {
        return "paquetes";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        if (!sesionUsuario.estaAutenticado()) {
            return "redirect:/login";
        }
        var usuario = usuarioApiClient.consultar(sesionUsuario.getNumeroDocumento());
        if (usuario == null) {
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        return "profile";
    }
}
