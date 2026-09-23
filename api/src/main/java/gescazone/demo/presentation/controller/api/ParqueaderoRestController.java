package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.service.ParqueaderoService;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
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
@RequestMapping("/api/parqueaderos")
public class ParqueaderoRestController {

    @Autowired
    private ParqueaderoService parqueaderoService;

    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTodos() {
        List<ParqueaderoModel> parqueaderos = parqueaderoService.consultarTodos();

        List<Map<String, Object>> response = parqueaderos.stream().map(parq -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", parq.getId());
            map.put("numero", parq.getNumero());
            map.put("medidas", parq.getMedidas());
            map.put("telefono", parq.getTelefono());
            map.put("activo", parq.isActivo());

            if (parq.getEstado() != null) {
                map.put("estado", parq.getEstado().getNombreEstado());
            }

            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<?> obtenerPorNumero(@PathVariable String numero) {
        ParqueaderoModel parqueadero = parqueaderoService.consultar(numero);
        if (parqueadero == null)
            throw new NotFoundException("No existe un parqueadero con el número: " + numero);

        Map<String, Object> response = new HashMap<>();
        response.put("id", parqueadero.getId());
        response.put("numero", parqueadero.getNumero());
        response.put("medidas", parqueadero.getMedidas());
        response.put("telefono", parqueadero.getTelefono());
        response.put("activo", parqueadero.isActivo());

        if (parqueadero.getEstado() != null) {
            response.put("estado", parqueadero.getEstado().getNombreEstado());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        String numero = (String) datos.get("numero");
        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del parqueadero es obligatorio");

        if (!numero.matches("^P-\\d{2,3}$"))
            return ResponseEntity.badRequest().body("El número debe tener el formato P-01, P-02, etc.");

        String medidas = (String) datos.get("medidas");
        if (medidas == null || medidas.trim().isEmpty())
            return ResponseEntity.badRequest().body("Las medidas son obligatorias");

        try {
            double medidasNum = Double.parseDouble(medidas.trim());
            if (medidasNum <= 0)
                return ResponseEntity.badRequest().body("Las medidas deben ser un número positivo");
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body("Las medidas deben ser un número válido");
        }

        if (!datos.containsKey("telefono") || datos.get("telefono") == null)
            return ResponseEntity.badRequest().body("El teléfono es obligatorio");

        Long telefono = null;
        Object telefonoObj = datos.get("telefono");
        if (telefonoObj instanceof Number) {
            telefono = ((Number) telefonoObj).longValue();
        } else if (telefonoObj instanceof String) {
            try {
                telefono = Long.parseLong((String) telefonoObj);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
            }
        }

        if (!String.valueOf(telefono).matches("^3\\d{9}$"))
            return ResponseEntity.badRequest().body("El teléfono debe iniciar con 3 y tener 10 dígitos");

        String nombreEstado = (String) datos.get("estado");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body("El estado es obligatorio");

        EstadoModel estado = new EstadoModel();
        estado.setNombreEstado(nombreEstado.trim());

        ParqueaderoModel parqueadero = new ParqueaderoModel();
        parqueadero.setNumero(numero.trim().toUpperCase());
        parqueadero.setMedidas(medidas.trim());
        parqueadero.setTelefono(telefono);
        parqueadero.setEstado(estado);

        String resultado = parqueaderoService.crear(parqueadero);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Alta masiva: genera `cantidad` parqueaderos consecutivos a partir de
     * `numeroInicial` (ver GeneradorNumeros). A diferencia de /crear, aquí
     * el teléfono es opcional — no tiene sentido pedir el mismo número para
     * varios parqueaderos nuevos.
     */
    @PostMapping("/crear-varios")
    public ResponseEntity<Map<String, Object>> crearVarios(@RequestBody Map<String, Object> datos) {
        Object cantidadObj = datos.get("cantidad");
        if (cantidadObj == null)
            return ResponseEntity.badRequest().body(Map.of("error", "La cantidad es obligatoria"));

        String numeroInicial = (String) datos.get("numeroInicial");
        String nombreEstado = (String) datos.get("estado");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El estado es obligatorio"));

        Long telefono = null;
        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object telefonoObj = datos.get("telefono");
            try {
                telefono = telefonoObj instanceof Number
                        ? ((Number) telefonoObj).longValue()
                        : Long.parseLong((String) telefonoObj);
            } catch (NumberFormatException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "El teléfono debe ser un número válido"));
            }
            if (!String.valueOf(telefono).matches("^3\\d{9}$"))
                return ResponseEntity.badRequest().body(Map.of("error", "El teléfono debe iniciar con 3 y tener 10 dígitos"));
        }
        String medidas = (String) datos.get("medidas");

        List<String> numeros = GeneradorNumeros.generar(
                numeroInicial != null ? numeroInicial.toUpperCase() : null, ((Number) cantidadObj).intValue());

        List<String> creados = new ArrayList<>();
        List<Map<String, String>> omitidos = new ArrayList<>();

        for (String numero : numeros) {
            try {
                EstadoModel estado = new EstadoModel();
                estado.setNombreEstado(nombreEstado.trim());

                ParqueaderoModel parqueadero = new ParqueaderoModel();
                parqueadero.setNumero(numero);
                parqueadero.setMedidas(medidas);
                parqueadero.setTelefono(telefono);
                parqueadero.setEstado(estado);

                parqueaderoService.crear(parqueadero);
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
        ParqueaderoModel parqueadero = parqueaderoService.consultar(numero);
        if (parqueadero == null)
            throw new NotFoundException("No existe un parqueadero con el número: " + numero);

        String numeroNuevo = (String) datos.get("numero");
        if (numeroNuevo == null || numeroNuevo.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del parqueadero es obligatorio");

        if (!numeroNuevo.matches("^P-\\d{2,3}$"))
            return ResponseEntity.badRequest().body("El número debe tener el formato P-01, P-02, etc.");

        parqueadero.setNumero(numeroNuevo.trim().toUpperCase());

        if (datos.containsKey("medidas") && datos.get("medidas") != null) {
            String medidas = (String) datos.get("medidas");
            if (!medidas.trim().isEmpty()) {
                try {
                    double medidasNum = Double.parseDouble(medidas.trim());
                    if (medidasNum <= 0)
                        return ResponseEntity.badRequest().body("Las medidas deben ser un número positivo");
                    parqueadero.setMedidas(medidas.trim());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("Las medidas deben ser un número válido");
                }
            }
        }

        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object telefonoObj = datos.get("telefono");
            Long telefono = null;
            if (telefonoObj instanceof Number) {
                telefono = ((Number) telefonoObj).longValue();
            } else if (telefonoObj instanceof String) {
                try {
                    telefono = Long.parseLong((String) telefonoObj);
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
                }
            }
            if (!String.valueOf(telefono).matches("^3\\d{9}$"))
                return ResponseEntity.badRequest().body("El teléfono debe iniciar con 3 y tener 10 dígitos");
            parqueadero.setTelefono(telefono);
        }

        if (datos.containsKey("estado") && datos.get("estado") != null) {
            EstadoModel estado = new EstadoModel();
            estado.setNombreEstado(((String) datos.get("estado")).trim());
            parqueadero.setEstado(estado);
        }

        String resultado = parqueaderoService.actualizar(parqueadero);
        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/desactivar/{numero}")
    public ResponseEntity<String> desactivar(@PathVariable String numero) {
        return ResponseEntity.ok(parqueaderoService.cambiarEstadoActivo(numero, false));
    }

    @PutMapping("/activar/{numero}")
    public ResponseEntity<String> activar(@PathVariable String numero) {
        return ResponseEntity.ok(parqueaderoService.cambiarEstadoActivo(numero, true));
    }

    @PostMapping("/cambiar-estado")
    public ResponseEntity<String> cambiarEstado(@RequestBody Map<String, Object> datos) {
        String numero = (String) datos.get("numero");
        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número del parqueadero es obligatorio");

        String nombreEstado = (String) datos.get("estado");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            return ResponseEntity.badRequest().body("El estado es obligatorio");

        String resultado = parqueaderoService.setEstado(numero.trim(), nombreEstado.trim());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/estado/{nombreEstado}")
    public ResponseEntity<?> consultarPorEstado(@PathVariable String nombreEstado) {
        List<ParqueaderoModel> parqueaderos = parqueaderoService.consultarPorEstado(nombreEstado);
        return ResponseEntity.ok(parqueaderos);
    }

    @GetMapping("/medida/{medida}")
    public ResponseEntity<?> consultarPorMedida(@PathVariable String medida) {
        List<ParqueaderoModel> parqueaderos = parqueaderoService.consultarPorMedida(medida);
        return ResponseEntity.ok(parqueaderos);
    }
}
