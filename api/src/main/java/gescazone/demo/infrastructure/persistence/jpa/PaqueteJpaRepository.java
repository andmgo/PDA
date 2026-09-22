package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.PaqueteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaqueteJpaRepository extends JpaRepository<PaqueteEntity, UUID> {
    List<PaqueteEntity> findByEntregadoFalse();
    List<PaqueteEntity> findByApartamento_Id(UUID idApartamento);
}
