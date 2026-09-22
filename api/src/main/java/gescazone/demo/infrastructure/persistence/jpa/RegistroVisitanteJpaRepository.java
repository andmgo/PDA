package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.RegistroVisitanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroVisitanteJpaRepository extends JpaRepository<RegistroVisitanteEntity, UUID> {
    List<RegistroVisitanteEntity> findByResidente_Id(UUID idResidente);
    List<RegistroVisitanteEntity> findByApartamento_Id(UUID idApartamento);
    List<RegistroVisitanteEntity> findByApartamento_Numero(String numero);
    boolean existsByResidente_IdAndApartamento_Id(UUID idResidente, UUID idApartamento);
    Optional<RegistroVisitanteEntity> findByResidente_IdAndApartamento_Id(UUID idResidente, UUID idApartamento);
    List<RegistroVisitanteEntity> findByFechaHoraSalidaIsNull();
    List<RegistroVisitanteEntity> findByResidente_IdAndFechaHoraSalidaIsNull(UUID idResidente);
    List<RegistroVisitanteEntity> findByApartamento_IdAndFechaHoraSalidaIsNull(UUID idApartamento);
    long countByApartamento_IdAndFechaHoraSalidaIsNull(UUID idApartamento);
    long countByResidente_Id(UUID idResidente);
    List<RegistroVisitanteEntity> findAllByOrderByFechaHoraEntradaDesc();
    void deleteByResidente_Id(UUID idResidente);
    void deleteByApartamento_Id(UUID idApartamento);
}
