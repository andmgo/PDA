package gescazone.demo.application.service;

import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.NivelPermiso;
import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Resuelve permisos consultando rol_permiso en tiempo real (nunca hardcodeado
 * por rol) — cacheado en memoria para no pegarle a la base en cada request.
 * invalidarCache() se llama al guardar cambios en la pantalla "Roles y
 * permisos". SecurityConfig consulta tienePermiso() en cada request, así que
 * cambiar qué puede hacer un rol (o crear uno nuevo) no requiere redeploy.
 */
@Service
public class PermisoService {

    @Autowired
    private RolPermisoRepository rolPermisoRepository;

    private volatile Map<String, RolPermisoModel> matrizCacheada;

    public boolean tienePermiso(String nombreRol, String codigoPermiso, NivelPermiso nivel) {
        if (nombreRol == null || codigoPermiso == null) return false;

        RolPermisoModel celda = obtenerMatriz().get(clave(nombreRol, codigoPermiso));
        if (celda == null) return false;

        return nivel == NivelPermiso.EDITAR ? celda.isPuedeEditar() : celda.isPuedeVer();
    }

    public void invalidarCache() {
        matrizCacheada = null;
    }

    // ── Administración de roles y permisos (pantalla "Roles y permisos") ──
    // Antes RolPermisoRestController escribía directo en el repositorio y
    // llamaba invalidarCache() a mano después de cada escritura: si otro
    // punto modificaba permisos y lo olvidaba, el cambio no se aplicaba
    // hasta reiniciar. Ahora toda escritura pasa por aquí e invalida sola.

    public List<PermisoModel> listarPermisos() {
        return rolPermisoRepository.listarPermisos();
    }

    public List<String> listarRoles() {
        return rolPermisoRepository.listarNombresRoles();
    }

    public List<RolPermisoModel> matriz() {
        return rolPermisoRepository.matrizCompleta();
    }

    /** Celdas de un rol (para armar los permisos de la sesión al iniciar sesión). */
    public List<RolPermisoModel> permisosDeRol(String nombreRol) {
        if (nombreRol == null) return List.of();
        return obtenerMatriz().values().stream()
                .filter(c -> c.getNombreRol().equalsIgnoreCase(nombreRol))
                .toList();
    }

    public void crearRol(String nombreRol) {
        if (nombreRol == null || nombreRol.trim().isEmpty())
            throw new ValidationException("El nombre del rol no puede estar vacío");
        rolPermisoRepository.crearRol(nombreRol.trim().toUpperCase());
        invalidarCache();
    }

    public void guardarPermiso(String nombreRol, String codigoPermiso, boolean puedeVer, boolean puedeEditar) {
        if (nombreRol == null || codigoPermiso == null)
            throw new ValidationException("nombreRol y codigoPermiso son obligatorios");
        rolPermisoRepository.guardarPermiso(nombreRol, codigoPermiso, puedeVer, puedeEditar);
        invalidarCache();
    }

    public void eliminarRol(String nombreRol) {
        rolPermisoRepository.eliminarRol(nombreRol);
        invalidarCache();
    }

    private Map<String, RolPermisoModel> obtenerMatriz() {
        Map<String, RolPermisoModel> matriz = matrizCacheada;
        if (matriz != null) return matriz;

        synchronized (this) {
            if (matrizCacheada == null) {
                matrizCacheada = rolPermisoRepository.matrizCompleta().stream()
                        .collect(Collectors.toMap(
                                c -> clave(c.getNombreRol(), c.getCodigoPermiso()),
                                Function.identity(),
                                (a, b) -> b));
            }
            return matrizCacheada;
        }
    }

    private String clave(String nombreRol, String codigoPermiso) {
        return nombreRol.toUpperCase() + "::" + codigoPermiso;
    }
}
