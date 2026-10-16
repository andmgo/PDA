package gescazone.demo.application.port;

import gescazone.demo.domain.model.PagoModel;

import java.util.Map;
import java.util.Optional;

/**
 * Pasarela de pagos en línea (hoy Wompi, ver infrastructure.pagos). La
 * aplicación no sabe de URLs ni firmas: pide un enlace de pago y consulta
 * el resultado de una transacción.
 */
public interface PasarelaPagos {

    /** false mientras no estén las llaves de la pasarela (pagos en línea deshabilitados). */
    boolean configurada();

    /** Enlace al checkout de la pasarela para este pago pendiente. */
    String urlCheckout(PagoModel pago, String correoPagador, String urlRegreso);

    /** Estado real de una transacción, consultado directamente a la pasarela. */
    Optional<Transaccion> consultar(String idTransaccion);

    /**
     * Transacción hecha con nuestra referencia (para conciliar pagos que se
     * quedaron "en proceso": sin URL de regreso o sin evento). Vacío si no
     * hay, o si la pasarela no permite buscar.
     */
    Optional<Transaccion> buscarPorReferencia(String referencia, java.time.LocalDate desde);

    /** ¿El evento (webhook) viene firmado por la pasarela? */
    boolean eventoAutentico(Map<String, Object> evento);

    /** Transacción extraída de un evento ya verificado. */
    Optional<Transaccion> transaccionDeEvento(Map<String, Object> evento);

    record Transaccion(String id, String referencia, long valorEnCentavos, boolean aprobada, boolean final_) {}
}
