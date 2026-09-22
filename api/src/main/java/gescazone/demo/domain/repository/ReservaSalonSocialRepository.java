package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.ReservaSalonSocialModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservaSalonSocialRepository {
    List<ReservaSalonSocialModel> findByIdSalon(String idSalon);
    List<ReservaSalonSocialModel> findBySalonNumero(String numero);
    List<ReservaSalonSocialModel> findByIdUsuario(String idUsuario);
    List<ReservaSalonSocialModel> findByUsuarioNumeroDocumento(String documento);
    boolean existsByIdSalonAndFechaYHoraReserva(String idSalon, LocalDateTime fechaYHora);
    List<ReservaSalonSocialModel> findByFechaYHoraReservaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<ReservaSalonSocialModel> findReservasFuturasPorSalon(String idSalon, LocalDateTime ahora);
    List<ReservaSalonSocialModel> findReservasFuturasPorUsuario(String idUsuario, LocalDateTime ahora);
    boolean existsReservaEnMismoDia(String idSalon, LocalDateTime fecha);
    ReservaSalonSocialModel save(ReservaSalonSocialModel reserva);
    void deleteById(String id);
    Optional<ReservaSalonSocialModel> findById(String id);
    List<ReservaSalonSocialModel> findAll();
}