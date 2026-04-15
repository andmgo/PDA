package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.*;
import gescazone.demo.domain.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accesos")
@CrossOrigin(origins = "*")
public class AccesoRestController {

    @Autowired
    private ResidenteService residenteService;

    @Autowired
    private ApartamentoService apartamentoService;

    @Autowired
    private ParqueaderoService parqueaderoService;

    @Autowired
    private RegistroVisitanteService registroVisitanteService;

    @Autowired
    private RegistroParqueaderoVisitanteService registroParqueaderoVisitanteService;

    @Autowired
    private RegistroAccesoPiscinaService registroAccesoPiscinaService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String ahora() {
        return LocalDateTime.now().format(FORMATTER);
    }

    // ===== VISITANTES SIN VEHÍCULO =====

    @GetMapping("/visitante-sin-vehiculo/todos")
    public ResponseEntity<?> obtenerVisitantesSinVehiculo() {
        try {
            List<RegistroVisitanteModel> registros = registroVisitanteService.consultarTodos();

            List<Map<String, Object>> resultado = registros.stream().map(reg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reg.getId());
                map.put("idResidente", reg.getIdResidente());
                map.put("idApartamento", reg.getIdApartamento());
                map.put("fechaHoraEntrada", reg.getFechaHoraEntrada());
                map.put("fechaHoraSalida", reg.getFechaHoraSalida());
                map.put("activo", reg.isActivo());

                // Enriquecer con datos del residente
                ResidenteModel residente = residenteService.consultarPorId(reg.getIdResidente());
                if (residente != null) {
                    map.put("numeroDocumento", residente.getNumeroDocumento());
                    map.put("nombreCompleto", residente.getNombre() + " " + residente.getApellido());
                }

                // Enriquecer con datos del apartamento
                ApartamentoModel apartamento = apartamentoService.consultar(reg.getIdApartamento()) != null
                        ? buscarApartamentoPorId(reg.getIdApartamento())
                        : null;
                if (apartamento != null) {
                    map.put("numeroApartamento", apartamento.getNumero());
                }

                return map;
            }).toList();

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al consultar visitantes: " + e.getMessage());
        }
    }

    @PostMapping("/visitante-sin-vehiculo/registrar-entrada")
    public ResponseEntity<String> registrarEntradaSinVehiculo(@RequestBody Map<String, Object> datos) {
        try {
            Integer numeroDocumento = parsearDocumento(datos.get("numeroDocumento"));
            String numeroApartamento = (String) datos.get("numeroApartamento");

            if (numeroDocumento == null || numeroApartamento == null)
                return ResponseEntity.badRequest().body("Datos incompletos");

            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.badRequest()
                        .body("El documento " + numeroDocumento + " no está registrado");

            ApartamentoModel apartamento = apartamentoService.consultar(numeroApartamento.toUpperCase());
            if (apartamento == null)
                return ResponseEntity.badRequest()
                        .body("El apartamento " + numeroApartamento + " no existe");

            RegistroVisitanteModel registro = new RegistroVisitanteModel(
                    residente.getId(), apartamento.getId(), ahora());

            String resultado = registroVisitanteService.crear(registro, residente.getId(), apartamento.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar: " + e.getMessage());
        }
    }

    @PutMapping("/visitante-sin-vehiculo/registrar-salida/{id}")
    public ResponseEntity<String> registrarSalidaSinVehiculo(
            @PathVariable String id) {
        try {
            String resultado = registroVisitanteService.registrarSalida(id, ahora());
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar salida: " + e.getMessage());
        }
    }

    // ===== VISITANTES CON VEHÍCULO =====

    @GetMapping("/visitante-vehiculo/todos")
    public ResponseEntity<?> obtenerVisitantesConVehiculo() {
        try {
            List<RegistroParqueaderoVisitanteModel> registros = registroParqueaderoVisitanteService.consultarTodos();

            List<Map<String, Object>> resultado = registros.stream().map(reg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reg.getId());
                map.put("idResidente", reg.getIdResidente());
                map.put("idParqueadero", reg.getIdParqueadero());
                map.put("idApartamento", reg.getIdApartamento());
                map.put("placa", reg.getPlaca());
                map.put("fechaHoraEntrada", reg.getFechaHoraEntrada());
                map.put("fechaHoraSalida", reg.getFechaHoraSalida());
                map.put("activo", reg.isActivo());

                // Enriquecer con datos del residente
                ResidenteModel residente = residenteService.consultarPorId(reg.getIdResidente());
                if (residente != null) {
                    map.put("numeroDocumento", residente.getNumeroDocumento());
                    map.put("nombreCompleto", residente.getNombre() + " " + residente.getApellido());
                }

                // Enriquecer con datos del parqueadero
                ParqueaderoModel parqueadero = parqueaderoService.consultarPorId(reg.getIdParqueadero());
                if (parqueadero != null) {
                    map.put("numeroParqueadero", parqueadero.getNumero());
                }

                // Enriquecer con datos del apartamento
                if (reg.getIdApartamento() != null) {
                    ApartamentoModel apartamento = buscarApartamentoPorId(reg.getIdApartamento());
                    if (apartamento != null) {
                        map.put("numeroApartamento", apartamento.getNumero());
                    }
                }

                return map;
            }).toList();

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al consultar: " + e.getMessage());
        }
    }

    @PostMapping("/visitante-vehiculo/registrar-entrada")
    public ResponseEntity<String> registrarEntradaVehiculo(@RequestBody Map<String, Object> datos) {
        try {
            Integer numeroDocumento = parsearDocumento(datos.get("numeroDocumento"));
            String numeroApartamento = (String) datos.get("numeroApartamento");
            String numeroParqueadero = (String) datos.get("numeroParqueadero");
            String placa = (String) datos.get("placa");

            if (numeroDocumento == null || numeroApartamento == null || numeroParqueadero == null || placa == null)
                return ResponseEntity.badRequest().body("Datos incompletos");

            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.badRequest()
                        .body("El documento " + numeroDocumento + " no está registrado");

            // Verificar que es visitante
            if (residente.getTipoResidente() == null ||
                    !residente.getTipoResidente().getNombreTipoResidente().toLowerCase().contains("visitante"))
                return ResponseEntity.badRequest()
                        .body("El documento corresponde a un residente tipo '"
                                + (residente.getTipoResidente() != null
                                        ? residente.getTipoResidente().getNombreTipoResidente()
                                        : "desconocido")
                                + "', no a un visitante");

            ApartamentoModel apartamento = apartamentoService.consultar(numeroApartamento.toUpperCase());
            if (apartamento == null)
                return ResponseEntity.badRequest()
                        .body("El apartamento " + numeroApartamento + " no existe");

            ParqueaderoModel parqueadero = parqueaderoService.consultar(numeroParqueadero.toUpperCase());
            if (parqueadero == null)
                return ResponseEntity.badRequest()
                        .body("El parqueadero " + numeroParqueadero + " no existe");

            RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel(
                    residente.getId(), parqueadero.getId(), ahora(), placa.toUpperCase());
            registro.setIdApartamento(apartamento.getId());

            String resultado = registroParqueaderoVisitanteService.crear(
                    registro, residente.getId(), parqueadero.getId(), apartamento.getId());

            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar: " + e.getMessage());
        }
    }

    @PutMapping("/visitante-vehiculo/registrar-salida/{id}")
    public ResponseEntity<String> registrarSalidaVehiculo(
            @PathVariable String id) {

        try {
            String resultado = registroParqueaderoVisitanteService
                    .registrarSalida(id, ahora());

            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar salida: " + e.getMessage());
        }
    }

    // ===== ACCESO A PISCINA =====

    @GetMapping("/piscina/todos")
    public ResponseEntity<?> obtenerAccesosPiscina() {
        try {
            List<RegistroAccesoPiscinaModel> registros = registroAccesoPiscinaService.consultarTodos();

            List<Map<String, Object>> resultado = registros.stream().map(reg -> {
                Map<String, Object> map = new HashMap<>();
                map.put("id", reg.getId());
                map.put("idResidente", reg.getIdResidente());
                map.put("idApartamento", reg.getIdApartamento());
                map.put("fechaHora", reg.getFechaHora());

                // Enriquecer con datos del residente
                ResidenteModel residente = residenteService.consultarPorId(reg.getIdResidente());
                if (residente != null) {
                    map.put("numeroDocumento", residente.getNumeroDocumento());
                    map.put("nombreCompleto", residente.getNombre() + " " + residente.getApellido());
                }

                // Enriquecer con datos del apartamento
                ApartamentoModel apartamento = buscarApartamentoPorId(reg.getIdApartamento());
                if (apartamento != null) {
                    map.put("numeroApartamento", apartamento.getNumero());
                }

                return map;
            }).toList();

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al consultar: " + e.getMessage());
        }
    }

    @PostMapping("/piscina/registrar")
    public ResponseEntity<String> registrarAccesoPiscina(@RequestBody Map<String, Object> datos) {
        try {
            Integer numeroDocumento = parsearDocumento(datos.get("numeroDocumento"));
            String numeroApartamento = (String) datos.get("numeroApartamento");

            if (numeroDocumento == null || numeroApartamento == null)
                return ResponseEntity.badRequest().body("Datos incompletos");

            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.badRequest()
                        .body("El documento no corresponde a un residente registrado");

            ApartamentoModel apartamento = apartamentoService.consultar(numeroApartamento.toUpperCase());
            if (apartamento == null)
                return ResponseEntity.badRequest().body("El apartamento no existe");


            String resultado = registroAccesoPiscinaService.registrarIngreso(numeroApartamento, numeroDocumento);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al registrar: " + e.getMessage());
        }
    }

    @DeleteMapping("/piscina/eliminar/{id}")
    public ResponseEntity<String> eliminarAccesoPiscina(@PathVariable String id) {
        try {
            String resultado = registroAccesoPiscinaService.eliminar(id);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar: " + e.getMessage());
        }
    }

    @PutMapping("/piscina/modificar/{id}")
    public ResponseEntity<String> modificarAccesoPiscina(
            @PathVariable String id,
            @RequestBody Map<String, Object> datos) {
        try {
            Integer numeroDocumento = parsearDocumento(datos.get("numeroDocumento"));
            String numeroApartamento = (String) datos.get("numeroApartamento");

            if (numeroDocumento == null || numeroApartamento == null)
                return ResponseEntity.badRequest().body("Datos incompletos");

            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.badRequest()
                        .body("El documento no corresponde a un residente registrado");

            ApartamentoModel apartamento = apartamentoService.consultar(numeroApartamento.toUpperCase());
            if (apartamento == null)
                return ResponseEntity.badRequest().body("El apartamento no existe");

            String resultado = registroAccesoPiscinaService.modificar(id, residente.getId(), apartamento.getId());
            return ResponseEntity.ok(resultado);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al modificar: " + e.getMessage());
        }
    }

    // ===== HELPERS PRIVADOS =====

    private Integer parsearDocumento(Object docObj) {
        if (docObj instanceof String) return Integer.parseInt((String) docObj);
        if (docObj instanceof Integer) return (Integer) docObj;
        if (docObj instanceof Number) return ((Number) docObj).intValue();
        return null;
    }

    /**
     * Busca un apartamento por su ID de MongoDB.
     * ApartamentoService expone consultar(numero), así que iteramos la lista completa.
     * Si en el futuro agregas consultarPorId() al service, reemplaza este método.
     */
    private ApartamentoModel buscarApartamentoPorId(String id) {
        return apartamentoService.consultarTodos().stream()
                .filter(a -> id.equals(a.getId()))
                .findFirst()
                .orElse(null);
    }
}