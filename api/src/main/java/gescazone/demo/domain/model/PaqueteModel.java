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
    /** "Caja de Amazon", "Sobre"… (opcional). */
    private String descripcion;
    /** Funcionario que lo recibió en portería (auditoría). */
    private String idRegistradoPor;
    /** Funcionario que lo entregó. */
    private String idEntregadoPor;
    /** Quién lo retiró de portería (puede ser distinto del destinatario). */
    private String entregadoA;

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

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getIdRegistradoPor() { return idRegistradoPor; }
    public void setIdRegistradoPor(String idRegistradoPor) { this.idRegistradoPor = idRegistradoPor; }

    public String getIdEntregadoPor() { return idEntregadoPor; }
    public void setIdEntregadoPor(String idEntregadoPor) { this.idEntregadoPor = idEntregadoPor; }

    public String getEntregadoA() { return entregadoA; }
    public void setEntregadoA(String entregadoA) { this.entregadoA = entregadoA; }

    public void marcarEntregado() {
        marcarEntregado(null, null);
    }

    /**
     * @param entregadoA      quién lo retiró; si viene vacío se asume el destinatario
     * @param idEntregadoPor  funcionario que lo entrega (auditoría, puede ser null)
     */
    public void marcarEntregado(String entregadoA, String idEntregadoPor) {
        if (this.entregado)
            throw new IllegalStateException("Este paquete ya fue marcado como entregado");
        this.entregado = true;
        this.fechaHoraEntrega = LocalDateTime.now();
        this.entregadoA = entregadoA == null || entregadoA.isBlank() ? nombreReceptor : entregadoA.trim();
        this.idEntregadoPor = idEntregadoPor;
    }
}
