package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RolJpaRepository extends JpaRepository<RolEntity, UUID> {
    Optional<RolEntity> findByNombreRol(String nombreRol);
}
