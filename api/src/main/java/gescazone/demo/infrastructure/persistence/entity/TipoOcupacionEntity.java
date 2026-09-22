package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "tipos_ocupacion")
public class TipoOcupacionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_tipo_ocupacion", nullable = false, unique = true)
    private String nombreTipoOcupacion;

    public TipoOcupacionEntity() {}
    public TipoOcupacionEntity(String nombreTipoOcupacion) { this.nombreTipoOcupacion = nombreTipoOcupacion; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreTipoOcupacion() { return nombreTipoOcupacion; }
    public void setNombreTipoOcupacion(String nombreTipoOcupacion) { this.nombreTipoOcupacion = nombreTipoOcupacion; }
}
