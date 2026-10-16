package gescazone.demo.infrastructure.pagos;

import gescazone.demo.domain.model.PagoModel;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WompiPasarelaTest {

    @Test
    void firmaDeIntegridad_comoLaDocumentaWompi() {
        // Ejemplo de la documentación de Wompi ("Firma de integridad")
        assertThat(WompiPasarela.firmaIntegridad("sk8-438k4-xmxm392-sn2m", 2490000, "COP",
                "prod_integrity_Z5mMke9x0k8gpErbDqwrJXMqsI6SFli6"))
                .isEqualTo("37c8407747e595535433ef8f6a811d853cd943046624a0ec04662b17bbf33bf5");
    }

    @Test
    void sinLlaves_noEstaConfigurada() {
        assertThat(new WompiPasarela("", "", "").configurada()).isFalse();
        assertThat(new WompiPasarela("pub_test_x", "test_integrity_y", "").configurada()).isTrue();
    }

    @Test
    void urlDeCheckout_llevaValorReferenciaYFirma() {
        WompiPasarela w = new WompiPasarela("pub_test_abc", "test_integrity_s", "");
        PagoModel pago = PagoModel.enLinea("c1", new BigDecimal("350000"), "u1");
        String url = w.urlCheckout(pago, "elena@correo.com", "http://localhost:8081/pagosYCartera");

        assertThat(url).startsWith("https://checkout.wompi.co/p/?public-key=pub_test_abc")
                .contains("amount-in-cents=35000000")
                .contains("reference=" + pago.getReferencia())
                .contains("signature:integrity=" + WompiPasarela.firmaIntegridad(pago.getReferencia(), 35000000, "COP", "test_integrity_s"));
    }

    @Test
    void evento_soloConChecksumCorrecto() {
        WompiPasarela w = new WompiPasarela("pub_test_abc", "i", "test_events_secreto");
        Map<String, Object> tx = Map.of("id", "1234-1610641025-49201", "status", "APPROVED", "amount_in_cents", 4490000,
                "reference", "AUR-1");
        String checksum = WompiPasarela.sha256("1234-1610641025-49201" + "APPROVED" + "4490000" + "1530291411" + "test_events_secreto");
        Map<String, Object> evento = Map.of(
                "event", "transaction.updated",
                "data", Map.of("transaction", tx),
                "signature", Map.of("properties", List.of("transaction.id", "transaction.status", "transaction.amount_in_cents"),
                        "checksum", checksum),
                "timestamp", 1530291411);

        assertThat(w.eventoAutentico(evento)).isTrue();
        assertThat(w.transaccionDeEvento(evento)).hasValueSatisfying(t -> {
            assertThat(t.aprobada()).isTrue();
            assertThat(t.referencia()).isEqualTo("AUR-1");
        });

        Map<String, Object> alterado = new java.util.HashMap<>(evento);
        alterado.put("timestamp", 1530291412);
        assertThat(w.eventoAutentico(alterado)).isFalse();
    }
}
