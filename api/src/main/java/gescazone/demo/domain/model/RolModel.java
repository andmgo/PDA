package gescazone.demo.domain.model;

// Dominio puro: el mapeo a la tabla "roles" vive en infrastructure/persistence.
public class RolModel {

    private String nombreRol;

    public RolModel() {}
    public RolModel(String nombreRol) { this.nombreRol = nombreRol; }

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }
}