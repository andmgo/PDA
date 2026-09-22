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
@Table(name = "salones_sociales")
public class SalonSocialEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String numero;

    private String medidas;

    private Long telefono;

    // EAGER: catálogo minúsculo — ver ApartamentoEntity.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "estado_id")
    private EstadoEntity estado;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }

    public Long getTelefono() { return telefono; }
    public void setTelefono(Long telefono) { this.telefono = telefono; }

    public EstadoEntity getEstado() { return estado; }
    public void setEstado(EstadoEntity estado) { this.estado = estado; }
}
