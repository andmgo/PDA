package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.ResidenteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidenteJpaRepository extends JpaRepository<ResidenteEntity, UUID> {
    Optional<ResidenteEntity> findByNumeroDocumento(Integer numeroDocumento);
    boolean existsByNumeroDocumento(Integer numeroDocumento);
    void deleteByNumeroDocumento(Integer numeroDocumento);
    List<ResidenteEntity> findByTipoResidente_NombreTipoResidente(String nombreTipoResidente);
    List<ResidenteEntity> findByTipoDocumento_NombreTipoDocumento(String nombreTipoDocumento);
    List<ResidenteEntity> findByNombreContainingIgnoreCase(String nombre);
    List<ResidenteEntity> findByApellidoContainingIgnoreCase(String apellido);
    List<ResidenteEntity> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
}
