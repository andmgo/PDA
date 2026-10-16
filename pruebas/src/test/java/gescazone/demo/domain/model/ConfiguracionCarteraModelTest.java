package gescazone.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Configuración de la cartera")
class ConfiguracionCarteraModelTest {

    private ConfiguracionCarteraModel conf(String cuota, String reserva, int dia) {
        ConfiguracionCarteraModel c = new ConfiguracionCarteraModel();
        c.setCuotaAdministracion(new BigDecimal(cuota));
        c.setValorReserva(new BigDecimal(reserva));
        c.setDiaVencimiento(dia);
        return c;
    }

    @Test
    @DisplayName("valores válidos pasan la validación")
    void valida() {
        assertThatCode(() -> conf("350000", "80000", 10).validar()).doesNotThrowAnyException();
        assertThatCode(() -> conf("0", "0", 1).validar()).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "día {0} → rechazado")
    @ValueSource(ints = {0, 29, 31, -1})
    @DisplayName("el día de vencimiento va de 1 a 28 (existe en todos los meses)")
    void diaFueraDeRango(int dia) {
        assertThatThrownBy(() -> conf("350000", "0", dia).validar())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 y 28");
    }

    @Test
    @DisplayName("ni la cuota ni la reserva pueden ser negativas")
    void negativos() {
        assertThatThrownBy(() -> conf("-1", "0", 10).validar()).hasMessageContaining("cuota");
        assertThatThrownBy(() -> conf("0", "-1", 10).validar()).hasMessageContaining("reserva");
    }

    @ParameterizedTest(name = "{0}, día {1} → {2}")
    @CsvSource({"2026-10, 10, 2026-10-10", "2027-02, 28, 2027-02-28", "2026-12, 1, 2026-12-01"})
    @DisplayName("la cuota de un mes vence el día configurado")
    void vencimiento(String periodo, int dia, String esperado) {
        assertThat(conf("1", "0", dia).vencimientoDe(periodo)).isEqualTo(LocalDate.parse(esperado));
    }
}
