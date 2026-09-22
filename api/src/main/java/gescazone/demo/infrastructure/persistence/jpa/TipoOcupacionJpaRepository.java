package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.TipoOcupacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoOcupacionJpaRepository extends JpaRepository<TipoOcupacionEntity, UUID> {
    Optional<TipoOcupacionEntity> findByNombreTipoOcupacion(String nombreTipoOcupacion);
}
