package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.ApartamentoModel;
import java.util.List;
import java.util.Optional;

public interface ApartamentoRepository {

    Optional<ApartamentoModel> findByNumero(String numero);
    boolean existsByNumero(String numero);
    void deleteByNumero(String numero);
    List<ApartamentoModel> findByNombreTipoOcupacion(String nombreTipoOcupacion);
    List<ApartamentoModel> findByNombreEstadoCuenta(String nombreEstadoCuenta);
    ApartamentoModel save(ApartamentoModel apartamento);
    void deleteById(String id);
    Optional<ApartamentoModel> findById(String id);
    List<ApartamentoModel> findAll();
}