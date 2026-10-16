package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.PagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PagoJpaRepository extends JpaRepository<PagoEntity, UUID> {
    List<PagoEntity> findByCargoIdIn(Collection<UUID> cargos);
    Optional<PagoEntity> findByReferencia(String referencia);
}
