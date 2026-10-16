package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pagos")
public class PagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "cargo_id", nullable = false)
    private UUID cargoId;

    @Column(nullable = false)
    private BigDecimal valor;

    @Column(nullable = false)
    private String medio;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false, unique = true)
    private String referencia;

    private String soporte;

    @Column(name = "transaccion_externa")
    private String transaccionExterna;

    @Column(name = "pagado_por_usuario_id")
    private UUID pagadoPorUsuarioId;

    @Column(name = "registrado_por_usuario_id")
    private UUID registradoPorUsuarioId;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getCargoId() { return cargoId; }
    public void setCargoId(UUID cargoId) { this.cargoId = cargoId; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public String getMedio() { return medio; }
    public void setMedio(String medio) { this.medio = medio; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getSoporte() { return soporte; }
    public void setSoporte(String soporte) { this.soporte = soporte; }
    public String getTransaccionExterna() { return transaccionExterna; }
    public void setTransaccionExterna(String transaccionExterna) { this.transaccionExterna = transaccionExterna; }
    public UUID getPagadoPorUsuarioId() { return pagadoPorUsuarioId; }
    public void setPagadoPorUsuarioId(UUID pagadoPorUsuarioId) { this.pagadoPorUsuarioId = pagadoPorUsuarioId; }
    public UUID getRegistradoPorUsuarioId() { return registradoPorUsuarioId; }
    public void setRegistradoPorUsuarioId(UUID registradoPorUsuarioId) { this.registradoPorUsuarioId = registradoPorUsuarioId; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
}
