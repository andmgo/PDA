package gescazone.demo.domain.model;

public class ResidenteModel {

    private String id;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private Long celular;
    private TipoDocumentoModel tipoDocumento;
    private TipoResidenteModel tipoResidente;
    private boolean activo = true;

    public ResidenteModel() {}

    /**
     * Forma en que se guarda y se busca un documento: mayúsculas, sin puntos,
     * guiones ni espacios ("1.234.567" → "1234567", "ab 123" → "AB123").
     * Antes era INTEGER: no cabían documentos de más de 10 dígitos ni
     * pasaportes con letras. Devuelve null si queda vacío.
     */
    public static String normalizarDocumento(Object valor) {
        if (valor == null) return null;
        String limpio = valor.toString().toUpperCase().replaceAll("[^A-Z0-9]", "");
        return limpio.isEmpty() ? null : limpio;
    }

    public ResidenteModel(String numeroDocumento, String nombre, String apellido,
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

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

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

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}