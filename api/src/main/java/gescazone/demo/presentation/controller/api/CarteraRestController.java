package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.application.service.CarteraService;
import gescazone.demo.domain.model.ConfiguracionCarteraModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Administración de la cartera (permiso ADMIN_CARTERA): resumen del
 * conjunto, estado de un apartamento, generar cuotas del mes, registrar
 * pagos manuales y valores (cuota, reserva, día de vencimiento).
 */
@RestController
@RequestMapping("/api/cartera")
public class CarteraRestController {

    @Autowired
    private CarteraService carteraService;

    @GetMapping("/resumen")
    public ResponseEntity<CarteraService.ResumenCartera> resumen() {
        return ResponseEntity.ok(carteraService.resumen());
    }

    @GetMapping("/apartamentos/{idApartamento}")
    public ResponseEntity<CarteraService.EstadoCuenta> apartamento(@PathVariable String idApartamento) {
        return ResponseEntity.ok(carteraService.estadoApartamento(idApartamento));
    }

    @PostMapping("/cuotas")
    public ResponseEntity<String> generarCuotas(@RequestBody(required = false) Map<String, String> datos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carteraService.generarCuotas(datos == null ? null : datos.get("periodo")));
    }

    @PostMapping("/pagos")
    public ResponseEntity<String> registrarPago(@RequestBody Map<String, String> datos, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(carteraService.registrarPagoManual(
                authentication.getName(), datos.get("idCargo"), dinero(datos.get("valor")), datos.get("medio"), datos.get("soporte")));
    }

    @GetMapping("/configuracion")
    public ResponseEntity<ConfiguracionCarteraModel> configuracion() {
        return ResponseEntity.ok(carteraService.configuracion());
    }

    @PutMapping("/configuracion")
    public ResponseEntity<ConfiguracionCarteraModel> guardarConfiguracion(@RequestBody Map<String, String> datos) {
        String dia = datos.get("diaVencimiento");
        Integer diaVence;
        try {
            diaVence = dia == null || dia.isBlank() ? null : Integer.valueOf(dia.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Día de vencimiento inválido");
        }
        return ResponseEntity.ok(carteraService.guardarConfiguracion(
                dinero(datos.get("cuotaAdministracion")), dinero(datos.get("valorReserva")), diaVence));
    }

    /** "350000", "350.000" o "$ 350.000" → 350000 (pesos sin centavos). */
    static BigDecimal dinero(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String limpio = texto.replaceAll("[^0-9]", "");
        if (limpio.isEmpty()) throw new ValidationException("Valor inválido: " + texto);
        return new BigDecimal(limpio);
    }
}
