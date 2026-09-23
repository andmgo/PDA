package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.SalonSocialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SalonSocialJpaRepository extends JpaRepository<SalonSocialEntity, UUID> {
    Optional<SalonSocialEntity> findByNumero(String numero);
    boolean existsByNumero(String numero);
    List<SalonSocialEntity> findByEstado_NombreEstado(String nombreEstado);
    List<SalonSocialEntity> findByMedidas(String medidas);
}
