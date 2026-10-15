package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.VisitaAutorizadaModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitaAutorizadaRepository {
    VisitaAutorizadaModel save(VisitaAutorizadaModel visita);
    Optional<VisitaAutorizadaModel> findById(String id);
    /** Autorizaciones creadas por un usuario, de la más reciente a la más antigua. */
    List<VisitaAutorizadaModel> findByAutorizadaPor(String idUsuario);
    /** No canceladas y vigentes en esa fecha (todas las del conjunto). */
    List<VisitaAutorizadaModel> findVigentesEn(LocalDate fecha);
    /** Cuántos ingresos se han registrado con esta autorización. */
    long contarIngresos(String idVisitaAutorizada);
}
