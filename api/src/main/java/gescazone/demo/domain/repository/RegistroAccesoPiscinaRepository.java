package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.RegistroAccesoPiscinaModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RegistroAccesoPiscinaRepository {
    List<RegistroAccesoPiscinaModel> findByApartamentoNumero(String numero);
    List<RegistroAccesoPiscinaModel> findByResidenteNumeroDocumento(Integer numeroDocumento);
    List<RegistroAccesoPiscinaModel> findRegistrosHoy();
    List<RegistroAccesoPiscinaModel> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);
    List<RegistroAccesoPiscinaModel> findAllOrderByFechaHoraDesc();
    RegistroAccesoPiscinaModel save(RegistroAccesoPiscinaModel registro);
    void deleteById(String id);
    Optional<RegistroAccesoPiscinaModel> findById(String id);
    boolean existsById(String id);
}