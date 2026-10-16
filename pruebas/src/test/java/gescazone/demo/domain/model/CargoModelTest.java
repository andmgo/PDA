package gescazone.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Cargo (cobro a un apartamento)")
class CargoModelTest {

    private static final BigDecimal CUOTA = new BigDecimal("350000");
    private static final LocalDate VENCE = LocalDate.of(2026, 10, 10);

    private CargoModel cuota() {
        return CargoModel.cuotaAdministracion("apto-1", "2026-10", CUOTA, VENCE);
    }

    @Nested
    @DisplayName("al crearlo")
    class Creacion {

        @Test
        @DisplayName("la cuota de administración lleva periodo y descripción legible")
        void cuotaDeAdministracion() {
            CargoModel c = cuota();
            assertThat(c.getConcepto()).isEqualTo(CargoModel.Concepto.ADMINISTRACION);
            assertThat(c.getPeriodo()).isEqualTo("2026-10");
            assertThat(c.getDescripcion()).isEqualTo("Administración octubre 2026");
            assertThat(c.isAnulado()).isFalse();
            assertThat(c.getFechaCreacion()).isNotNull();
        }

        @Test
        @DisplayName("el cobro de una reserva queda ligado a la reserva")
        void cobroDeReserva() {
            CargoModel c = CargoModel.reserva("apto-1", "res-9", "Reserva salón S-10", new BigDecimal("80000"), VENCE);
            assertThat(c.getConcepto()).isEqualTo(CargoModel.Concepto.RESERVA);
            assertThat(c.getIdReserva()).isEqualTo("res-9");
            assertThat(c.getPeriodo()).isNull();
        }

        @ParameterizedTest(name = "valor {0} → rechazado")
        @ValueSource(strings = {"0", "-1", "-350000"})
        @DisplayName("no acepta valores en cero o negativos")
        void valorInvalido(String valor) {
            assertThatThrownBy(() -> CargoModel.cuotaAdministracion("apto-1", "2026-10", new BigDecimal(valor), VENCE))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("mayor que cero");
        }

        @Test
        @DisplayName("no acepta valor nulo")
        void valorNulo() {
            assertThatThrownBy(() -> CargoModel.cuotaAdministracion("apto-1", "2026-10", null, VENCE))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("saldo")
    class Saldo {

        @ParameterizedTest(name = "pagado {0} → saldo {1}")
        @CsvSource({"0, 350000", "100000, 250000", "350000, 0", "400000, 0"})
        @DisplayName("es el valor menos lo pagado, nunca negativo")
        void valorMenosPagado(String pagado, String esperado) {
            assertThat(cuota().saldo(new BigDecimal(pagado))).isEqualByComparingTo(esperado);
        }

        @Test
        @DisplayName("sin pagos (null) el saldo es el valor completo")
        void sinPagos() {
            assertThat(cuota().saldo(null)).isEqualByComparingTo(CUOTA);
        }

        @Test
        @DisplayName("un cargo anulado no debe nada")
        void anulado() {
            CargoModel c = cuota();
            c.anular();
            assertThat(c.isAnulado()).isTrue();
            assertThat(c.saldo(BigDecimal.ZERO)).isZero();
        }
    }

    @Nested
    @DisplayName("vencimiento")
    class Vencimiento {

        @Test
        @DisplayName("vence si pasó la fecha y queda saldo")
        void vencido() {
            assertThat(cuota().vencido(VENCE.plusDays(1), BigDecimal.ZERO)).isTrue();
        }

        @Test
        @DisplayName("el mismo día del vencimiento todavía no está vencido")
        void elMismoDia() {
            assertThat(cuota().vencido(VENCE, BigDecimal.ZERO)).isFalse();
        }

        @Test
        @DisplayName("pagado completo nunca queda vencido")
        void pagado() {
            assertThat(cuota().vencido(VENCE.plusMonths(3), CUOTA)).isFalse();
        }
    }

    @ParameterizedTest(name = "{0} → {1}")
    @CsvSource({"2026-01, enero 2026", "2026-10, octubre 2026", "2027-12, diciembre 2027"})
    @DisplayName("el periodo se muestra en español")
    void periodoLegible(String periodo, String esperado) {
        assertThat(CargoModel.periodoLegible(periodo)).isEqualTo(esperado);
    }

    @ParameterizedTest(name = "\"{0}\" se deja igual")
    @ValueSource(strings = {"octubre", "2026/10", ""})
    @DisplayName("un periodo con formato inesperado no rompe")
    void periodoRaro(String periodo) {
        assertThat(CargoModel.periodoLegible(periodo)).isEqualTo(periodo);
    }
}
