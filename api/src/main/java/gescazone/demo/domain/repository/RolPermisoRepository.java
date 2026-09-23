package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;

import java.util.List;

public interface RolPermisoRepository {

    /** Catálogo fijo de permisos (las funcionalidades que el sistema ya sabe hacer). */
    List<PermisoModel> listarPermisos();

    /** Nombres de todos los roles existentes (los 3 base + los que el admin haya creado). */
    List<String> listarNombresRoles();

    /** Todas las celdas de la matriz con algún acceso otorgado (ver y/o editar). */
    List<RolPermisoModel> matrizCompleta();

    /** Crea un rol nuevo sin ningún permiso marcado — nunca hereda de otro rol. */
    void crearRol(String nombreRol);

    /** Marca/desmarca ver y editar para una celda (rol, permiso) puntual. */
    void guardarPermiso(String nombreRol, String codigoPermiso, boolean puedeVer, boolean puedeEditar);

    /**
     * Elimina un rol. Falla si no existe, si todavía tiene usuarios asignados,
     * o si es el último rol con permiso para administrar roles y permisos
     * (dejaría la pantalla sin nadie que pueda volver a abrirla).
     */
    void eliminarRol(String nombreRol);
}
