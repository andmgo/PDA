package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "estados_cuenta")
public class EstadoCuentaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "nombre_estado_cuenta", nullable = false, unique = true)
    private String nombreEstadoCuenta;

    public EstadoCuentaEntity() {}
    public EstadoCuentaEntity(String nombreEstadoCuenta) { this.nombreEstadoCuenta = nombreEstadoCuenta; }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombreEstadoCuenta() { return nombreEstadoCuenta; }
    public void setNombreEstadoCuenta(String nombreEstadoCuenta) { this.nombreEstadoCuenta = nombreEstadoCuenta; }
}
