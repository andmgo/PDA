package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import java.util.List;
import java.util.Optional;

public interface RegistroParqueaderoVisitanteRepository {
    List<RegistroParqueaderoVisitanteModel> findByIdResidente(String idResidente);
    List<RegistroParqueaderoVisitanteModel> findByIdParqueadero(String idParqueadero);
    List<RegistroParqueaderoVisitanteModel> findByIdApartamento(String idApartamento);
    List<RegistroParqueaderoVisitanteModel> findByParqueaderoNumero(String numero);
    boolean existsByIdResidenteAndIdParqueadero(String idResidente, String idParqueadero);
    Optional<RegistroParqueaderoVisitanteModel> findByIdResidenteAndIdParqueadero(String idResidente, String idParqueadero);
    List<RegistroParqueaderoVisitanteModel> findByFechaHoraSalidaIsNull();
    List<RegistroParqueaderoVisitanteModel> findByIdResidenteAndFechaHoraSalidaIsNull(String idResidente);
    List<RegistroParqueaderoVisitanteModel> findActivosByIdParqueadero(String idParqueadero);
    List<RegistroParqueaderoVisitanteModel> findActivosByIdApartamento(String idApartamento);
    List<RegistroParqueaderoVisitanteModel> findByPlacaContainingIgnoreCase(String placa);
    void deleteByIdResidente(String idResidente);
    void deleteByIdParqueadero(String idParqueadero);
    void deleteByIdApartamento(String idApartamento);
    long countActivosByIdParqueadero(String idParqueadero);
    long countActivosByIdApartamento(String idApartamento);
    long countByIdResidente(String idResidente);
    List<RegistroParqueaderoVisitanteModel> findLatestRegistros();
    RegistroParqueaderoVisitanteModel save(RegistroParqueaderoVisitanteModel registro);
    void deleteById(String id);
    Optional<RegistroParqueaderoVisitanteModel> findById(String id);
}