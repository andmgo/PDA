package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.RegistroVisitanteModel;
import java.util.List;
import java.util.Optional;

public interface RegistroVisitanteRepository {

    List<RegistroVisitanteModel> findByIdResidente(String idResidente);
    List<RegistroVisitanteModel> findByIdApartamento(String idApartamento);
    List<RegistroVisitanteModel> findByApartamentoNumero(String numero);

    boolean existsByIdResidenteAndIdApartamento(String idResidente, String idApartamento);
    Optional<RegistroVisitanteModel> findByIdResidenteAndIdApartamento(String idResidente, String idApartamento);

    List<RegistroVisitanteModel> findByFechaHoraSalidaIsNull();
    List<RegistroVisitanteModel> findByIdResidenteAndFechaHoraSalidaIsNull(String idResidente);
    List<RegistroVisitanteModel> findVisitantesActivosByIdApartamento(String idApartamento);

    void deleteByIdResidente(String idResidente);
    void deleteByIdApartamento(String idApartamento);

    long countVisitantesActivosByIdApartamento(String idApartamento);
    long countByIdResidente(String idResidente);

    List<RegistroVisitanteModel> findLatestRegistros();

    RegistroVisitanteModel save(RegistroVisitanteModel registro);

    // 👇 ESTOS SON LOS IMPORTANTES PARA ID ÚNICO
    Optional<RegistroVisitanteModel> findById(String id);
    void deleteById(String id);
    boolean existsById(String id);  // 🔥 ESTE FALTABA
}