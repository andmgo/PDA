package gescazone.demo.application.service;

import gescazone.demo.domain.model.NivelPermiso;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
