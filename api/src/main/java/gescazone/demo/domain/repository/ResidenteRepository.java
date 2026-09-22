package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.ResidenteModel;
import java.util.List;
import java.util.Optional;

public interface ResidenteRepository {
    Optional<ResidenteModel> findByNumeroDocumento(Integer numeroDocumento);
    boolean existsByNumeroDocumento(Integer numeroDocumento);
    void deleteByNumeroDocumento(Integer numeroDocumento);
    List<ResidenteModel> findByNombreTipoResidente(String nombreTipoResidente);
    List<ResidenteModel> findByNombreTipoDocumento(String nombreTipoDocumento);
    List<ResidenteModel> findByNombreContainingIgnoreCase(String nombre);
    List<ResidenteModel> findByApellidoContainingIgnoreCase(String apellido);
    List<ResidenteModel> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido);
    ResidenteModel save(ResidenteModel residente);
    void deleteById(String id);
    Optional<ResidenteModel> findById(String id);
    List<ResidenteModel> findAll();
}