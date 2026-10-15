package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.VehiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehiculoJpaRepository extends JpaRepository<VehiculoEntity, UUID> {
    Optional<VehiculoEntity> findByPlacaAndActivoTrue(String placa);
    List<VehiculoEntity> findByApartamento_IdOrderByActivoDescFechaRegistroDesc(UUID idApartamento);

    @Query("select distinct v.parqueadero.id from VehiculoEntity v where v.activo = true and v.parqueadero is not null")
    List<UUID> findIdsParqueaderoAsignados();
}
