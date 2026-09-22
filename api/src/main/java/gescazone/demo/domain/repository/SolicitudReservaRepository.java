package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.SolicitudReservaModel;

import java.util.List;
import java.util.Optional;

public interface SolicitudReservaRepository {
    SolicitudReservaModel save(SolicitudReservaModel solicitud);
    Optional<SolicitudReservaModel> findById(String id);
    List<SolicitudReservaModel> findByEstado(String estado);
    List<SolicitudReservaModel> findAll();
}
