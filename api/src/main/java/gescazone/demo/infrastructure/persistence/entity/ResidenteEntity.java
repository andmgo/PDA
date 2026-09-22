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
@Table(name = "residentes")
public class ResidenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero_documento", nullable = false, unique = true)
    private Integer numeroDocumento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    private Long celular;

    // EAGER: catálogos minúsculos — ver ApartamentoEntity.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_documento_id")
    private TipoDocumentoEntity tipoDocumento;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_residente_id")
    private TipoResidenteEntity tipoResidente;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(Integer numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Long getCelular() { return celular; }
    public void setCelular(Long celular) { this.celular = celular; }

    public TipoDocumentoEntity getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoEntity tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public TipoResidenteEntity getTipoResidente() { return tipoResidente; }
    public void setTipoResidente(TipoResidenteEntity tipoResidente) { this.tipoResidente = tipoResidente; }
}
