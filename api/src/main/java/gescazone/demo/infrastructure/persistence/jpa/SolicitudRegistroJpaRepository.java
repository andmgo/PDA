package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.SolicitudRegistroEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SolicitudRegistroJpaRepository extends JpaRepository<SolicitudRegistroEntity, UUID> {
    List<SolicitudRegistroEntity> findByEstado(String estado);
}
