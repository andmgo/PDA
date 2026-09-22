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
@Table(name = "paquetes")
public class PaqueteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @Column(name = "nombre_receptor", nullable = false)
    private String nombreReceptor;

    @Column(name = "cedula_receptor", nullable = false)
    private String cedulaReceptor;

    @Column(name = "fecha_hora_llegada", nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Column(nullable = false)
    private boolean entregado;

    @Column(name = "fecha_hora_entrega")
    private LocalDateTime fechaHoraEntrega;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ApartamentoEntity getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoEntity apartamento) { this.apartamento = apartamento; }

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
}
