package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.application.service.VisitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Portería en un solo paso (Fase 3). Vive bajo /api/accesos/** — mismo
 * permiso CONTROL_ACCESOS que el resto de Control de Accesos.
 */
@RestController
@RequestMapping("/api/accesos/visitas")
public class VisitaRestController {

    @Autowired
    private VisitaService visitaService;

    @GetMapping("/dentro")
    public ResponseEntity<List<VisitaService.Visita>> dentro() {
        return ResponseEntity.ok(visitaService.dentro());
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<VisitaService.Visita>> hoy() {
        return ResponseEntity.ok(visitaService.deHoy());
    }

    @GetMapping("/visitante/{numeroDocumento}")
    public ResponseEntity<VisitaService.Visitante> visitante(@PathVariable String numeroDocumento) {
        return ResponseEntity.ok(visitaService.buscarVisitante(numeroDocumento));
    }

    @GetMapping("/parqueaderos-libres")
    public ResponseEntity<List<String>> parqueaderosLibres() {
        return ResponseEntity.ok(visitaService.parqueaderosLibres());
    }

    @PostMapping("/ingreso")
    public ResponseEntity<String> ingreso(@RequestBody Map<String, Object> datos) {
        VisitaService.Ingreso ingreso = new VisitaService.Ingreso(
                texto(datos, "numeroDocumento"), texto(datos, "nombre"), texto(datos, "apellido"),
                texto(datos, "celular"), texto(datos, "numeroApartamento"),
                Boolean.TRUE.equals(datos.get("conVehiculo")),
                texto(datos, "placa"), texto(datos, "numeroParqueadero"));
        return ResponseEntity.status(HttpStatus.CREATED).body(visitaService.registrarIngreso(ingreso));
    }

    @PutMapping("/{tipo}/{id}/salida")
    public ResponseEntity<String> salida(@PathVariable String tipo, @PathVariable String id) {
        VisitaService.Tipo t;
        try {
            t = VisitaService.Tipo.valueOf(tipo.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Tipo de visita inválido: " + tipo);
        }
        return ResponseEntity.ok(visitaService.registrarSalida(t, id));
    }

    private static String texto(Map<String, Object> datos, String clave) {
        Object valor = datos.get(clave);
        return valor == null ? null : valor.toString();
    }
}
