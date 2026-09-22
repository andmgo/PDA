package gescazone.demo.domain.model;

import java.time.LocalDateTime;

public class RegistroAccesoPiscinaModel {

    private String id;
    private String idApartamento;
    private String idResidente;
    private LocalDateTime fechaHora;

    public RegistroAccesoPiscinaModel() { this.fechaHora = LocalDateTime.now(); }

    public RegistroAccesoPiscinaModel(String idApartamento, String idResidente) {
        this.idApartamento = idApartamento;
        this.idResidente = idResidente;
        this.fechaHora = LocalDateTime.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getIdResidente() { return idResidente; }
    public void setIdResidente(String idResidente) { this.idResidente = idResidente; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}