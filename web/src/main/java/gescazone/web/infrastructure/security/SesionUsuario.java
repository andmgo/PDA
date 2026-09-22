package gescazone.web.infrastructure.security;

import gescazone.web.dto.LoginResultado;
import gescazone.web.dto.PermisoCelda;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.util.List;

/**
 * Guarda el JWT emitido por gescazone-api (y los datos del usuario logueado)
 * durante toda la sesión HTTP del navegador — equivalente Java del
 * SesionUsuario.cs del proyecto de referencia, pero atado a una sesión HTTP
 * clásica (Thymeleaf server-rendered) en vez de un "circuito" de Blazor.
 *
 * Las rutas MVC (SecurityConfig) y las plantillas consultan tienePermiso(...)
 * en vez de llamarle de nuevo a la Api en cada render — los permisos se
 * recalculan recién en el próximo login si cambian.
 */
@Component
@SessionScope
public class SesionUsuario {

    private String jwt;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String nombreRol;
    private List<PermisoCelda> permisos = List.of();

    public void iniciarSesion(LoginResultado resultado) {
        this.jwt = resultado.token();
        this.numeroDocumento = resultado.numeroDocumento();
        this.nombre = resultado.nombre();
        this.apellido = resultado.apellido();
        this.nombreRol = resultado.nombreRol();
        this.permisos = resultado.permisos() != null ? resultado.permisos() : List.of();
    }

    public void cerrarSesion() {
        jwt = null;
        numeroDocumento = null;
        nombre = null;
        apellido = null;
        nombreRol = null;
        permisos = List.of();
    }

    public boolean estaAutenticado() {
        return jwt != null;
    }

    public boolean tienePermiso(String codigo, boolean requiereEditar) {
        return permisos.stream()
                .filter(p -> p.codigo().equals(codigo))
                .findFirst()
                .map(p -> requiereEditar ? p.puedeEditar() : p.puedeVer())
                .orElse(false);
    }

    /** "ADMINISTRADOR" (como lo guarda la BD) -> "Administrador" (como lo esperan las plantillas). */
    public String getNombreRolParaVista() {
        if (nombreRol == null) return "Desconocido";
        return switch (nombreRol.toUpperCase()) {
            case "ADMINISTRADOR" -> "Administrador";
            case "PROPIETARIO"   -> "Propietario";
            case "FUNCIONARIO"   -> "Funcionario";
            default               -> "Desconocido";
        };
    }

    public String getJwt() { return jwt; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getNombreRol() { return nombreRol; }
    public List<PermisoCelda> getPermisos() { return permisos; }
}
