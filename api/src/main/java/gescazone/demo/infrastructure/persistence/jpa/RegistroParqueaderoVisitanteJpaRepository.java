package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.RegistroParqueaderoVisitanteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RegistroParqueaderoVisitanteJpaRepository extends JpaRepository<RegistroParqueaderoVisitanteEntity, UUID> {
    List<RegistroParqueaderoVisitanteEntity> findByResidente_Id(UUID idResidente);
    List<RegistroParqueaderoVisitanteEntity> findByParqueadero_Id(UUID idParqueadero);
    List<RegistroParqueaderoVisitanteEntity> findByApartamento_Id(UUID idApartamento);
    List<RegistroParqueaderoVisitanteEntity> findByParqueadero_Numero(String numero);
    boolean existsByResidente_IdAndParqueadero_Id(UUID idResidente, UUID idParqueadero);
    Optional<RegistroParqueaderoVisitanteEntity> findByResidente_IdAndParqueadero_Id(UUID idResidente, UUID idParqueadero);
    List<RegistroParqueaderoVisitanteEntity> findByFechaHoraSalidaIsNull();
    List<RegistroParqueaderoVisitanteEntity> findByResidente_IdAndFechaHoraSalidaIsNull(UUID idResidente);
    List<RegistroParqueaderoVisitanteEntity> findByParqueadero_IdAndFechaHoraSalidaIsNull(UUID idParqueadero);
    List<RegistroParqueaderoVisitanteEntity> findByApartamento_IdAndFechaHoraSalidaIsNull(UUID idApartamento);
    List<RegistroParqueaderoVisitanteEntity> findByPlacaContainingIgnoreCase(String placa);
    long countByParqueadero_IdAndFechaHoraSalidaIsNull(UUID idParqueadero);
    long countByApartamento_IdAndFechaHoraSalidaIsNull(UUID idApartamento);
    long countByResidente_Id(UUID idResidente);
    List<RegistroParqueaderoVisitanteEntity> findAllByOrderByFechaHoraEntradaDesc();
    void deleteByResidente_Id(UUID idResidente);
    void deleteByParqueadero_Id(UUID idParqueadero);
    void deleteByApartamento_Id(UUID idApartamento);
}
