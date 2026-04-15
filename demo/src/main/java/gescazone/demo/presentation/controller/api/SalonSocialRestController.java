package gescazone.demo.presentation.controller.api;

import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.application.service.SalonSocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/salones")
@CrossOrigin(origins = "*")
public class SalonSocialRestController {

    @Autowired
    private SalonSocialService salonSocialService;

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            return ResponseEntity.ok(salonSocialService.consultarTodos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{numero}")
    public ResponseEntity<Map<String, Object>> obtenerPorNumero(@PathVariable String numero) {
        try {
            SalonSocialModel salon = salonSocialService.consultar(numero);
            if (salon == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(convertirAMap(salon));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        try {
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el salón social: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{numero}")
    public ResponseEntity<String> actualizar(@PathVariable String numero,
                                             @RequestBody Map<String, Object> datos) {
        try {
            SalonSocialModel salon = salonSocialService.consultar(numero);
            if (salon == null)
                return ResponseEntity.badRequest().body("No existe un salón social con el número: " + numero);

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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el salón social: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{numero}")
    public ResponseEntity<String> eliminar(@PathVariable String numero) {
        try {
            return ResponseEntity.ok(salonSocialService.eliminar(numero));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el salón social: " + e.getMessage());
        }
    }

    @PostMapping("/cambiar-estado")
    public ResponseEntity<String> cambiarEstado(@RequestBody Map<String, Object> datos) {
        try {
            String numero = (String) datos.get("numero");
            String nombreEstado = (String) datos.get("nombreEstado");

            if (numero == null || numero.trim().isEmpty())
                return ResponseEntity.badRequest().body("El número del salón social es obligatorio");
            if (nombreEstado == null || nombreEstado.trim().isEmpty())
                return ResponseEntity.badRequest().body("El nombre del estado es obligatorio");

            return ResponseEntity.ok(salonSocialService.setEstado(numero, nombreEstado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado: " + e.getMessage());
        }
    }

    @GetMapping("/estado-actual/{numero}")
    public ResponseEntity<Map<String, Object>> obtenerEstado(@PathVariable String numero) {
        try {
            SalonSocialModel salon = salonSocialService.consultar(numero);
            if (salon == null)
                return ResponseEntity.notFound().build();

            EstadoModel estado = salonSocialService.getEstado(numero);
            Map<String, Object> response = new HashMap<>();
            response.put("numero", numero);
            response.put("nombreEstado", estado.getNombreEstado());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/estado/{nombreEstado}")
    public ResponseEntity<List<Map<String, Object>>> consultarPorEstado(@PathVariable String nombreEstado) {
        try {
            return ResponseEntity.ok(salonSocialService.consultarPorEstado(nombreEstado)
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/medida/{medida}")
    public ResponseEntity<List<Map<String, Object>>> consultarPorMedida(@PathVariable String medida) {
        try {
            return ResponseEntity.ok(salonSocialService.consultarPorMedida(medida)
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
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