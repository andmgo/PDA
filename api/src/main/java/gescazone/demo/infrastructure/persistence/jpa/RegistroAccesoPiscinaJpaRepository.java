package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.RegistroAccesoPiscinaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface RegistroAccesoPiscinaJpaRepository extends JpaRepository<RegistroAccesoPiscinaEntity, UUID> {
    List<RegistroAccesoPiscinaEntity> findByApartamento_Numero(String numero);
    List<RegistroAccesoPiscinaEntity> findByResidente_NumeroDocumento(Integer numeroDocumento);
    List<RegistroAccesoPiscinaEntity> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    List<RegistroAccesoPiscinaEntity> findAllByOrderByFechaHoraDesc();
}
