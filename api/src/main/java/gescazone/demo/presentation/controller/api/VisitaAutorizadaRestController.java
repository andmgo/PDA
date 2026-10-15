package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.application.service.VisitaAutorizadaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * "Mis visitas" del propietario (Fase 6). Todo opera sobre la cuenta del JWT:
 * el propietario solo ve, crea y cancela SUS autorizaciones, y solo para SUS
 * apartamentos. Permiso VISITAS (ver/editar) en SecurityConfig.
 */
@RestController
@RequestMapping("/api/visitas-autorizadas")
public class VisitaAutorizadaRestController {

    @Autowired
    private VisitaAutorizadaService visitaAutorizadaService;

    @GetMapping("/mias")
    public ResponseEntity<List<VisitaAutorizadaService.Autorizacion>> mias(Authentication authentication) {
        return ResponseEntity.ok(visitaAutorizadaService.mias(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<VisitaAutorizadaService.Autorizacion> crear(@RequestBody Map<String, String> datos,
                                                                      Authentication authentication) {
        VisitaAutorizadaService.Nueva nueva = new VisitaAutorizadaService.Nueva(
                datos.get("idApartamento"), datos.get("nombreVisitante"), datos.get("apellidoVisitante"),
                datos.get("numeroDocumento"), datos.get("placa"),
                fecha(datos.get("fechaDesde")), fecha(datos.get("fechaHasta")), datos.get("nota"));
        return ResponseEntity.status(HttpStatus.CREATED).body(visitaAutorizadaService.crear(authentication.getName(), nueva));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<String> cancelar(@PathVariable String id, Authentication authentication) {
        return ResponseEntity.ok(visitaAutorizadaService.cancelar(authentication.getName(), id));
    }

    private static LocalDate fecha(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            return LocalDate.parse(texto.trim());
        } catch (DateTimeParseException e) {
            throw new ValidationException("Fecha inválida: " + texto + " (formato AAAA-MM-DD)");
        }
    }
}
