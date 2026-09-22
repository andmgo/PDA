package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.ParqueaderoModel;
import java.util.List;
import java.util.Optional;

public interface ParqueaderoRepository {
    Optional<ParqueaderoModel> findByNumero(String numero);
    boolean existsByNumero(String numero);
    void deleteByNumero(String numero);
    List<ParqueaderoModel> findByNombreEstado(String nombreEstado);
    List<ParqueaderoModel> findByMedidas(String medidas);
    ParqueaderoModel save(ParqueaderoModel parqueadero);
    void deleteById(String id);
    Optional<ParqueaderoModel> findById(String id);
    List<ParqueaderoModel> findAll();
}