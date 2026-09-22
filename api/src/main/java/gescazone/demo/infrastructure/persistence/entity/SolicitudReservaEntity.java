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
@Table(name = "solicitud_reserva_excepcional")
public class SolicitudReservaEntity {

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

    @Column(name = "fecha_solicitada", nullable = false)
    private LocalDateTime fechaSolicitada;

    private String justificacion;

    @Column(nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_usuario_id")
    private UsuarioEntity revisadoPor;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UsuarioEntity getUsuario() { return usuario; }
    public void setUsuario(UsuarioEntity usuario) { this.usuario = usuario; }

    public SalonSocialEntity getSalon() { return salon; }
    public void setSalon(SalonSocialEntity salon) { this.salon = salon; }

    public LocalDateTime getFechaSolicitada() { return fechaSolicitada; }
    public void setFechaSolicitada(LocalDateTime fechaSolicitada) { this.fechaSolicitada = fechaSolicitada; }

    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public UsuarioEntity getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(UsuarioEntity revisadoPor) { this.revisadoPor = revisadoPor; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
