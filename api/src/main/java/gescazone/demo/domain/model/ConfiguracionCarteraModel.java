package gescazone.demo.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

/** Valores de la cartera que define la administración (una sola fila). */
public class ConfiguracionCarteraModel {

    private BigDecimal cuotaAdministracion = BigDecimal.ZERO;
    private BigDecimal valorReserva = BigDecimal.ZERO;
    private int diaVencimiento = 10;

    public void validar() {
        if (cuotaAdministracion == null || cuotaAdministracion.signum() < 0)
            throw new IllegalArgumentException("La cuota de administración no puede ser negativa");
        if (valorReserva == null || valorReserva.signum() < 0)
            throw new IllegalArgumentException("El valor de la reserva no puede ser negativo");
        if (diaVencimiento < 1 || diaVencimiento > 28)
            throw new IllegalArgumentException("El día de vencimiento debe estar entre 1 y 28");
    }

    /** Fecha en que vence la cuota de un mes ("2026-10" → 10 oct 2026). */
    public LocalDate vencimientoDe(String periodo) {
        return YearMonth.parse(periodo).atDay(diaVencimiento);
    }

    public BigDecimal getCuotaAdministracion() { return cuotaAdministracion; }
    public void setCuotaAdministracion(BigDecimal cuotaAdministracion) { this.cuotaAdministracion = cuotaAdministracion; }
    public BigDecimal getValorReserva() { return valorReserva; }
    public void setValorReserva(BigDecimal valorReserva) { this.valorReserva = valorReserva; }
    public int getDiaVencimiento() { return diaVencimiento; }
    public void setDiaVencimiento(int diaVencimiento) { this.diaVencimiento = diaVencimiento; }
}
