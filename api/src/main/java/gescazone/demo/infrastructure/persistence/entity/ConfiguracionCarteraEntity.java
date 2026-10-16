package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/** Una sola fila (id = 1), ver V17__pagos_y_cartera.sql. */
@Entity
@Table(name = "configuracion_cartera")
public class ConfiguracionCarteraEntity {

    @Id
    private Short id = 1;

    @Column(name = "cuota_administracion", nullable = false)
    private BigDecimal cuotaAdministracion;

    @Column(name = "valor_reserva", nullable = false)
    private BigDecimal valorReserva;

    @Column(name = "dia_vencimiento", nullable = false)
    private short diaVencimiento;

    public Short getId() { return id; }
    public void setId(Short id) { this.id = id; }
    public BigDecimal getCuotaAdministracion() { return cuotaAdministracion; }
    public void setCuotaAdministracion(BigDecimal cuotaAdministracion) { this.cuotaAdministracion = cuotaAdministracion; }
    public BigDecimal getValorReserva() { return valorReserva; }
    public void setValorReserva(BigDecimal valorReserva) { this.valorReserva = valorReserva; }
    public short getDiaVencimiento() { return diaVencimiento; }
    public void setDiaVencimiento(short diaVencimiento) { this.diaVencimiento = diaVencimiento; }
}
