package gescazone.demo.infrastructure.pagos;

import gescazone.demo.domain.model.PagoModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pasarela Wompi")
class WompiPasarelaPruebas {

    private final WompiPasarela wompi = new WompiPasarela("pub_test_abc", "test_integrity_s", "test_events_e");

    @Test
    @DisplayName("sin URL de regreso ni correo, el enlace no los incluye")
    void enlaceMinimo() {
        String url = wompi.urlCheckout(PagoModel.enLinea("c1", new BigDecimal("80000"), "u1"), null, " ");
        assertThat(url).contains("currency=COP").contains("amount-in-cents=8000000")
                .doesNotContain("redirect-url").doesNotContain("customer-data");
    }

    @Test
    @DisplayName("la URL de regreso solo se envía si es https (Wompi bloquea con 403 las http)")
    void regresoSoloHttps() {
        PagoModel pago = PagoModel.enLinea("c1", new BigDecimal("80000"), "u1");
        assertThat(wompi.urlCheckout(pago, null, "http://localhost:8081/pagosYCartera")).doesNotContain("redirect-url");
        assertThat(wompi.urlCheckout(pago, null, "https://aurum.co/pagosYCartera")).contains("redirect-url=https://aurum.co/pagosYCartera");
    }

    @Test
    @DisplayName("sin llave privada no se busca por referencia")
    void busquedaSinLlavePrivada() {
        assertThat(wompi.buscarPorReferencia("AUR-1234", java.time.LocalDate.now())).isEmpty();
        assertThat(new WompiPasarela("pub_test_abc", "i", "e", "prv_test_x").buscarPorReferencia("../x", java.time.LocalDate.now())).isEmpty();
    }

    @Test
    @DisplayName("cambiar el valor cambia la firma (nadie puede pagar menos)")
    void firmaDependeDelValor() {
        assertThat(WompiPasarela.firmaIntegridad("AUR-1", 35_000_000, "COP", "s"))
                .isNotEqualTo(WompiPasarela.firmaIntegridad("AUR-1", 100, "COP", "s"))
                .hasSize(64);
    }

    @ParameterizedTest(name = "estado {0} → aprobada {1}, final {2}")
    @CsvSource({"APPROVED, true, true", "DECLINED, false, true", "VOIDED, false, true",
            "ERROR, false, true", "PENDING, false, false"})
    @DisplayName("interpreta los estados de transacción de Wompi")
    void estados(String estado, boolean aprobada, boolean fin) {
        var t = WompiPasarela.aTransaccion(Map.of("id", "tx", "reference", "AUR-1", "amount_in_cents", 100, "status", estado));
        assertThat(t.aprobada()).isEqualTo(aprobada);
        assertThat(t.final_()).isEqualTo(fin);
    }

    @ParameterizedTest(name = "id \"{0}\" → no se consulta")
    @ValueSource(strings = {"", "../../admin", "tx con espacios", "a"})
    @DisplayName("ids de transacción con formato raro no se envían a Wompi")
    void idsInvalidos(String id) {
        assertThat(wompi.consultar(id)).isEmpty();
    }

    @Test
    @DisplayName("eventos que no son de transacción se ignoran; sin secreto ninguno es auténtico")
    void eventosIgnorados() {
        assertThat(wompi.transaccionDeEvento(Map.of("event", "nequi_token.updated", "data", Map.of()))).isEmpty();
        assertThat(new WompiPasarela("pub_test_abc", "i", "").eventoAutentico(Map.of("event", "x"))).isFalse();
        assertThat(wompi.eventoAutentico(Map.of("event", "sin firma"))).isFalse();
    }
}
