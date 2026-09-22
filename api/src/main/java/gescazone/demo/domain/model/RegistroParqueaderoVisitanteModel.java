package gescazone.demo.domain.model;

public class RegistroParqueaderoVisitanteModel {

    private String id;
    private String idResidente;
    private String idParqueadero;
    private String idApartamento;
    private String fechaHoraEntrada;
    private String fechaHoraSalida;
    private String placa;

    public RegistroParqueaderoVisitanteModel() {}

    public RegistroParqueaderoVisitanteModel(String idResidente, String idParqueadero, String fechaHoraEntrada) {
        this.idResidente = idResidente;
        this.idParqueadero = idParqueadero;
        this.fechaHoraEntrada = fechaHoraEntrada;
    }

    public RegistroParqueaderoVisitanteModel(String idResidente, String idParqueadero,
                                             String fechaHoraEntrada, String placa) {
        this(idResidente, idParqueadero, fechaHoraEntrada);
        this.placa = placa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdResidente() { return idResidente; }
    public void setIdResidente(String idResidente) { this.idResidente = idResidente; }

    public String getIdParqueadero() { return idParqueadero; }
    public void setIdParqueadero(String idParqueadero) { this.idParqueadero = idParqueadero; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getFechaHoraEntrada() { return fechaHoraEntrada; }
    public void setFechaHoraEntrada(String fechaHoraEntrada) { this.fechaHoraEntrada = fechaHoraEntrada; }

    public String getFechaHoraSalida() { return fechaHoraSalida; }
    public void setFechaHoraSalida(String fechaHoraSalida) { this.fechaHoraSalida = fechaHoraSalida; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

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

    public void limpiarSalida() {
        this.fechaHoraSalida = null;
    }

    @Override
    public String toString() {
        return "RegistroParqueo{" +
                "idResidente='" + idResidente + '\'' +
                ", idParqueadero='" + idParqueadero + '\'' +
                ", idApartamento='" + idApartamento + '\'' +
                ", placa='" + placa + '\'' +
                ", entrada='" + fechaHoraEntrada + '\'' +
                ", salida='" + (fechaHoraSalida != null ? fechaHoraSalida : "AÚN EN PARQUEADERO") + '\'' +
                '}';
    }
}