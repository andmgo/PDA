package gescazone.demo.domain.model;

public class TipoDocumentoModel {

    private String nombreTipoDocumento;

    public TipoDocumentoModel() {}
    public TipoDocumentoModel(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }

    public String getNombreTipoDocumento() { return nombreTipoDocumento; }
    public void setNombreTipoDocumento(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }
}