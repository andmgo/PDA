package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PaqueteService;
import gescazone.demo.domain.model.PaqueteModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    /** Acepta idApartamento o, desde portería, numeroApartamento ("301"). */
    @PostMapping
    public ResponseEntity<String> registrar(@RequestBody Map<String, String> datos) {
        String mensaje = datos.get("idApartamento") != null
                ? paqueteService.registrar(datos.get("idApartamento"), datos.get("nombreReceptor"), datos.get("cedulaReceptor"))
                : paqueteService.registrarPorNumeroApartamento(datos.get("numeroApartamento"), datos.get("nombreReceptor"), datos.get("cedulaReceptor"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> todos() {
        return ResponseEntity.ok(conNumero(paqueteService.consultarTodos()));
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<Map<String, Object>>> pendientes() {
        return ResponseEntity.ok(conNumero(paqueteService.consultarPendientes()));
    }

    @GetMapping("/apartamento/{idApartamento}")
    public ResponseEntity<?> porApartamento(@PathVariable String idApartamento) {
        return ResponseEntity.ok(conNumero(paqueteService.consultarPorApartamento(idApartamento)));
    }

    /** Antes la tabla mostraba el UUID del apartamento; se agrega el número legible. */
    private List<Map<String, Object>> conNumero(List<PaqueteModel> paquetes) {
        Map<String, String> numeros = new HashMap<>();
        return paquetes.stream()
                .sorted((a, b) -> b.getFechaHoraLlegada().compareTo(a.getFechaHoraLlegada()))
                .map(p -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", p.getId());
                    m.put("idApartamento", p.getIdApartamento());
                    m.put("numeroApartamento", numeros.computeIfAbsent(p.getIdApartamento(), paqueteService::numeroApartamento));
                    m.put("nombreReceptor", p.getNombreReceptor());
                    m.put("cedulaReceptor", p.getCedulaReceptor());
                    m.put("fechaHoraLlegada", p.getFechaHoraLlegada());
                    m.put("entregado", p.isEntregado());
                    m.put("fechaHoraEntrega", p.getFechaHoraEntrega());
                    return m;
                }).toList();
    }

    @PostMapping("/{id}/entregar")
    public ResponseEntity<String> marcarEntregado(@PathVariable String id) {
        return ResponseEntity.ok(paqueteService.marcarEntregado(id));
    }
}
