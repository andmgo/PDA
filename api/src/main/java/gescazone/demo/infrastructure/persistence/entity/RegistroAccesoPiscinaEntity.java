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
@Table(name = "registro_acceso_piscina")
public class RegistroAccesoPiscinaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartamento_id", nullable = false)
    private ApartamentoEntity apartamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residente_id", nullable = false)
    private ResidenteEntity residente;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ApartamentoEntity getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoEntity apartamento) { this.apartamento = apartamento; }

    public ResidenteEntity getResidente() { return residente; }
    public void setResidente(ResidenteEntity residente) { this.residente = residente; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
}
