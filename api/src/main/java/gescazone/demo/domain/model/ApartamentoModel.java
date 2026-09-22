package gescazone.demo.domain.model;

public class ApartamentoModel {

    private String id;
    private String numero;
    private String medidas;
    private Long telefono;
    private TipoOcupacionModel tipoOcupacion;
    private EstadoCuentaModel estadoCuenta;

    public ApartamentoModel() {}

    public ApartamentoModel(String numero, String medidas, Long telefono,
                            TipoOcupacionModel tipoOcupacion, EstadoCuentaModel estadoCuenta) {
        this.numero = numero;
        this.medidas = medidas;
        this.telefono = telefono;
        this.tipoOcupacion = tipoOcupacion;
        this.estadoCuenta = estadoCuenta;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }

    public Long getTelefono() { return telefono; }
    public void setTelefono(Long telefono) { this.telefono = telefono; }

    public TipoOcupacionModel getTipoOcupacion() { return tipoOcupacion; }
    public void setTipoOcupacion(TipoOcupacionModel tipoOcupacion) { this.tipoOcupacion = tipoOcupacion; }

    public EstadoCuentaModel getEstadoCuenta() { return estadoCuenta; }
    public void setEstadoCuenta(EstadoCuentaModel estadoCuenta) { this.estadoCuenta = estadoCuenta; }
}