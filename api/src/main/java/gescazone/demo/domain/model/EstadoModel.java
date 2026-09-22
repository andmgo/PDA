package gescazone.demo.domain.model;

public class EstadoModel {

    private String nombreEstado;

    public EstadoModel() {}
    public EstadoModel(String nombreEstado) { this.nombreEstado = nombreEstado; }

    public String getNombreEstado() { return nombreEstado; }
    public void setNombreEstado(String nombreEstado) { this.nombreEstado = nombreEstado; }
}