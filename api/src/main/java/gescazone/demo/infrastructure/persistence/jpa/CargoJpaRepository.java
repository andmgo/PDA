package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.CargoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CargoJpaRepository extends JpaRepository<CargoEntity, UUID> {
    List<CargoEntity> findByAnuladoFalse();
    List<CargoEntity> findByAnuladoFalseAndApartamentoIdIn(Collection<UUID> apartamentos);
    Optional<CargoEntity> findFirstByReservaIdAndAnuladoFalse(UUID reservaId);
    boolean existsByApartamentoIdAndPeriodoAndConceptoAndAnuladoFalse(UUID apartamentoId, String periodo, String concepto);
}
