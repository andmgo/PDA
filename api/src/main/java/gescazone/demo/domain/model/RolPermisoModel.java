package gescazone.demo.domain.model;

// Una celda de la matriz rol x permiso: qué puede hacer un rol sobre un
// permiso concreto. Es lo que la pantalla "Roles y permisos" lee y edita.
// Se identifica por nombres naturales (nombreRol/codigoPermiso), no por id
// interno — igual que RolModel, que tampoco expone un id.
public class RolPermisoModel {

    private String nombreRol;
    private String codigoPermiso;
    private boolean puedeVer;
    private boolean puedeEditar;

    public RolPermisoModel() {}

    public String getNombreRol() { return nombreRol; }
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    public String getCodigoPermiso() { return codigoPermiso; }
    public void setCodigoPermiso(String codigoPermiso) { this.codigoPermiso = codigoPermiso; }

    public boolean isPuedeVer() { return puedeVer; }
    public void setPuedeVer(boolean puedeVer) { this.puedeVer = puedeVer; }

    public boolean isPuedeEditar() { return puedeEditar; }
    public void setPuedeEditar(boolean puedeEditar) { this.puedeEditar = puedeEditar; }
}
