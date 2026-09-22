package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tipos_documento")
public class TipoDocumentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_tipo_documento", nullable = false, unique = true)
    private String nombreTipoDocumento;

    public TipoDocumentoEntity() {}
    public TipoDocumentoEntity(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreTipoDocumento() { return nombreTipoDocumento; }
    public void setNombreTipoDocumento(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }
}
