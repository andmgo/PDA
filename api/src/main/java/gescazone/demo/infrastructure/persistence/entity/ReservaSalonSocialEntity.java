package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "reserva_salon_social")
public class ReservaSalonSocialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioEntity usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salon_id", nullable = false)
    private SalonSocialEntity salon;

    @Column(name = "fecha_y_hora_reserva", nullable = false)
    private LocalDateTime fechaYHoraReserva;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }

    public SalonSocialEntity getSalon() { return salon; }
    public void setSalon(SalonSocialEntity salon) { this.salon = salon; }

    public LocalDateTime getFechaYHoraReserva() { return fechaYHoraReserva; }
    public void setFechaYHoraReserva(LocalDateTime fechaYHoraReserva) { this.fechaYHoraReserva = fechaYHoraReserva; }
}
