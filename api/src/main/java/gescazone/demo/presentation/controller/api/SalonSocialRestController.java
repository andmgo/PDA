package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.application.service.SalonSocialService;
import gescazone.demo.infrastructure.util.GeneradorNumeros;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salones")
public class SalonSocialRestController {

    @Autowired
    private SalonSocialService salonSocialService;

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        return ResponseEntity.ok(salonSocialService.consultarTodos()
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/{numero}")
    public ResponseEntity<Map<String, Object>> obtenerPorNumero(@PathVariable String numero) {
        SalonSocialModel salon = salonSocialService.consultar(numero);
        if (salon == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(convertirAMap(salon));
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        String numero = (String) datos.get("numero");
        String nombreEstado = (String) datos.get("nombreEstado");

        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del salón social es obligatorio");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body("El estado es obligatorio");

        SalonSocialModel salon = new SalonSocialModel();
        salon.setNumero(numero);
        salon.setMedidas((String) datos.get("medidas"));
        salon.setEstado(new EstadoModel(nombreEstado.trim()));

        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object tel = datos.get("telefono");
            if (tel instanceof Number)
                salon.setTelefono(((Number) tel).longValue());
            else if (tel instanceof String) {
                try { salon.setTelefono(Long.parseLong((String) tel)); }
                catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
                }
            }
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(salonSocialService.crear(salon));
    }

    /**
     * Alta masiva: genera `cantidad` salones consecutivos a partir de
     * `numeroInicial` (ver GeneradorNumeros). Requiere el mismo permiso
     * SALONES EDITAR que /crear (ver SecurityConfig).
     */
    @PostMapping("/crear-varios")
    public ResponseEntity<Map<String, Object>> crearVarios(@RequestBody Map<String, Object> datos) {
        Object cantidadObj = datos.get("cantidad");
        if (cantidadObj == null)
            return ResponseEntity.badRequest().body(Map.of("error", "La cantidad es obligatoria"));

        String numeroInicial = (String) datos.get("numeroInicial");
        String nombreEstado = (String) datos.get("nombreEstado");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El estado es obligatorio"));

        Long telefono = null;
        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object tel = datos.get("telefono");
            try {
                telefono = tel instanceof Number ? ((Number) tel).longValue() : Long.parseLong((String) tel);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "El teléfono debe ser un número válido"));
            }
        }
        String medidas = (String) datos.get("medidas");

        List<String> numeros = GeneradorNumeros.generar(numeroInicial, ((Number) cantidadObj).intValue());

        List<String> creados = new ArrayList<>();
        List<Map<String, String>> omitidos = new ArrayList<>();

        for (String numero : numeros) {
            try {
                SalonSocialModel salon = new SalonSocialModel();
                salon.setNumero(numero);
                salon.setMedidas(medidas);
                salon.setTelefono(telefono);
                salon.setEstado(new EstadoModel(nombreEstado.trim()));

                salonSocialService.crear(salon);
                creados.add(numero);
            } catch (IllegalArgumentException e) {
                omitidos.add(Map.of("numero", numero, "motivo", e.getMessage()));
            }
        }

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("creados", creados);
        respuesta.put("omitidos", omitidos);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PutMapping("/actualizar/{numero}")
    public ResponseEntity<String> actualizar(@PathVariable String numero,
                                             @RequestBody Map<String, Object> datos) {
        SalonSocialModel salon = salonSocialService.consultar(numero);
        if (salon == null)
            throw new NotFoundException("No existe un salón social con el número: " + numero);

        String numeroNuevo = (String) datos.get("numero");
        if (numeroNuevo == null || numeroNuevo.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del salón social es obligatorio");

        salon.setNumero(numeroNuevo);

        if (datos.containsKey("medidas"))
            salon.setMedidas((String) datos.get("medidas"));

        if (datos.containsKey("nombreEstado")) {
            String nombreEstado = (String) datos.get("nombreEstado");
            if (nombreEstado != null && !nombreEstado.trim().isEmpty())
                salon.setEstado(new EstadoModel(nombreEstado.trim()));
        }

        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object tel = datos.get("telefono");
            if (tel instanceof Number)
                salon.setTelefono(((Number) tel).longValue());
            else if (tel instanceof String) {
                try { salon.setTelefono(Long.parseLong((String) tel)); }
                catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
                }
            }
        }

        return ResponseEntity.ok(salonSocialService.actualizar(salon));
    }

    @DeleteMapping("/eliminar/{numero}")
    public ResponseEntity<String> eliminar(@PathVariable String numero) {
        return ResponseEntity.ok(salonSocialService.eliminar(numero));
    }

    @PostMapping("/cambiar-estado")
    public ResponseEntity<String> cambiarEstado(@RequestBody Map<String, Object> datos) {
        String numero = (String) datos.get("numero");
        String nombreEstado = (String) datos.get("nombreEstado");

        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del salón social es obligatorio");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body("El nombre del estado es obligatorio");

        return ResponseEntity.ok(salonSocialService.setEstado(numero, nombreEstado));
    }

    @GetMapping("/estado-actual/{numero}")
    public ResponseEntity<Map<String, Object>> obtenerEstado(@PathVariable String numero) {
        SalonSocialModel salon = salonSocialService.consultar(numero);
        if (salon == null)
            return ResponseEntity.notFound().build();

        EstadoModel estado = salonSocialService.getEstado(numero);
        Map<String, Object> response = new HashMap<>();
        response.put("numero", numero);
        response.put("nombreEstado", estado.getNombreEstado());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/estado/{nombreEstado}")
    public ResponseEntity<List<Map<String, Object>>> consultarPorEstado(@PathVariable String nombreEstado) {
        return ResponseEntity.ok(salonSocialService.consultarPorEstado(nombreEstado)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/medida/{medida}")
    public ResponseEntity<List<Map<String, Object>>> consultarPorMedida(@PathVariable String medida) {
        return ResponseEntity.ok(salonSocialService.consultarPorMedida(medida)
                .stream().map(this::convertirAMap).toList());
    }

    private Map<String, Object> convertirAMap(SalonSocialModel salon) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", salon.getId());
        map.put("numero", salon.getNumero());
        map.put("medidas", salon.getMedidas());
        map.put("telefono", salon.getTelefono());
        if (salon.getEstado() != null)
            map.put("nombreEstado", salon.getEstado().getNombreEstado());
        return map;
    }
}
