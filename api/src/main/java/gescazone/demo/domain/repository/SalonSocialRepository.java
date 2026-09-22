package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.SalonSocialModel;
import java.util.List;
import java.util.Optional;

public interface SalonSocialRepository {
    Optional<SalonSocialModel> findByNumero(String numero);
    boolean existsByNumero(String numero);
    void deleteByNumero(String numero);
    List<SalonSocialModel> findByNombreEstado(String nombreEstado);
    List<SalonSocialModel> findByMedidas(String medidas);
    SalonSocialModel save(SalonSocialModel salon);
    void deleteById(String id);
    Optional<SalonSocialModel> findById(String id);
    List<SalonSocialModel> findAll();
}