package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, UUID> {
    Optional<VehiculoEntity> findByPlacaAndActivoTrue(String placa);
    List<VehiculoEntity> findByApartamento_IdOrderByActivoDescFechaRegistroDesc(UUID idApartamento);
}
