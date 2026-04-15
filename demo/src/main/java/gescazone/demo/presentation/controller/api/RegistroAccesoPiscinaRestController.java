package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.RegistroAccesoPiscinaService;
import gescazone.demo.domain.model.RegistroAccesoPiscinaModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/piscina")
@CrossOrigin(origins = "*")
public class RegistroAccesoPiscinaRestController {

    private final RegistroAccesoPiscinaService registroService;

    public RegistroAccesoPiscinaRestController(RegistroAccesoPiscinaService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            List<Map<String, Object>> response = registroService.consultarTodos()
                    .stream().map(this::convertirAMap).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // ID ahora es String (ObjectId de Mongo)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            return registroService.consultarTodos().stream()
                    .filter(r -> r.getId().equals(id))
                    .findFirst()
                    .map(r -> ResponseEntity.ok(convertirAMap(r)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<Map<String, Object>>> obtenerRegistrosHoy() {
        try {
            List<Map<String, Object>> response = registroService.consultarRegistrosHoy()
                    .stream().map(this::convertirAMap).toList();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/apartamento/{numero}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorApartamento(@PathVariable String numero) {
        try {
            List<Map<String, Object>> response = registroService.consultarPorApartamento(numero)
                    .stream().map(this::convertirAMap).toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // numeroDocumento ahora es String, no Integer
    @GetMapping("/residente/{numeroDocumento}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorResidente(@PathVariable Integer numeroDocumento) {
        try {
            List<Map<String, Object>> response = registroService.consultarPorResidente(numeroDocumento)
                    .stream().map(this::convertirAMap).toList();
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/verificar-acceso/{numeroApartamento}")
    public ResponseEntity<Map<String, Object>> verificarAcceso(@PathVariable String numeroApartamento) {
        try {
            boolean puedeAcceder = registroService.verificarAcceso(numeroApartamento);
            return ResponseEntity.ok(Map.of(
                    "numeroApartamento", numeroApartamento,
                    "puedeAcceder", puedeAcceder,
                    "mensaje", puedeAcceder
                            ? "Apartamento autorizado para acceder"
                            : "Apartamento NO autorizado (verificar pagos)"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/registrar-ingreso")
    public ResponseEntity<String> registrarIngreso(@RequestBody Map<String, Object> datos) {
        try {
            String numeroApartamento = (String) datos.get("numeroApartamento");
            Object docObj = datos.get("numeroDocumento");

            if (numeroApartamento == null || numeroApartamento.isBlank())
                return ResponseEntity.badRequest().body("El número del apartamento es obligatorio");
            if (docObj == null)
                return ResponseEntity.badRequest().body("El número de documento es obligatorio");

            Integer numeroDocumento = ((Number) docObj).intValue();

            String resultado = registroService.registrarIngreso(numeroApartamento, numeroDocumento);
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar ingreso: " + e.getMessage());
        }
    }

    // ID ahora es String
    @PutMapping("/modificar/{id}")
    public ResponseEntity<String> modificar(@PathVariable String id,
                                            @RequestBody Map<String, Object> datos) {
        try {
            String idResidente  = (String) datos.get("idResidente");
            String idApartamento = (String) datos.get("idApartamento");

            if (idResidente == null || idResidente.isBlank())
                return ResponseEntity.badRequest().body("El ID del residente es obligatorio");
            if (idApartamento == null || idApartamento.isBlank())
                return ResponseEntity.badRequest().body("El ID del apartamento es obligatorio");

            String resultado = registroService.modificar(id, idResidente, idApartamento);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar el registro: " + e.getMessage());
        }
    }

    // ID ahora es String
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

    private Map<String, Object> convertirAMap(RegistroAccesoPiscinaModel reg) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",             reg.getId());
        map.put("idApartamento",  reg.getIdApartamento());
        map.put("idResidente",    reg.getIdResidente());
        map.put("fechaHora",      reg.getFechaHora().toString());
        return map;
    }
}