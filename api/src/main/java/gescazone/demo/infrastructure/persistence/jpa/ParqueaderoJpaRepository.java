package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.ParqueaderoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParqueaderoJpaRepository extends JpaRepository<ParqueaderoEntity, UUID> {
    Optional<ParqueaderoEntity> findByNumero(String numero);
    boolean existsByNumero(String numero);
    List<ParqueaderoEntity> findByEstado_NombreEstado(String nombreEstado);
    List<ParqueaderoEntity> findByMedidas(String medidas);
}
