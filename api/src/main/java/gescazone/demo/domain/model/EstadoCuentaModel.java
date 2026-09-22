package gescazone.demo.domain.model;

public class EstadoCuentaModel {

    private String nombreEstadoCuenta;

    public EstadoCuentaModel() {}
    public EstadoCuentaModel(String nombreEstadoCuenta) { this.nombreEstadoCuenta = nombreEstadoCuenta; }

    public String getNombreEstadoCuenta() { return nombreEstadoCuenta; }
    public void setNombreEstadoCuenta(String nombreEstadoCuenta) { this.nombreEstadoCuenta = nombreEstadoCuenta; }
}