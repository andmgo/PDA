package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.RegistroParqueaderoVisitanteService;
import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registro-parqueadero")
@CrossOrigin(origins = "*")
public class RegistroParqueaderoVisitanteRestController {

    private final RegistroParqueaderoVisitanteService registroService;

    public RegistroParqueaderoVisitanteRestController(RegistroParqueaderoVisitanteService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            return ResponseEntity.ok(registroService.consultarTodos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Un solo ID String reemplaza la clave compuesta residenteId+parqueaderoId
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            return registroService.consultarPorId(id)
                    .map(r -> ResponseEntity.ok(convertirAMap(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/residente/{idResidente}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorResidente(@PathVariable String idResidente) {
        try {
            return ResponseEntity.ok(registroService.consultarPorResidente(idResidente)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/parqueadero/{idParqueadero}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorParqueadero(@PathVariable String idParqueadero) {
        try {
            return ResponseEntity.ok(registroService.consultarPorParqueadero(idParqueadero)
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
            return ResponseEntity.ok(registroService.consultarPorApartamento(idApartamento)
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
            return ResponseEntity.ok(registroService.consultarActivos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/parqueadero/{idParqueadero}/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivosPorParqueadero(
            @PathVariable String idParqueadero) {
        try {
            return ResponseEntity.ok(registroService.consultarActivosPorParqueadero(idParqueadero)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/apartamento/{idApartamento}/activos")
    public ResponseEntity<List<Map<String, Object>>> obtenerActivosPorApartamento(
            @PathVariable String idApartamento) {
        try {
            return ResponseEntity.ok(registroService.consultarActivosPorApartamento(idApartamento)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<List<Map<String, Object>>> buscarPorPlaca(@PathVariable String placa) {
        try {
            return ResponseEntity.ok(registroService.buscarPorPlaca(placa)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/parqueadero/{idParqueadero}/count-activos")
    public ResponseEntity<Map<String, Object>> contarActivosPorParqueadero(
            @PathVariable String idParqueadero) {
        try {
            long count = registroService.contarActivosPorParqueadero(idParqueadero);
            return ResponseEntity.ok(Map.of(
                    "idParqueadero", idParqueadero,
                    "visitantesActivos", count
            ));
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
            String idParqueadero = (String) datos.get("idParqueadero");
            String idApartamento = (String) datos.get("idApartamento");
            String placa         = (String) datos.get("placa");
            String fechaEntrada  = (String) datos.get("fechaHoraEntrada");

            if (idResidente == null || idResidente.isBlank())
                return ResponseEntity.badRequest().body("El ID del residente es obligatorio");
            if (idParqueadero == null || idParqueadero.isBlank())
                return ResponseEntity.badRequest().body("El ID del parqueadero es obligatorio");

            RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel(
                    idResidente, idParqueadero, fechaEntrada, placa);
            registro.setIdApartamento(idApartamento);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(registroService.crear(
                            registro,
                            idResidente,
                            idParqueadero,
                            idApartamento
                    ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el registro: " + e.getMessage());
        }
    }

    // Salida se identifica por el ID del registro, no por clave compuesta
    @PostMapping("/registrar-salida/{id}")
    public ResponseEntity<String> registrarSalida(@PathVariable String id,
                                                   @RequestBody Map<String, Object> datos) {
        try {
            String fechaHoraSalida = (String) datos.get("fechaHoraSalida");

            if (fechaHoraSalida == null || fechaHoraSalida.isBlank())
                return ResponseEntity.badRequest().body("La fecha y hora de salida es obligatoria");

            return ResponseEntity.ok(registroService.registrarSalida(id, fechaHoraSalida));
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
            String idResidente   = (String) datos.get("idResidente");
            String idParqueadero = (String) datos.get("idParqueadero");
            String idApartamento = (String) datos.get("idApartamento");
            String placa         = (String) datos.get("placa");
            String fechaEntrada  = (String) datos.get("fechaHoraEntrada");
            String fechaSalida   = (String) datos.get("fechaHoraSalida");

            RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel();
            registro.setIdResidente(idResidente);
            registro.setIdParqueadero(idParqueadero);
            registro.setIdApartamento(idApartamento);
            registro.setPlaca(placa);
            registro.setFechaHoraEntrada(fechaEntrada);
            if (fechaSalida != null) registro.setFechaHoraSalida(fechaSalida);

            return ResponseEntity.ok(registroService.actualizar(id, registro));
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
            return ResponseEntity.ok(registroService.eliminar(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el registro: " + e.getMessage());
        }
    }

    private Map<String, Object> convertirAMap(RegistroParqueaderoVisitanteModel reg) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",              reg.getId());
        map.put("idResidente",     reg.getIdResidente());
        map.put("idParqueadero",   reg.getIdParqueadero());
        map.put("idApartamento",   reg.getIdApartamento());
        map.put("placa",           reg.getPlaca());
        map.put("fechaHoraEntrada",reg.getFechaHoraEntrada());
        map.put("fechaHoraSalida", reg.getFechaHoraSalida());
        map.put("activo",          reg.isActivo());
        return map;
    }
}