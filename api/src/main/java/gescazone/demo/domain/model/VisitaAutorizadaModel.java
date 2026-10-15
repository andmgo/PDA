package gescazone.demo.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Visita que un propietario autoriza de antemano para uno de sus
 * apartamentos, en un rango de fechas. Portería la ve como "esperada" esos
 * días y registra el ingreso sin llamar al propietario. Una misma
 * autorización sirve para varios ingresos dentro del rango (p. ej. un
 * familiar que viene el fin de semana).
 */
public class VisitaAutorizadaModel {

    /** Máximo de días que puede cubrir una autorización. */
    public static final int MAXIMO_DIAS = 30;
    /** Con cuánta anticipación se puede autorizar. */
    public static final int MAXIMO_DIAS_ANTICIPACION = 90;

    private String id;
    private String idApartamento;
    private String idAutorizadaPor;
    private String nombreVisitante;
    private String apellidoVisitante;
    private String numeroDocumento;
    private String placa;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    private String nota;
    private boolean cancelada;
    private LocalDateTime fechaCreacion;

    public VisitaAutorizadaModel() {}

    /** Valida las reglas de una autorización nueva. {@code hoy} se recibe para poder probarlo. */
    public void validarNueva(LocalDate hoy) {
        if (nombreVisitante == null || nombreVisitante.isBlank()
                || apellidoVisitante == null || apellidoVisitante.isBlank())
            throw new IllegalArgumentException("Escribe el nombre y el apellido de quien te visita");
        if (fechaDesde == null || fechaHasta == null)
            throw new IllegalArgumentException("Indica desde qué día y hasta qué día está autorizada la visita");
        if (fechaDesde.isBefore(hoy))
            throw new IllegalArgumentException("La visita no puede empezar en una fecha pasada");
        if (fechaHasta.isBefore(fechaDesde))
            throw new IllegalArgumentException("La fecha final no puede ser anterior a la inicial");
        if (ChronoUnit.DAYS.between(fechaDesde, fechaHasta) + 1 > MAXIMO_DIAS)
            throw new IllegalArgumentException("Una autorización puede cubrir máximo " + MAXIMO_DIAS + " días");
        if (ChronoUnit.DAYS.between(hoy, fechaDesde) > MAXIMO_DIAS_ANTICIPACION)
            throw new IllegalArgumentException("Solo se pueden autorizar visitas con hasta "
                    + MAXIMO_DIAS_ANTICIPACION + " días de anticipación");
        nombreVisitante = nombreVisitante.trim();
        apellidoVisitante = apellidoVisitante.trim();
        if (nota != null) nota = nota.isBlank() ? null : nota.trim();
    }

    /** ¿Sirve para dejar entrar a alguien en esta fecha? */
    public boolean vigenteEn(LocalDate fecha) {
        return !cancelada && !fecha.isBefore(fechaDesde) && !fecha.isAfter(fechaHasta);
    }

    public boolean vencida(LocalDate hoy) {
        return fechaHasta.isBefore(hoy);
    }

    public void cancelar(LocalDate hoy) {
        if (cancelada) throw new IllegalStateException("Esta autorización ya estaba cancelada");
        if (vencida(hoy)) throw new IllegalStateException("Esta autorización ya venció");
        cancelada = true;
    }

    public String nombreCompleto() {
        return nombreVisitante + " " + apellidoVisitante;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }

    public String getIdAutorizadaPor() { return idAutorizadaPor; }
    public void setIdAutorizadaPor(String idAutorizadaPor) { this.idAutorizadaPor = idAutorizadaPor; }

    public String getNombreVisitante() { return nombreVisitante; }
    public void setNombreVisitante(String nombreVisitante) { this.nombreVisitante = nombreVisitante; }

    public String getApellidoVisitante() { return apellidoVisitante; }
    public void setApellidoVisitante(String apellidoVisitante) { this.apellidoVisitante = apellidoVisitante; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa; }

    public LocalDate getFechaDesde() { return fechaDesde; }
    public void setFechaDesde(LocalDate fechaDesde) { this.fechaDesde = fechaDesde; }

    public LocalDate getFechaHasta() { return fechaHasta; }
    public void setFechaHasta(LocalDate fechaHasta) { this.fechaHasta = fechaHasta; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public boolean isCancelada() { return cancelada; }
    public void setCancelada(boolean cancelada) { this.cancelada = cancelada; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
