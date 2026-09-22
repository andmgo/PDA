package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "roles")
public class RolEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_rol", nullable = false, unique = true)
    private String nombreRol;

    public RolEntity() {}
    public RolEntity(String nombreRol) { this.nombreRol = nombreRol; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
}
