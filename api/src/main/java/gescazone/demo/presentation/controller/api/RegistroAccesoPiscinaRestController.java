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
public class RegistroAccesoPiscinaRestController {

    private final RegistroAccesoPiscinaService registroService;

    public RegistroAccesoPiscinaRestController(RegistroAccesoPiscinaService registroService) {
        this.registroService = registroService;
    }

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        List<Map<String, Object>> response = registroService.consultarTodos()
                .stream().map(this::convertirAMap).toList();
        return ResponseEntity.ok(response);
    }

    // ID ahora es String (ObjectId de Mongo)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        return registroService.consultarTodos().stream()
                .filter(r -> r.getId().equals(id))
                .findFirst()
                .map(r -> ResponseEntity.ok(convertirAMap(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/hoy")
    public ResponseEntity<List<Map<String, Object>>> obtenerRegistrosHoy() {
        List<Map<String, Object>> response = registroService.consultarRegistrosHoy()
                .stream().map(this::convertirAMap).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/apartamento/{numero}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorApartamento(@PathVariable String numero) {
        List<Map<String, Object>> response = registroService.consultarPorApartamento(numero)
                .stream().map(this::convertirAMap).toList();
        return ResponseEntity.ok(response);
    }

    // numeroDocumento ahora es String, no Integer
    @GetMapping("/residente/{numeroDocumento}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorResidente(@PathVariable Integer numeroDocumento) {
        List<Map<String, Object>> response = registroService.consultarPorResidente(numeroDocumento)
                .stream().map(this::convertirAMap).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verificar-acceso/{numeroApartamento}")
    public ResponseEntity<Map<String, Object>> verificarAcceso(@PathVariable String numeroApartamento) {
        boolean puedeAcceder = registroService.verificarAcceso(numeroApartamento);
        return ResponseEntity.ok(Map.of(
                "numeroApartamento", numeroApartamento,
                "puedeAcceder", puedeAcceder,
                "mensaje", puedeAcceder
                        ? "Apartamento autorizado para acceder"
                        : "Apartamento NO autorizado (verificar pagos)"
        ));
    }

    @PostMapping("/registrar-ingreso")
    public ResponseEntity<String> registrarIngreso(@RequestBody Map<String, Object> datos) {
        String numeroApartamento = (String) datos.get("numeroApartamento");
        Object docObj = datos.get("numeroDocumento");

        if (numeroApartamento == null || numeroApartamento.isBlank())
            return ResponseEntity.badRequest().body("El número del apartamento es obligatorio");
        if (docObj == null)
            return ResponseEntity.badRequest().body("El número de documento es obligatorio");

        Integer numeroDocumento = ((Number) docObj).intValue();

        String resultado = registroService.registrarIngreso(numeroApartamento, numeroDocumento);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    // ID ahora es String
    @PutMapping("/modificar/{id}")
    public ResponseEntity<String> modificar(@PathVariable String id,
                                            @RequestBody Map<String, Object> datos) {
        String idResidente  = (String) datos.get("idResidente");
        String idApartamento = (String) datos.get("idApartamento");

        if (idResidente == null || idResidente.isBlank())
            return ResponseEntity.badRequest().body("El ID del residente es obligatorio");
        if (idApartamento == null || idApartamento.isBlank())
            return ResponseEntity.badRequest().body("El ID del apartamento es obligatorio");

        String resultado = registroService.modificar(id, idResidente, idApartamento);
        return ResponseEntity.ok(resultado);
    }

    // ID ahora es String
    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(registroService.eliminar(id));
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
