package gescazone.demo.domain.model;

import java.time.LocalDateTime;

/** Paquete que llega a recepción — lo registra un funcionario (o admin). */
public class PaqueteModel {

    private String id;
    private String idApartamento;
    private String nombreReceptor;
    private String cedulaReceptor;
    private LocalDateTime fechaHoraLlegada;
    private boolean entregado;
    private LocalDateTime fechaHoraEntrega;

    public PaqueteModel() {}

    public PaqueteModel(String idApartamento, String nombreReceptor, String cedulaReceptor) {
        this.idApartamento = idApartamento;
        this.nombreReceptor = nombreReceptor;
        this.cedulaReceptor = cedulaReceptor;
        this.fechaHoraLlegada = LocalDateTime.now();
        this.entregado = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getNombreReceptor() { return nombreReceptor; }
    public void setNombreReceptor(String nombreReceptor) { this.nombreReceptor = nombreReceptor; }

    public String getCedulaReceptor() { return cedulaReceptor; }
    public void setCedulaReceptor(String cedulaReceptor) { this.cedulaReceptor = cedulaReceptor; }

    public LocalDateTime getFechaHoraLlegada() { return fechaHoraLlegada; }
    public void setFechaHoraLlegada(LocalDateTime fechaHoraLlegada) { this.fechaHoraLlegada = fechaHoraLlegada; }

    public boolean isEntregado() { return entregado; }
    public void setEntregado(boolean entregado) { this.entregado = entregado; }

    public LocalDateTime getFechaHoraEntrega() { return fechaHoraEntrega; }
    public void setFechaHoraEntrega(LocalDateTime fechaHoraEntrega) { this.fechaHoraEntrega = fechaHoraEntrega; }

    public void marcarEntregado() {
        if (this.entregado)
            throw new IllegalStateException("Este paquete ya fue marcado como entregado");
        this.entregado = true;
        this.fechaHoraEntrega = LocalDateTime.now();
    }
}
