package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.service.ApartamentoService;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.TipoOcupacionModel;
import gescazone.demo.domain.model.EstadoCuentaModel;
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
@RequestMapping("/api/apartamentos")
public class ApartamentoRestController {

    @Autowired
    private ApartamentoService apartamentoService;

    @GetMapping("/todos")
    public ResponseEntity<?> obtenerTodos() {
        List<ApartamentoModel> apartamentos = apartamentoService.consultarTodos();

        List<Map<String, Object>> response = apartamentos.stream().map(apt -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", apt.getId());
            map.put("numero", apt.getNumero());
            map.put("medidas", apt.getMedidas());
            map.put("telefono", apt.getTelefono());

            if (apt.getTipoOcupacion() != null) {
                map.put("tipoOcupacion", apt.getTipoOcupacion().getNombreTipoOcupacion());
            }

            if (apt.getEstadoCuenta() != null) {
                map.put("estadoCuenta", apt.getEstadoCuenta().getNombreEstadoCuenta());
            }

            return map;
        }).toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{numero}")
    public ResponseEntity<?> obtenerPorNumero(@PathVariable String numero) {
        ApartamentoModel apartamento = apartamentoService.consultar(numero);
        if (apartamento == null)
            throw new NotFoundException("No existe un apartamento con el número: " + numero);

        Map<String, Object> response = new HashMap<>();
        response.put("id", apartamento.getId());
        response.put("numero", apartamento.getNumero());
        response.put("medidas", apartamento.getMedidas());
        response.put("telefono", apartamento.getTelefono());

        if (apartamento.getTipoOcupacion() != null) {
            response.put("tipoOcupacion", apartamento.getTipoOcupacion().getNombreTipoOcupacion());
        }

        if (apartamento.getEstadoCuenta() != null) {
            response.put("estadoCuenta", apartamento.getEstadoCuenta().getNombreEstadoCuenta());
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        String numero = (String) datos.get("numero");
        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número de apartamento es obligatorio");

        String nombreTipoOcupacion = (String) datos.get("tipoOcupacion");
        if (nombreTipoOcupacion == null || nombreTipoOcupacion.trim().isEmpty())
            return ResponseEntity.badRequest().body("El tipo de ocupación es obligatorio");

        String nombreEstadoCuenta = (String) datos.get("estadoCuenta");
        if (nombreEstadoCuenta == null || nombreEstadoCuenta.trim().isEmpty())
            return ResponseEntity.badRequest().body("El estado de cuenta es obligatorio");

        TipoOcupacionModel tipoOcupacion = new TipoOcupacionModel();
        tipoOcupacion.setNombreTipoOcupacion(nombreTipoOcupacion.trim());

        EstadoCuentaModel estadoCuenta = new EstadoCuentaModel();
        estadoCuenta.setNombreEstadoCuenta(nombreEstadoCuenta.trim());

        ApartamentoModel apartamento = new ApartamentoModel();
        apartamento.setNumero(numero.trim());
        apartamento.setMedidas((String) datos.get("medidas"));
        apartamento.setTipoOcupacion(tipoOcupacion);
        apartamento.setEstadoCuenta(estadoCuenta);

        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object telefonoObj = datos.get("telefono");
            if (telefonoObj instanceof Number) {
                apartamento.setTelefono(((Number) telefonoObj).longValue());
            } else if (telefonoObj instanceof String) {
                try {
                    apartamento.setTelefono(Long.parseLong((String) telefonoObj));
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
                }
            }
        }

        String resultado = apartamentoService.crear(apartamento);
        return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
    }

    /**
     * Alta masiva: genera `cantidad` apartamentos consecutivos a partir de
     * `numeroInicial` (ver GeneradorNumeros), todos con los mismos datos
     * comunes. No es transaccional a propósito — si uno ya existe se omite
     * y se sigue con los demás, en vez de revertir todo el lote.
     */
    @PostMapping("/crear-varios")
    public ResponseEntity<Map<String, Object>> crearVarios(@RequestBody Map<String, Object> datos) {
        Object cantidadObj = datos.get("cantidad");
        if (cantidadObj == null)
            return ResponseEntity.badRequest().body(Map.of("error", "La cantidad es obligatoria"));

        String numeroInicial = (String) datos.get("numeroInicial");
        String nombreTipoOcupacion = (String) datos.get("tipoOcupacion");
        String nombreEstadoCuenta = (String) datos.get("estadoCuenta");

        if (nombreTipoOcupacion == null || nombreTipoOcupacion.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El tipo de ocupación es obligatorio"));
        if (nombreEstadoCuenta == null || nombreEstadoCuenta.trim().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "El estado de cuenta es obligatorio"));

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
        }
        String medidas = (String) datos.get("medidas");

        List<String> numeros = GeneradorNumeros.generar(numeroInicial, ((Number) cantidadObj).intValue());

        List<String> creados = new ArrayList<>();
        List<Map<String, String>> omitidos = new ArrayList<>();

        for (String numero : numeros) {
            try {
                ApartamentoModel apartamento = new ApartamentoModel();
                apartamento.setNumero(numero);
                apartamento.setMedidas(medidas);
                apartamento.setTelefono(telefono);

                TipoOcupacionModel tipoOcupacion = new TipoOcupacionModel();
                tipoOcupacion.setNombreTipoOcupacion(nombreTipoOcupacion.trim());
                apartamento.setTipoOcupacion(tipoOcupacion);

                EstadoCuentaModel estadoCuenta = new EstadoCuentaModel();
                estadoCuenta.setNombreEstadoCuenta(nombreEstadoCuenta.trim());
                apartamento.setEstadoCuenta(estadoCuenta);

                apartamentoService.crear(apartamento);
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

    @PostMapping("/actualizar")
    public ResponseEntity<String> actualizar(@RequestBody Map<String, Object> datos) {
        String numeroActual = (String) datos.get("numeroActual");
        if (numeroActual == null || numeroActual.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número actual del apartamento es obligatorio");

        ApartamentoModel apartamento = apartamentoService.consultar(numeroActual.trim());
        if (apartamento == null)
            throw new NotFoundException("No existe un apartamento con el número: " + numeroActual);

        String numeroNuevo = (String) datos.get("numero");
        if (numeroNuevo == null || numeroNuevo.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número de apartamento es obligatorio");

        apartamento.setNumero(numeroNuevo.trim());

        if (datos.containsKey("medidas")) {
            apartamento.setMedidas((String) datos.get("medidas"));
        }

        if (datos.containsKey("tipoOcupacion") && datos.get("tipoOcupacion") != null) {
            TipoOcupacionModel tipoOcupacion = new TipoOcupacionModel();
            tipoOcupacion.setNombreTipoOcupacion(((String) datos.get("tipoOcupacion")).trim());
            apartamento.setTipoOcupacion(tipoOcupacion);
        }

        if (datos.containsKey("estadoCuenta") && datos.get("estadoCuenta") != null) {
            EstadoCuentaModel estadoCuenta = new EstadoCuentaModel();
            estadoCuenta.setNombreEstadoCuenta(((String) datos.get("estadoCuenta")).trim());
            apartamento.setEstadoCuenta(estadoCuenta);
        }

        if (datos.containsKey("telefono") && datos.get("telefono") != null) {
            Object telefonoObj = datos.get("telefono");
            if (telefonoObj instanceof Number) {
                apartamento.setTelefono(((Number) telefonoObj).longValue());
            } else if (telefonoObj instanceof String) {
                try {
                    apartamento.setTelefono(Long.parseLong((String) telefonoObj));
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("El teléfono debe ser un número válido");
                }
            }
        }

        String resultado = apartamentoService.actualizar(apartamento);
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/eliminar/{numero}")
    public ResponseEntity<String> eliminar(@PathVariable String numero) {
        if (numero == null || numero.trim().isEmpty())
            return ResponseEntity.badRequest().body("El número de apartamento es obligatorio");

        String resultado = apartamentoService.eliminar(numero.trim());
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/tipo-ocupacion/{nombre}")
    public ResponseEntity<?> consultarPorTipoOcupacion(@PathVariable String nombre) {
        List<ApartamentoModel> apartamentos = apartamentoService.consultarPorTipoOcupacion(nombre);
        return ResponseEntity.ok(apartamentos);
    }

    @GetMapping("/estado-cuenta/{nombre}")
    public ResponseEntity<?> consultarPorEstadoCuenta(@PathVariable String nombre) {
        List<ApartamentoModel> apartamentos = apartamentoService.consultarPorEstadoCuenta(nombre);
        return ResponseEntity.ok(apartamentos);
    }
}
