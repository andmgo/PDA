package gescazone.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Pago (abono a un cargo)")
class PagoModelTest {

    private static final BigDecimal VALOR = new BigDecimal("350000");

    @Nested
    @DisplayName("en línea (pasarela)")
    class EnLinea {

        @Test
        @DisplayName("nace pendiente, con referencia única AUR-…")
        void naceePendiente() {
            PagoModel p = PagoModel.enLinea("c1", VALOR, "u1");
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.PENDIENTE);
            assertThat(p.getMedio()).isEqualTo(PagoModel.Medio.WOMPI);
            assertThat(p.getReferencia()).matches("AUR-[0-9A-F]{16}");
            assertThat(p.getIdPagadoPor()).isEqualTo("u1");
            assertThat(p.getFechaAprobacion()).isNull();
        }

        @Test
        @DisplayName("dos pagos nunca comparten referencia")
        void referenciasDistintas() {
            assertThat(PagoModel.enLinea("c1", VALOR, "u1").getReferencia())
                    .isNotEqualTo(PagoModel.enLinea("c1", VALOR, "u1").getReferencia());
        }

        @Test
        @DisplayName("aprobado por la pasarela guarda la transacción y la fecha")
        void aprobado() {
            PagoModel p = PagoModel.enLinea("c1", VALOR, "u1");
            p.resolver(true, "tx-1");
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.APROBADO);
            assertThat(p.getTransaccionExterna()).isEqualTo("tx-1");
            assertThat(p.getFechaAprobacion()).isNotNull();
        }

        @Test
        @DisplayName("un pago ya resuelto no cambia (un evento repetido no lo revierte)")
        void yaResuelto() {
            PagoModel p = PagoModel.enLinea("c1", VALOR, "u1");
            p.resolver(true, "tx-1");
            p.resolver(false, "tx-2");
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.APROBADO);
            assertThat(p.getTransaccionExterna()).isEqualTo("tx-1");
        }

        @Test
        @DisplayName("rechazado no tiene fecha de aprobación")
        void rechazado() {
            PagoModel p = PagoModel.enLinea("c1", VALOR, "u1");
            p.resolver(false, "tx-1");
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.RECHAZADO);
            assertThat(p.getFechaAprobacion()).isNull();
        }

        @Test
        @DisplayName("el valor se envía en centavos")
        void centavos() {
            assertThat(PagoModel.enLinea("c1", VALOR, "u1").valorEnCentavos()).isEqualTo(35_000_000L);
        }
    }

    @Nested
    @DisplayName("manual (registrado por la administración)")
    class Manual {

        @ParameterizedTest(name = "{0}")
        @EnumSource(value = PagoModel.Medio.class, names = {"TRANSFERENCIA", "CONSIGNACION"})
        @DisplayName("transferencia y consignación quedan aprobadas con su comprobante")
        void conComprobante(PagoModel.Medio medio) {
            PagoModel p = PagoModel.manual("c1", VALOR, medio, "  BCO-77812 ", "admin");
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.APROBADO);
            assertThat(p.getSoporte()).isEqualTo("BCO-77812");
            assertThat(p.getIdRegistradoPor()).isEqualTo("admin");
            assertThat(p.getFechaAprobacion()).isNotNull();
        }

        @ParameterizedTest(name = "comprobante \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   "})
        @DisplayName("una transferencia sin comprobante se rechaza")
        void sinComprobante(String soporte) {
            assertThatThrownBy(() -> PagoModel.manual("c1", VALOR, PagoModel.Medio.TRANSFERENCIA, soporte, "admin"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("comprobante");
        }

        @Test
        @DisplayName("en efectivo no exige comprobante")
        void efectivo() {
            PagoModel p = PagoModel.manual("c1", VALOR, PagoModel.Medio.EFECTIVO, null, "admin");
            assertThat(p.getSoporte()).isNull();
            assertThat(p.getEstado()).isEqualTo(PagoModel.Estado.APROBADO);
        }

        @Test
        @DisplayName("no se puede registrar a mano un pago de la pasarela")
        void wompiNoEsManual() {
            assertThatThrownBy(() -> PagoModel.manual("c1", VALOR, PagoModel.Medio.WOMPI, "x", "admin"))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> PagoModel.manual("c1", VALOR, null, "x", "admin"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @ParameterizedTest(name = "valor {0} → rechazado")
    @ValueSource(strings = {"0", "-5"})
    @DisplayName("ningún pago puede ser en cero o negativo")
    void valorInvalido(String valor) {
        assertThatThrownBy(() -> PagoModel.enLinea("c1", new BigDecimal(valor), "u1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("mayor que cero");
    }
}
