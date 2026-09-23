package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import gescazone.demo.infrastructure.persistence.entity.PermisoEntity;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.RolPermisoEntity;
import gescazone.demo.infrastructure.persistence.jpa.PermisoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolPermisoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RolPermisoRepositoryImpl implements RolPermisoRepository {

    /**
     * Único permiso que puede administrar esta misma pantalla (ver
     * SecurityConfig, requestMatchers("/api/roles-permisos/**")). Si el
     * último rol que lo tiene en modo editar lo perdiera, nadie podría
     * volver a entrar aquí para revertirlo — se bloquea ese caso.
     */
    private static final String PERMISO_PROTEGIDO = "GESTION_DATOS";

    @Autowired
    private RolJpaRepository rolJpaRepository;

    @Autowired
    private PermisoJpaRepository permisoJpaRepository;

    @Autowired
    private RolPermisoJpaRepository rolPermisoJpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    private PermisoModel toModel(PermisoEntity entity) {
        PermisoModel model = new PermisoModel();
        model.setId(entity.getId().toString());
        model.setCodigo(entity.getCodigo());
        model.setNombre(entity.getNombre());
        model.setModulo(entity.getModulo());
        return model;
    }

    private RolPermisoModel toModel(RolPermisoEntity entity) {
        RolPermisoModel model = new RolPermisoModel();
        model.setNombreRol(entity.getRol().getNombreRol());
        model.setCodigoPermiso(entity.getPermiso().getCodigo());
        model.setPuedeVer(entity.isPuedeVer());
        model.setPuedeEditar(entity.isPuedeEditar());
        return model;
    }

    @Override
    public List<PermisoModel> listarPermisos() {
        return permisoJpaRepository.findAll().stream().map(this::toModel).toList();
    }

    @Override
    public List<String> listarNombresRoles() {
        return rolJpaRepository.findAll().stream().map(RolEntity::getNombreRol).toList();
    }

    @Override
    public List<RolPermisoModel> matrizCompleta() {
        return rolPermisoJpaRepository.findAll().stream().map(this::toModel).toList();
    }

    @Override
    public void crearRol(String nombreRol) {
        if (rolJpaRepository.findByNombreRol(nombreRol).isPresent()) {
            throw new IllegalArgumentException("Ya existe un rol con ese nombre: " + nombreRol);
        }
        rolJpaRepository.save(new RolEntity(nombreRol));
    }

    @Override
    public void guardarPermiso(String nombreRol, String codigoPermiso, boolean puedeVer, boolean puedeEditar) {
        RolEntity rol = rolJpaRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new NotFoundException("No existe el rol: " + nombreRol));
        PermisoEntity permiso = permisoJpaRepository.findByCodigo(codigoPermiso)
                .orElseThrow(() -> new NotFoundException("No existe el permiso: " + codigoPermiso));

        RolPermisoEntity celda = rolPermisoJpaRepository
                .findByRol_NombreRolAndPermiso_Codigo(nombreRol, codigoPermiso)
                .orElseGet(RolPermisoEntity::new);

        boolean quitaLaUltimaEdicionProtegida = PERMISO_PROTEGIDO.equals(codigoPermiso)
                && celda.isPuedeEditar() && !puedeEditar
                && contarOtrosRolesConEdicion(PERMISO_PROTEGIDO, nombreRol) == 0;
        if (quitaLaUltimaEdicionProtegida) {
            throw new ValidationException("No se puede quitar el permiso \"" + PERMISO_PROTEGIDO
                    + "\" (editar) de \"" + nombreRol + "\": es el único rol que puede administrar roles y "
                    + "permisos. Dale ese permiso a otro rol antes de quitárselo a este.");
        }

        celda.setRol(rol);
        celda.setPermiso(permiso);
        celda.setPuedeVer(puedeVer);
        celda.setPuedeEditar(puedeEditar);
        rolPermisoJpaRepository.save(celda);
    }

    @Override
    public void eliminarRol(String nombreRol) {
        RolEntity rol = rolJpaRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new NotFoundException("No existe el rol: " + nombreRol));

        if (!usuarioJpaRepository.findByRol_NombreRol(nombreRol).isEmpty()) {
            throw new ValidationException("No se puede eliminar el rol \"" + nombreRol
                    + "\": todavía tiene usuarios asignados.");
        }

        boolean esElUltimoConEdicionProtegida = rolPermisoJpaRepository
                .findByRol_NombreRolAndPermiso_Codigo(nombreRol, PERMISO_PROTEGIDO)
                .map(RolPermisoEntity::isPuedeEditar)
                .orElse(false)
                && contarOtrosRolesConEdicion(PERMISO_PROTEGIDO, nombreRol) == 0;
        if (esElUltimoConEdicionProtegida) {
            throw new ValidationException("No se puede eliminar el rol \"" + nombreRol
                    + "\": es el único que puede administrar roles y permisos.");
        }

        rolPermisoJpaRepository.deleteAll(rolPermisoJpaRepository.findByRol_NombreRol(nombreRol));
        rolJpaRepository.delete(rol);
    }

    private long contarOtrosRolesConEdicion(String codigoPermiso, String nombreRolExcluido) {
        return rolPermisoJpaRepository.findAll().stream()
                .filter(c -> c.getPermiso().getCodigo().equals(codigoPermiso)
                        && c.isPuedeEditar()
                        && !c.getRol().getNombreRol().equalsIgnoreCase(nombreRolExcluido))
                .count();
    }
}
