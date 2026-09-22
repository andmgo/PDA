package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.EstadoCuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EstadoCuentaJpaRepository extends JpaRepository<EstadoCuentaEntity, UUID> {
    Optional<EstadoCuentaEntity> findByNombreEstadoCuenta(String nombreEstadoCuenta);
}
