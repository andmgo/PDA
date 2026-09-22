package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tipos_residente")
public class TipoResidenteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_tipo_residente", nullable = false, unique = true)
    private String nombreTipoResidente;

    public TipoResidenteEntity() {}
    public TipoResidenteEntity(String nombreTipoResidente) { this.nombreTipoResidente = nombreTipoResidente; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreTipoResidente() { return nombreTipoResidente; }
    public void setNombreTipoResidente(String nombreTipoResidente) { this.nombreTipoResidente = nombreTipoResidente; }
}
