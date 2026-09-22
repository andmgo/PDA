package gescazone.demo.domain.model;

public class ParqueaderoModel {

    private String id;
    private String numero;
    private String medidas;
    private Long telefono;
    private EstadoModel estado;

    public ParqueaderoModel() {}

    public ParqueaderoModel(String numero, String medidas, Long telefono, EstadoModel estado) {
        this.numero = numero;
        this.medidas = medidas;
        this.telefono = telefono;
        this.estado = estado;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public String getMedidas() { return medidas; }
    public void setMedidas(String medidas) { this.medidas = medidas; }

    public Long getTelefono() { return telefono; }
    public void setTelefono(Long telefono) { this.telefono = telefono; }

    public EstadoModel getEstado() { return estado; }
    public void setEstado(EstadoModel estado) { this.estado = estado; }
}