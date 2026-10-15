package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.VisitaAutorizadaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface VisitaAutorizadaJpaRepository extends JpaRepository<VisitaAutorizadaEntity, UUID> {

    List<VisitaAutorizadaEntity> findByAutorizadaPor_IdOrderByFechaDesdeDescFechaCreacionDesc(UUID idUsuario);

    @Query("select v from VisitaAutorizadaEntity v where v.cancelada = false "
            + "and v.fechaDesde <= :fecha and v.fechaHasta >= :fecha order by v.fechaCreacion")
    List<VisitaAutorizadaEntity> findVigentesEn(LocalDate fecha);

    @Query(value = "select (select count(*) from registro_visitante where visita_autorizada_id = :id) "
            + "+ (select count(*) from registro_parqueadero_visitante where visita_autorizada_id = :id)",
            nativeQuery = true)
    long contarIngresos(UUID id);
}
