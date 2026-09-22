package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.TipoDocumentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TipoDocumentoJpaRepository extends JpaRepository<TipoDocumentoEntity, UUID> {
    Optional<TipoDocumentoEntity> findByNombreTipoDocumento(String nombreTipoDocumento);
}
