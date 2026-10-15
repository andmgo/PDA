package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PaqueteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Paquetes de recepción. Portería (permiso PAQUETES: ver para listar, editar
 * para registrar y entregar) y, desde la Fase 7, "Mis paquetes" del
 * propietario en /mios (permiso MIS_PAQUETES). Ver SecurityConfig.
 */
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteRestController {

    @Autowired
    private PaqueteService paqueteService;

    /** Acepta idApartamento o, desde portería, numeroApartamento ("301"). */
    @PostMapping
    public ResponseEntity<String> registrar(@RequestBody Map<String, String> datos, Authentication authentication) {
        String mensaje = datos.get("idApartamento") != null
                ? paqueteService.registrar(datos.get("idApartamento"), datos.get("nombreReceptor"),
                        datos.get("cedulaReceptor"), datos.get("descripcion"), authentication.getName())
                : paqueteService.registrarPorNumeroApartamento(datos.get("numeroApartamento"), datos.get("nombreReceptor"),
                        datos.get("cedulaReceptor"), datos.get("descripcion"), authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @GetMapping
    public ResponseEntity<List<PaqueteService.Paquete>> todos() {
        return ResponseEntity.ok(paqueteService.vista(paqueteService.consultarTodos()));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PaqueteService.Paquete>> pendientes() {
        return ResponseEntity.ok(paqueteService.vista(paqueteService.consultarPendientes()));
    }

    @GetMapping("/apartamento/{idApartamento}")
    public ResponseEntity<List<PaqueteService.Paquete>> porApartamento(@PathVariable String idApartamento) {
        return ResponseEntity.ok(paqueteService.vista(paqueteService.consultarPorApartamento(idApartamento)));
    }

    /** Paquetes de los apartamentos de la cuenta del JWT (Fase 7). */
    @GetMapping("/mios")
    public ResponseEntity<List<PaqueteService.Paquete>> mios(Authentication authentication) {
        return ResponseEntity.ok(paqueteService.mios(authentication.getName()));
    }

    /** Cuerpo opcional: {"entregadoA": "Nombre de quien lo retira"}. */
    @PostMapping("/{id}/entregar")
    public ResponseEntity<String> marcarEntregado(@PathVariable String id,
                                                  @RequestBody(required = false) Map<String, String> datos,
                                                  Authentication authentication) {
        String entregadoA = datos == null ? null : datos.get("entregadoA");
        return ResponseEntity.ok(paqueteService.marcarEntregado(id, entregadoA, authentication.getName()));
    }
}
