package gescazone.demo.domain.model;

import java.time.LocalDateTime;

public class ReservaSalonSocialModel {

    private String id;
    private String idUsuario;
    private String idSalon;
    private LocalDateTime fechaYHoraReserva;

    public ReservaSalonSocialModel() {}

    public ReservaSalonSocialModel(String idUsuario, String idSalon, LocalDateTime fechaYHoraReserva) {
        this.idUsuario = idUsuario;
        this.idSalon = idSalon;
        this.fechaYHoraReserva = fechaYHoraReserva;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public String getIdSalon() { return idSalon; }
    public void setIdSalon(String idSalon) { this.idSalon = idSalon; }

    public LocalDateTime getFechaYHoraReserva() { return fechaYHoraReserva; }
    public void setFechaYHoraReserva(LocalDateTime fechaYHoraReserva) { this.fechaYHoraReserva = fechaYHoraReserva; }

    @Override
    public String toString() {
        return "ReservaSalonSocial{" +
                "id='" + id + '\'' +
                ", idUsuario='" + idUsuario + '\'' +
                ", idSalon='" + idSalon + '\'' +
                ", fecha=" + fechaYHoraReserva +
                '}';
    }
}