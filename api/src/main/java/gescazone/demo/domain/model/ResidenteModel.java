package gescazone.demo.domain.model;

public class ResidenteModel {

    private String id;
    private Integer numeroDocumento;
    private String nombre;
    private String apellido;
    private Long celular;
    private TipoDocumentoModel tipoDocumento;
    private TipoResidenteModel tipoResidente;

    public ResidenteModel() {}

    public ResidenteModel(Integer numeroDocumento, String nombre, String apellido,
                         Long celular, TipoDocumentoModel tipoDocumento, TipoResidenteModel tipoResidente) {
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.celular = celular;
        this.tipoDocumento = tipoDocumento;
        this.tipoResidente = tipoResidente;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Integer getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(Integer numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public Long getCelular() { return celular; }
    public void setCelular(Long celular) { this.celular = celular; }

    public TipoDocumentoModel getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(TipoDocumentoModel tipoDocumento) { this.tipoDocumento = tipoDocumento; }

    public TipoResidenteModel getTipoResidente() { return tipoResidente; }
    public void setTipoResidente(TipoResidenteModel tipoResidente) { this.tipoResidente = tipoResidente; }
}