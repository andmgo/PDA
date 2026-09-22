package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.RegistroVisitanteService;
import gescazone.demo.domain.model.RegistroVisitanteModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registro-visitante")
public class RegistroVisitanteRestController {

    private final RegistroVisitanteService registroVisitanteService;

    public RegistroVisitanteRestController(RegistroVisitanteService registroVisitanteService) {
        this.registroVisitanteService = registroVisitanteService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        return ResponseEntity.ok(registroVisitanteService.consultarTodos()
                .stream().map(this::convertirAMap).toList());
    }

    // Un solo ID String reemplaza la clave compuesta residenteId+apartamentoId
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        return registroVisitanteService.consultarPorId(id)
                .map(r -> ResponseEntity.ok(convertirAMap(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/visitante/{idResidente}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorVisitante(@PathVariable String idResidente) {
        return ResponseEntity.ok(registroVisitanteService.consultarPorIdResidente(idResidente)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/apartamento/{idApartamento}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorApartamento(@PathVariable String idApartamento) {
        return ResponseEntity.ok(registroVisitanteService.consultarPorIdApartamento(idApartamento)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivos() {
        return ResponseEntity.ok(registroVisitanteService.consultarVisitantesActivos()
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/apartamento/{idApartamento}/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivosPorApartamento(
            @PathVariable String idApartamento) {
        return ResponseEntity.ok(
                registroVisitanteService.consultarVisitantesActivosPorApartamento(idApartamento)
                        .stream().map(this::convertirAMap).toList());
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        String idResidente   = (String) datos.get("idResidente");
        String idApartamento = (String) datos.get("idApartamento");
        String fechaEntrada  = (String) datos.get("fechaHoraEntrada");

        if (idResidente == null || idResidente.isBlank())
            return ResponseEntity.badRequest().body("El ID del residente es obligatorio");
        if (idApartamento == null || idApartamento.isBlank())
            return ResponseEntity.badRequest().body("El ID del apartamento es obligatorio");

        RegistroVisitanteModel registro = new RegistroVisitanteModel(
                idResidente, idApartamento, fechaEntrada);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(registroVisitanteService.crear(registro, idResidente, idApartamento));
    }

    @PostMapping("/registrar-salida/{id}")
    public ResponseEntity<String> registrarSalida(@PathVariable String id,
                                                   @RequestBody Map<String, Object> datos) {
        String fechaHoraSalida = (String) datos.get("fechaHoraSalida");

        if (fechaHoraSalida == null || fechaHoraSalida.isBlank())
            return ResponseEntity.badRequest().body("La fecha y hora de salida es obligatoria");

        return ResponseEntity.ok(registroVisitanteService.registrarSalida(id, fechaHoraSalida));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizar(@PathVariable String id,
                                             @RequestBody Map<String, Object> datos) {
        String fechaEntrada = (String) datos.get("fechaHoraEntrada");
        String fechaSalida  = (String) datos.get("fechaHoraSalida");

        RegistroVisitanteModel registro = new RegistroVisitanteModel();
        registro.setFechaHoraEntrada(fechaEntrada);
        if (fechaSalida != null) registro.setFechaHoraSalida(fechaSalida);

        return ResponseEntity.ok(registroVisitanteService.actualizar(id, registro));
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(registroVisitanteService.eliminar(id));
    }

    private Map<String, Object> convertirAMap(RegistroVisitanteModel reg) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",              reg.getId());
        map.put("idResidente",     reg.getIdResidente());
        map.put("idApartamento",   reg.getIdApartamento());
        map.put("fechaHoraEntrada",reg.getFechaHoraEntrada());
        map.put("fechaHoraSalida", reg.getFechaHoraSalida());
        map.put("activo",          reg.isActivo());
        return map;
    }
}
