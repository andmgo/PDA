package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.ResidenteModel;
import java.util.List;
import java.util.Optional;

public interface ResidenteRepository {
    Optional<ResidenteModel> findByNumeroDocumento(String numeroDocumento);
    boolean existsByNumeroDocumento(String numeroDocumento);
    List<ResidenteModel> findByNombreTipoResidente(String nombreTipoResidente);
    List<ResidenteModel> findByNombreTipoDocumento(String nombreTipoDocumento);
    List<ResidenteModel> findByNombreContainingIgnoreCase(String nombre);
    List<ResidenteModel> findByApellidoContainingIgnoreCase(String apellido);
    List<ResidenteModel> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
    ResidenteModel save(ResidenteModel residente);
    Optional<ResidenteModel> findById(String id);
    List<ResidenteModel> findAll();
}