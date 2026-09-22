package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import gescazone.demo.infrastructure.persistence.entity.PermisoEntity;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.RolPermisoEntity;
import gescazone.demo.infrastructure.persistence.jpa.PermisoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolPermisoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RolPermisoRepositoryImpl implements RolPermisoRepository {

    @Autowired
    private RolJpaRepository rolJpaRepository;

    @Autowired
    private PermisoJpaRepository permisoJpaRepository;

    @Autowired
    private RolPermisoJpaRepository rolPermisoJpaRepository;

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
                .orElseThrow(() -> new IllegalArgumentException("No existe el rol: " + nombreRol));
        PermisoEntity permiso = permisoJpaRepository.findByCodigo(codigoPermiso)
                .orElseThrow(() -> new IllegalArgumentException("No existe el permiso: " + codigoPermiso));

        RolPermisoEntity celda = rolPermisoJpaRepository
                .findByRol_NombreRolAndPermiso_Codigo(nombreRol, codigoPermiso)
                .orElseGet(RolPermisoEntity::new);

        celda.setRol(rol);
        celda.setPermiso(permiso);
        celda.setPuedeVer(puedeVer);
        celda.setPuedeEditar(puedeEditar);
        rolPermisoJpaRepository.save(celda);
    }
}
