package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.RolPermisoEntity;
import gescazone.demo.infrastructure.persistence.entity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolPermisoJpaRepository extends JpaRepository<RolPermisoEntity, RolPermisoId> {
    Optional<RolPermisoEntity> findByRol_NombreRolAndPermiso_Codigo(String nombreRol, String codigoPermiso);
}
