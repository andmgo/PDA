package gescazone.demo.domain.model;

import java.time.LocalDateTime;

/**
 * Solicitud de reserva excepcional (fuera del año actual) — la revisa un
 * administrador dentro de la app, sin envío de correo real. Ver
 * ReservaSalonSocialService.guardarReservaAprobada().
 */
public class SolicitudReservaModel {

    private String id;
    private String idUsuario;
    private String idSalon;
    private LocalDateTime fechaSolicitada;
    private String justificacion;
    private String estado; // PENDIENTE / APROBADA / RECHAZADA
    private String revisadoPorIdUsuario;
    private String motivoRechazo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRevision;

    public SolicitudReservaModel() {}

    public SolicitudReservaModel(String idUsuario, String idSalon, LocalDateTime fechaSolicitada, String justificacion) {
        this.idUsuario = idUsuario;
        this.idSalon = idSalon;
        this.fechaSolicitada = fechaSolicitada;
        this.justificacion = justificacion;
        this.estado = "PENDIENTE";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getIdSalon() { return idSalon; }
    public void setIdSalon(String idSalon) { this.idSalon = idSalon; }

    public LocalDateTime getFechaSolicitada() { return fechaSolicitada; }
    public void setFechaSolicitada(LocalDateTime fechaSolicitada) { this.fechaSolicitada = fechaSolicitada; }

    public String getJustificacion() { return justificacion; }
    public void setJustificacion(String justificacion) { this.justificacion = justificacion; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getRevisadoPorIdUsuario() { return revisadoPorIdUsuario; }
    public void setRevisadoPorIdUsuario(String revisadoPorIdUsuario) { this.revisadoPorIdUsuario = revisadoPorIdUsuario; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
