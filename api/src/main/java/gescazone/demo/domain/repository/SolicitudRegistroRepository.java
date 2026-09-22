package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.SolicitudRegistroModel;

import java.util.List;
import java.util.Optional;

public interface SolicitudRegistroRepository {
    SolicitudRegistroModel save(SolicitudRegistroModel solicitud);
    Optional<SolicitudRegistroModel> findById(String id);
    List<SolicitudRegistroModel> findByEstado(String estado);
    List<SolicitudRegistroModel> findAll();
}
