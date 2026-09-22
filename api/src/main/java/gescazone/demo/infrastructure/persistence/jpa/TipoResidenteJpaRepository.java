package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.TipoResidenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoResidenteJpaRepository extends JpaRepository<TipoResidenteEntity, UUID> {
    Optional<TipoResidenteEntity> findByNombreTipoResidente(String nombreTipoResidente);
}
