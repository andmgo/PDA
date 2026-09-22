package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.PaqueteModel;

import java.util.List;
import java.util.Optional;

public interface PaqueteRepository {
    PaqueteModel save(PaqueteModel paquete);
    Optional<PaqueteModel> findById(String id);
    List<PaqueteModel> findAll();
    List<PaqueteModel> findByEntregadoFalse();
    List<PaqueteModel> findByApartamentoId(String idApartamento);
}
