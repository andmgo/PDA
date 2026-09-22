package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PaqueteService;
import gescazone.demo.domain.model.PaqueteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Paquetes de recepción — visible solo para Funcionario y Administrador
 * (ver PAQUETES en SecurityConfig; Propietario no tiene el permiso en absoluto).
 */
@RestController
@RequestMapping("/api/paquetes")
public class PaqueteRestController {

    @Autowired
    private PaqueteService paqueteService;

    @PostMapping
    public ResponseEntity<String> registrar(@RequestBody Map<String, String> datos) {
        String mensaje = paqueteService.registrar(
                datos.get("idApartamento"), datos.get("nombreReceptor"), datos.get("cedulaReceptor"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @GetMapping
    public ResponseEntity<List<PaqueteModel>> todos() {
        return ResponseEntity.ok(paqueteService.consultarTodos());
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<PaqueteModel>> pendientes() {
        return ResponseEntity.ok(paqueteService.consultarPendientes());
    }

    @GetMapping("/apartamento/{idApartamento}")
    public ResponseEntity<?> porApartamento(@PathVariable String idApartamento) {
        return ResponseEntity.ok(paqueteService.consultarPorApartamento(idApartamento));
    }

    @PostMapping("/{id}/entregar")
    public ResponseEntity<String> marcarEntregado(@PathVariable String id) {
        return ResponseEntity.ok(paqueteService.marcarEntregado(id));
    }
}
