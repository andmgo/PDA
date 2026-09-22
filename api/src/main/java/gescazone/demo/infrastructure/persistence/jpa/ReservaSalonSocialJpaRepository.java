package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.ReservaSalonSocialEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservaSalonSocialJpaRepository extends JpaRepository<ReservaSalonSocialEntity, UUID> {
    List<ReservaSalonSocialEntity> findBySalon_Id(UUID idSalon);
    List<ReservaSalonSocialEntity> findBySalon_Numero(String numero);
    List<ReservaSalonSocialEntity> findByUsuario_Id(UUID idUsuario);
    List<ReservaSalonSocialEntity> findByUsuario_NumeroDocumento(String numeroDocumento);
    boolean existsBySalon_IdAndFechaYHoraReserva(UUID idSalon, LocalDateTime fechaYHora);
    List<ReservaSalonSocialEntity> findByFechaYHoraReservaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<ReservaSalonSocialEntity> findBySalon_IdAndFechaYHoraReservaAfter(UUID idSalon, LocalDateTime ahora);
    List<ReservaSalonSocialEntity> findByUsuario_IdAndFechaYHoraReservaAfter(UUID idUsuario, LocalDateTime ahora);
    boolean existsBySalon_IdAndFechaYHoraReservaBetween(UUID idSalon, LocalDateTime inicioDia, LocalDateTime finDia);
}
