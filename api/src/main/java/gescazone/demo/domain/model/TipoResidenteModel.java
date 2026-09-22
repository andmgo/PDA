package gescazone.demo.domain.model;

public class TipoResidenteModel {

    private String nombreTipoResidente;

    public TipoResidenteModel() {}
    public TipoResidenteModel(String nombreTipoResidente) { this.nombreTipoResidente = nombreTipoResidente; }

    public String getNombreTipoResidente() { return nombreTipoResidente; }
    public void setNombreTipoResidente(String nombreTipoResidente) { this.nombreTipoResidente = nombreTipoResidente; }
}