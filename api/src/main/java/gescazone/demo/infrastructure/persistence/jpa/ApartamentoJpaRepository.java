package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.ApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApartamentoJpaRepository extends JpaRepository<ApartamentoEntity, UUID> {
    Optional<ApartamentoEntity> findByNumero(String numero);
    boolean existsByNumero(String numero);
    void deleteByNumero(String numero);
    List<ApartamentoEntity> findByTipoOcupacion_NombreTipoOcupacion(String nombreTipoOcupacion);
    List<ApartamentoEntity> findByEstadoCuenta_NombreEstadoCuenta(String nombreEstadoCuenta);
}
