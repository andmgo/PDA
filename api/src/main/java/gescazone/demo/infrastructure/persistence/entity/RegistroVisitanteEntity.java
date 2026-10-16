package gescazone.demo.infrastructure.persistence.entity;

import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "registro_visitante")
public class RegistroVisitanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residente_id", nullable = false)
    private ResidenteEntity residente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @Column(name = "fecha_hora_entrada")
    private LocalDateTime fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private LocalDateTime fechaHoraSalida;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ResidenteEntity getResidente() { return residente; }
    public void setResidente(ResidenteEntity residente) { this.residente = residente; }

    public ApartamentoEntity getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoEntity apartamento) { this.apartamento = apartamento; }

    public LocalDateTime getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(LocalDateTime fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public LocalDateTime getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(LocalDateTime fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    @Column(name = "visita_autorizada_id")
    private UUID visitaAutorizadaId;

    @Column(name = "registrado_por_usuario_id")
    private UUID registradoPorUsuarioId;

    public UUID getVisitaAutorizadaId() { return visitaAutorizadaId; }
    public void setVisitaAutorizadaId(UUID visitaAutorizadaId) { this.visitaAutorizadaId = visitaAutorizadaId; }

    public UUID getRegistradoPorUsuarioId() { return registradoPorUsuarioId; }
    public void setRegistradoPorUsuarioId(UUID registradoPorUsuarioId) { this.registradoPorUsuarioId = registradoPorUsuarioId; }

    @Column(name = "autorizado_por")
    private String autorizadoPor;

    public String getAutorizadoPor() { return autorizadoPor; }
    public void setAutorizadoPor(String autorizadoPor) { this.autorizadoPor = autorizadoPor; }
}
