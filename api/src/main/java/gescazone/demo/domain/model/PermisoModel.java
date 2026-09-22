package gescazone.demo.domain.model;

// Dominio puro: catálogo fijo de funcionalidades permisables (no lo edita el
// admin). El mapeo a la tabla "permisos" vive en infrastructure/persistence.
public class PermisoModel {

    private String id;
    private String codigo;
    private String nombre;
    private String modulo;

    public PermisoModel() {}

    public PermisoModel(String codigo, String nombre, String modulo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.modulo = modulo;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getModulo() { return modulo; }
    public void setModulo(String modulo) { this.modulo = modulo; }
}
