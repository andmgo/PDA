package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.PermisoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PermisoJpaRepository extends JpaRepository<PermisoEntity, UUID> {
    Optional<PermisoEntity> findByCodigo(String codigo);
}
