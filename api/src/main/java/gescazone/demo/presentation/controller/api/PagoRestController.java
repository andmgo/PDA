package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.CarteraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * "Mi estado de cuenta" del propietario (permiso PAGOS_Y_CARTERA) y pago en
 * línea. Todo opera sobre la cuenta del JWT. El evento de Wompi
 * (/wompi/eventos) es público pero se acepta solo con firma válida.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoRestController {

    @Autowired
    private CarteraService carteraService;

    @GetMapping("/mi-cuenta")
    public ResponseEntity<List<CarteraService.EstadoCuenta>> miCuenta(Authentication authentication) {
        return ResponseEntity.ok(carteraService.miCuenta(authentication.getName()));
    }

    /** ¿Se puede pagar en línea? (sin llaves de la pasarela, solo pagos manuales). */
    @GetMapping("/pasarela")
    public ResponseEntity<Map<String, Object>> pasarela() {
        return ResponseEntity.ok(Map.of("habilitada", carteraService.pagoEnLineaHabilitado()));
    }

    /** Crea el pago pendiente y devuelve el enlace al checkout de la pasarela. */
    @PostMapping("/cargos/{idCargo}/pagar")
    public ResponseEntity<CarteraService.PagoEnLinea> pagar(@PathVariable String idCargo,
                                                            @RequestBody(required = false) Map<String, String> datos,
                                                            Authentication authentication) {
        String regreso = datos == null ? null : datos.get("urlRegreso");
        return ResponseEntity.ok(carteraService.iniciarPagoEnLinea(authentication.getName(), idCargo, regreso));
    }

    /** Al volver del checkout: se consulta la transacción a la pasarela. */
    @PostMapping("/confirmar")
    public ResponseEntity<Map<String, String>> confirmar(@RequestBody Map<String, String> datos) {
        return ResponseEntity.ok(Map.of("estado", carteraService.confirmarTransaccion(datos.get("idTransaccion"))));
    }

    /** Eventos de Wompi (URL de eventos configurada en su panel). */
    @PostMapping("/wompi/eventos")
    public ResponseEntity<String> evento(@RequestBody Map<String, Object> evento) {
        return ResponseEntity.ok(carteraService.procesarEvento(evento));
    }
}
