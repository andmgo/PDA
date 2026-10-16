package gescazone.demo.infrastructure.pagos;

import gescazone.demo.application.port.PasarelaPagos;
import gescazone.demo.domain.model.PagoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wompi (pasarela colombiana: PSE, tarjetas, Nequi, Bancolombia).
 *  · Checkout alojado por Wompi: Aurum nunca ve datos de tarjetas.
 *  · Cada enlace lleva firma de integridad SHA-256(referencia + centavos +
 *    moneda + secreto de integridad), así nadie puede cambiar el valor.
 *  · El resultado se consulta a la API de Wompi (GET /transactions/{id}) o
 *    llega por evento firmado (checksum con el secreto de eventos).
 * Llaves por variables de entorno (WOMPI_*). Con llaves de prueba
 * ("pub_test_…") usa el ambiente sandbox automáticamente.
 */
@Component
public class WompiPasarela implements PasarelaPagos {

    private static final Logger log = LoggerFactory.getLogger(WompiPasarela.class);
    private static final String CHECKOUT = "https://checkout.wompi.co/p/";

    private final String llavePublica;
    private final String secretoIntegridad;
    private final String secretoEventos;
    private final RestClient cliente;

    public WompiPasarela(@Value("${wompi.public-key:}") String llavePublica,
                         @Value("${wompi.integrity-secret:}") String secretoIntegridad,
                         @Value("${wompi.events-secret:}") String secretoEventos) {
        this.llavePublica = llavePublica.trim();
        this.secretoIntegridad = secretoIntegridad.trim();
        this.secretoEventos = secretoEventos.trim();
        String api = this.llavePublica.startsWith("pub_prod_") ? "https://production.wompi.co/v1" : "https://sandbox.wompi.co/v1";
        this.cliente = RestClient.builder().baseUrl(api).build();
    }

    @Override
    public boolean configurada() {
        return !llavePublica.isEmpty() && !secretoIntegridad.isEmpty();
    }

    @Override
    public String urlCheckout(PagoModel pago, String correoPagador, String urlRegreso) {
        long centavos = pago.valorEnCentavos();
        UriComponentsBuilder url = UriComponentsBuilder.fromHttpUrl(CHECKOUT)
                .queryParam("public-key", llavePublica)
                .queryParam("currency", "COP")
                .queryParam("amount-in-cents", centavos)
                .queryParam("reference", pago.getReferencia())
                .queryParam("signature:integrity", firmaIntegridad(pago.getReferencia(), centavos, "COP", secretoIntegridad));
        if (urlRegreso != null && !urlRegreso.isBlank()) url.queryParam("redirect-url", urlRegreso);
        if (correoPagador != null && !correoPagador.isBlank()) url.queryParam("customer-data:email", correoPagador);
        return url.encode().build().toUriString();
    }

    /** SHA-256 de referencia + centavos + moneda + secreto, en hexadecimal (formato de Wompi). */
    static String firmaIntegridad(String referencia, long centavos, String moneda, String secreto) {
        return sha256(referencia + centavos + moneda + secreto);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Transaccion> consultar(String idTransaccion) {
        if (idTransaccion == null || !idTransaccion.matches("[A-Za-z0-9-]{4,80}")) return Optional.empty();
        try {
            Map<String, Object> r = cliente.get().uri("/transactions/{id}", idTransaccion).retrieve().body(Map.class);
            return Optional.ofNullable(r).map(m -> (Map<String, Object>) m.get("data")).map(WompiPasarela::aTransaccion);
        } catch (RestClientException e) {
            log.warn("No se pudo consultar la transacción {} en Wompi: {}", idTransaccion, e.getMessage());
            return Optional.empty();
        }
    }

    /** Estados de Wompi: APPROVED, DECLINED, VOIDED, ERROR (finales) y PENDING. */
    static Transaccion aTransaccion(Map<String, Object> t) {
        String estado = String.valueOf(t.get("status"));
        return new Transaccion(String.valueOf(t.get("id")), String.valueOf(t.get("reference")),
                ((Number) t.get("amount_in_cents")).longValue(), "APPROVED".equals(estado), !"PENDING".equals(estado));
    }

    /**
     * checksum = SHA-256(valores de signature.properties en orden + timestamp
     * + secreto de eventos), según la documentación de eventos de Wompi.
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean eventoAutentico(Map<String, Object> evento) {
        if (secretoEventos.isEmpty() || evento == null) return false;
        try {
            Map<String, Object> firma = (Map<String, Object>) evento.get("signature");
            List<String> propiedades = (List<String>) firma.get("properties");
            Map<String, Object> datos = (Map<String, Object>) evento.get("data");
            StringBuilder cadena = new StringBuilder();
            for (String propiedad : propiedades) cadena.append(valorEn(datos, propiedad));
            cadena.append(evento.get("timestamp")).append(secretoEventos);
            return MessageDigest.isEqual(sha256(cadena.toString()).getBytes(StandardCharsets.UTF_8),
                    String.valueOf(firma.get("checksum")).toLowerCase().getBytes(StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<Transaccion> transaccionDeEvento(Map<String, Object> evento) {
        if (!"transaction.updated".equals(evento.get("event"))) return Optional.empty();
        Map<String, Object> datos = (Map<String, Object>) evento.get("data");
        return Optional.ofNullable((Map<String, Object>) datos.get("transaction")).map(WompiPasarela::aTransaccion);
    }

    @SuppressWarnings("unchecked")
    private static Object valorEn(Map<String, Object> datos, String ruta) {
        Object actual = datos;
        for (String parte : ruta.split("\\.")) actual = ((Map<String, Object>) actual).get(parte);
        return actual;
    }

    static String sha256(String texto) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(texto.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
