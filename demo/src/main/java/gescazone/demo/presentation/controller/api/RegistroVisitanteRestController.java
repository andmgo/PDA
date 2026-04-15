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
@CrossOrigin(origins = "*")
public class RegistroVisitanteRestController {

    private final RegistroVisitanteService registroVisitanteService;

    public RegistroVisitanteRestController(RegistroVisitanteService registroVisitanteService) {
        this.registroVisitanteService = registroVisitanteService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            return ResponseEntity.ok(registroVisitanteService.consultarTodos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Un solo ID String reemplaza la clave compuesta residenteId+apartamentoId
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            return registroVisitanteService.consultarPorId(id)
                    .map(r -> ResponseEntity.ok(convertirAMap(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/visitante/{idResidente}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorVisitante(@PathVariable String idResidente) {
        try {
            return ResponseEntity.ok(registroVisitanteService.consultarPorIdResidente(idResidente)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/apartamento/{idApartamento}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorApartamento(@PathVariable String idApartamento) {
        try {
            return ResponseEntity.ok(registroVisitanteService.consultarPorIdApartamento(idApartamento)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivos() {
        try {
            return ResponseEntity.ok(registroVisitanteService.consultarVisitantesActivos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/apartamento/{idApartamento}/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivosPorApartamento(
            @PathVariable String idApartamento) {
        try {
            return ResponseEntity.ok(
                    registroVisitanteService.consultarVisitantesActivosPorApartamento(idApartamento)
                            .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        try {
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el registro: " + e.getMessage());
        }
    }

    @PostMapping("/registrar-salida/{id}")
    public ResponseEntity<String> registrarSalida(@PathVariable String id,
                                                   @RequestBody Map<String, Object> datos) {
        try {
            String fechaHoraSalida = (String) datos.get("fechaHoraSalida");

            if (fechaHoraSalida == null || fechaHoraSalida.isBlank())
                return ResponseEntity.badRequest().body("La fecha y hora de salida es obligatoria");

            return ResponseEntity.ok(registroVisitanteService.registrarSalida(id, fechaHoraSalida));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar la salida: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizar(@PathVariable String id,
                                             @RequestBody Map<String, Object> datos) {
        try {
            String fechaEntrada = (String) datos.get("fechaHoraEntrada");
            String fechaSalida  = (String) datos.get("fechaHoraSalida");

            RegistroVisitanteModel registro = new RegistroVisitanteModel();
            registro.setFechaHoraEntrada(fechaEntrada);
            if (fechaSalida != null) registro.setFechaHoraSalida(fechaSalida);

            return ResponseEntity.ok(registroVisitanteService.actualizar(id, registro));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el registro: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(registroVisitanteService.eliminar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el registro: " + e.getMessage());
        }
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