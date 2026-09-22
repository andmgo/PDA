package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "estados")
public class EstadoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_estado", nullable = false, unique = true)
    private String nombreEstado;

    public EstadoEntity() {}
    public EstadoEntity(String nombreEstado) { this.nombreEstado = nombreEstado; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }
}
