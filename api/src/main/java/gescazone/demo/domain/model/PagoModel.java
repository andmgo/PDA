package gescazone.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Abono a un cargo. En línea (Wompi) nace PENDIENTE y la pasarela lo
 * aprueba o rechaza; un pago manual de la administración nace APROBADO.
 */
public class PagoModel {

    public enum Medio { WOMPI, TRANSFERENCIA, CONSIGNACION, EFECTIVO }

    public enum Estado { PENDIENTE, APROBADO, RECHAZADO }

    private String id;
    private String idCargo;
    private BigDecimal valor;
    private Medio medio;
    private Estado estado;
    private String referencia;
    private String soporte;
    private String transaccionExterna;
    private String idPagadoPor;
    private String idRegistradoPor;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaAprobacion;

    public PagoModel() {}

    /** Pago en línea: queda pendiente hasta que la pasarela confirme. */
    public static PagoModel enLinea(String idCargo, BigDecimal valor, String idPagadoPor) {
        PagoModel p = base(idCargo, valor, Medio.WOMPI);
        p.estado = Estado.PENDIENTE;
        p.idPagadoPor = idPagadoPor;
        return p;
    }

    /** Pago registrado por la administración con su comprobante: aprobado de una vez. */
    public static PagoModel manual(String idCargo, BigDecimal valor, Medio medio, String soporte, String idRegistradoPor) {
        if (medio == null || medio == Medio.WOMPI)
            throw new IllegalArgumentException("Elige cómo pagó: transferencia, consignación o efectivo");
        if (medio != Medio.EFECTIVO && (soporte == null || soporte.isBlank()))
            throw new IllegalArgumentException("Escribe el número del comprobante de la " + medio.name().toLowerCase());
        PagoModel p = base(idCargo, valor, medio);
        p.soporte = soporte == null || soporte.isBlank() ? null : soporte.trim();
        p.idRegistradoPor = idRegistradoPor;
        p.estado = Estado.APROBADO;
        p.fechaAprobacion = LocalDateTime.now();
        return p;
    }

    private static PagoModel base(String idCargo, BigDecimal valor, Medio medio) {
        if (valor == null || valor.signum() <= 0)
            throw new IllegalArgumentException("El valor del pago debe ser mayor que cero");
        PagoModel p = new PagoModel();
        p.idCargo = idCargo;
        p.valor = valor;
        p.medio = medio;
        p.referencia = "AUR-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
        p.fechaCreacion = LocalDateTime.now();
        return p;
    }

    /** Respuesta de la pasarela; solo cambia un pago que sigue pendiente. */
    public void resolver(boolean aprobado, String transaccionExterna) {
        if (estado != Estado.PENDIENTE) return;
        this.estado = aprobado ? Estado.APROBADO : Estado.RECHAZADO;
        this.transaccionExterna = transaccionExterna;
        if (aprobado) this.fechaAprobacion = LocalDateTime.now();
    }

    /** Valor en centavos, como lo pide la pasarela. */
    public long valorEnCentavos() {
        return valor.movePointRight(2).longValueExact();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getIdCargo() { return idCargo; }
    public void setIdCargo(String idCargo) { this.idCargo = idCargo; }
    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }
    public Medio getMedio() { return medio; }
    public void setMedio(Medio medio) { this.medio = medio; }
    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public String getSoporte() { return soporte; }
    public void setSoporte(String soporte) { this.soporte = soporte; }
    public String getTransaccionExterna() { return transaccionExterna; }
    public void setTransaccionExterna(String transaccionExterna) { this.transaccionExterna = transaccionExterna; }
    public String getIdPagadoPor() { return idPagadoPor; }
    public void setIdPagadoPor(String idPagadoPor) { this.idPagadoPor = idPagadoPor; }
    public String getIdRegistradoPor() { return idRegistradoPor; }
    public void setIdRegistradoPor(String idRegistradoPor) { this.idRegistradoPor = idRegistradoPor; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public LocalDateTime getFechaAprobacion() { return fechaAprobacion; }
    public void setFechaAprobacion(LocalDateTime fechaAprobacion) { this.fechaAprobacion = fechaAprobacion; }
}
