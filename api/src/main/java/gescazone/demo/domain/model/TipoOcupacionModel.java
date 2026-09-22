package gescazone.demo.domain.model;

public class TipoOcupacionModel {

    private String nombreTipoOcupacion;

    public TipoOcupacionModel() {}
    public TipoOcupacionModel(String nombreTipoOcupacion) { this.nombreTipoOcupacion = nombreTipoOcupacion; }

    public String getNombreTipoOcupacion() { return nombreTipoOcupacion; }
    public void setNombreTipoOcupacion(String nombreTipoOcupacion) { this.nombreTipoOcupacion = nombreTipoOcupacion; }
}