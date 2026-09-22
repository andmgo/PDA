package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.SolicitudReservaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitudReservaJpaRepository extends JpaRepository<SolicitudReservaEntity, UUID> {
    List<SolicitudReservaEntity> findByEstado(String estado);
}
