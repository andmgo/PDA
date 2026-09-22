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

import java.util.UUID;

@Entity
@Table(name = "registro_parqueadero_visitante")
public class RegistroParqueaderoVisitanteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "residente_id", nullable = false)
    private ResidenteEntity residente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parqueadero_id", nullable = false)
    private ParqueaderoEntity parqueadero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "apartamento_id")
    private ApartamentoEntity apartamento;

    private String placa;

    @Column(name = "fecha_hora_entrada")
    private String fechaHoraEntrada;

    @Column(name = "fecha_hora_salida")
    private String fechaHoraSalida;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public ResidenteEntity getResidente() { return residente; }
    public void setResidente(ResidenteEntity residente) { this.residente = residente; }

    public ParqueaderoEntity getParqueadero() { return parqueadero; }
    public void setParqueadero(ParqueaderoEntity parqueadero) { this.parqueadero = parqueadero; }

    public ApartamentoEntity getApartamento() { return apartamento; }
    public void setApartamento(ApartamentoEntity apartamento) { this.apartamento = apartamento; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public String getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(String fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public String getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(String fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }
}
