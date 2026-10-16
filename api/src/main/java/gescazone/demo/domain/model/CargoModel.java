package gescazone.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Lo que debe un apartamento: la cuota de administración de un mes, una
 * reserva del salón social u otro concepto. El saldo se calcula con los
 * pagos aprobados (ver PagoModel).
 */
public class CargoModel {

    public enum Concepto { ADMINISTRACION, RESERVA, OTRO }

    private String id;
    private String idApartamento;
    private Concepto concepto;
    private String descripcion;
    private String periodo; // "2026-10" (solo administración)
    private String idReserva;
    private BigDecimal valor;
    private LocalDate fechaVencimiento;
    private boolean anulado;
    private LocalDateTime fechaCreacion;

    public CargoModel() {}

    public static CargoModel cuotaAdministracion(String idApartamento, String periodo, BigDecimal valor, LocalDate vence) {
        CargoModel c = nuevo(idApartamento, Concepto.ADMINISTRACION, "Administración " + periodoLegible(periodo), valor, vence);
        c.periodo = periodo;
        return c;
    }

    public static CargoModel reserva(String idApartamento, String idReserva, String descripcion, BigDecimal valor, LocalDate vence) {
        CargoModel c = nuevo(idApartamento, Concepto.RESERVA, descripcion, valor, vence);
        c.idReserva = idReserva;
        return c;
    }

    private static CargoModel nuevo(String idApartamento, Concepto concepto, String descripcion, BigDecimal valor, LocalDate vence) {
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException("El valor del cobro debe ser mayor que cero");
        CargoModel c = new CargoModel();
        c.idApartamento = idApartamento;
        c.concepto = concepto;
        c.descripcion = descripcion;
        c.valor = valor;
        c.fechaVencimiento = vence;
        c.fechaCreacion = LocalDateTime.now();
        return c;
    }

    private static final String[] MESES = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio",
            "agosto", "septiembre", "octubre", "noviembre", "diciembre"};

    /** "2026-10" → "octubre 2026" */
    public static String periodoLegible(String periodo) {
        if (periodo == null || !periodo.matches("\\d{4}-\\d{2}")) return periodo;
        int mes = Integer.parseInt(periodo.substring(5));
        return MESES[mes - 1] + " " + periodo.substring(0, 4);
    }

    /** Saldo pendiente dado lo ya pagado (nunca negativo). */
    public BigDecimal saldo(BigDecimal pagado) {
        if (anulado) return BigDecimal.ZERO;
        BigDecimal s = valor.subtract(pagado == null ? BigDecimal.ZERO : pagado);
        return s.signum() < 0 ? BigDecimal.ZERO : s;
    }

    public boolean vencido(LocalDate hoy, BigDecimal pagado) {
        return saldo(pagado).signum() > 0 && fechaVencimiento.isBefore(hoy);
    }

    public void anular() {
        this.anulado = true;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdApartamento() { return idApartamento; }
    public void setIdApartamento(String idApartamento) { this.idApartamento = idApartamento; }
    public Concepto getConcepto() { return concepto; }
    public void setConcepto(Concepto concepto) { this.concepto = concepto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
    public String getIdReserva() { return idReserva; }
    public void setIdReserva(String idReserva) { this.idReserva = idReserva; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public LocalDate getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDate fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    public boolean isAnulado() { return anulado; }
    public void setAnulado(boolean anulado) { this.anulado = anulado; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}
