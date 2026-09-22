package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.EstadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EstadoJpaRepository extends JpaRepository<EstadoEntity, UUID> {
    Optional<EstadoEntity> findByNombreEstado(String nombreEstado);
}
