package gescazone.demo.domain.model;

public class RegistroVisitanteModel {

    private String id;
    private String idResidente;
    private String idApartamento;
    private String fechaHoraEntrada;
    private String fechaHoraSalida;

    public RegistroVisitanteModel() {}

    public RegistroVisitanteModel(String idResidente, String idApartamento, String fechaHoraEntrada) {
        this.idResidente = idResidente;
        this.idApartamento = idApartamento;
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public RegistroVisitanteModel(String idResidente, String idApartamento,
                                  String fechaHoraEntrada, String fechaHoraSalida) {
        this(idResidente, idApartamento, fechaHoraEntrada);
        this.fechaHoraSalida = fechaHoraSalida;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdResidente() { return idResidente; }
    public void setIdResidente(String idResidente) { this.idResidente = idResidente; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(String fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public String getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(String fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public void registrarSalida(String fechaHoraSalida) {
        if (fechaHoraSalida == null || fechaHoraSalida.trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de salida no puede estar vacía");
        if (this.fechaHoraSalida != null && !this.fechaHoraSalida.trim().isEmpty())
            throw new IllegalStateException("Este registro ya tiene una salida registrada");
        this.fechaHoraSalida = fechaHoraSalida.trim();
    }

    public boolean isActivo() {
        return fechaHoraSalida == null || fechaHoraSalida.trim().isEmpty();
    }

    public void limpiarSalida() { this.fechaHoraSalida = null; }

    @Override
    public String toString() {
        return "RegistroVisitante{" +
                "idResidente='" + idResidente + '\'' +
                ", idApartamento='" + idApartamento + '\'' +
                ", entrada='" + fechaHoraEntrada + '\'' +
                ", salida='" + (fechaHoraSalida != null ? fechaHoraSalida : "AÚN EN EL EDIFICIO") + '\'' +
                '}';
    }
}