package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.ReservaSalonSocialService;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaSalonSocialRestController {

    private final ReservaSalonSocialService reservaService;

    public ReservaSalonSocialRestController(ReservaSalonSocialService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodas() {
        try {
            return ResponseEntity.ok(reservaService.listarReservas()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        try {
            ReservaSalonSocialModel r = reservaService.buscarPorId(id);

            if (r == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(convertirAMap(r));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/salon/{idSalon}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorSalon(@PathVariable String idSalon) {
        try {
            return ResponseEntity.ok(reservaService.buscarPorIdSalon(idSalon)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorUsuario(@PathVariable String idUsuario) {
        try {
            return ResponseEntity.ok(reservaService.buscarPorIdUsuario(idUsuario)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        try {
            return ResponseEntity.ok(reservaService.buscarPorRangoFechas(inicio, fin)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/salon/{idSalon}/futuras")
    public ResponseEntity<List<Map<String, Object>>> obtenerFuturasSalon(@PathVariable String idSalon) {
        try {
            return ResponseEntity.ok(reservaService.buscarReservasFuturasSalon(idSalon)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/usuario/{idUsuario}/futuras")
    public ResponseEntity<List<Map<String, Object>>> obtenerFuturasUsuario(@PathVariable String idUsuario) {
        try {
            return ResponseEntity.ok(reservaService.buscarReservasFuturasUsuario(idUsuario)
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
            String idUsuario     = (String) datos.get("idUsuario");
            String idSalon       = (String) datos.get("idSalon");
            String fechaHoraStr  = (String) datos.get("fechaYHoraReserva");

            if (idUsuario == null || idUsuario.isBlank())
                return ResponseEntity.badRequest().body("El ID del usuario es obligatorio");
            if (idSalon == null || idSalon.isBlank())
                return ResponseEntity.badRequest().body("El ID del salón es obligatorio");
            if (fechaHoraStr == null || fechaHoraStr.isBlank())
                return ResponseEntity.badRequest().body("La fecha y hora de la reserva son obligatorias");

            ReservaSalonSocialModel reserva = new ReservaSalonSocialModel(
                    idUsuario, idSalon, LocalDateTime.parse(fechaHoraStr));

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(reservaService.guardarReserva(reserva));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear la reserva: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizar(@PathVariable String id,
                                             @RequestBody Map<String, Object> datos) {
        try {
            String idUsuario    = (String) datos.get("idUsuario");
            String idSalon      = (String) datos.get("idSalon");
            String fechaHoraStr = (String) datos.get("fechaYHoraReserva");

            if (idUsuario == null || idUsuario.isBlank())
                return ResponseEntity.badRequest().body("El ID del usuario es obligatorio");
            if (idSalon == null || idSalon.isBlank())
                return ResponseEntity.badRequest().body("El ID del salón es obligatorio");
            if (fechaHoraStr == null || fechaHoraStr.isBlank())
                return ResponseEntity.badRequest().body("La fecha y hora de la reserva son obligatorias");

            ReservaSalonSocialModel reserva = new ReservaSalonSocialModel(
                    idUsuario, idSalon, LocalDateTime.parse(fechaHoraStr));
            reserva.setId(id);

            return ResponseEntity.ok(reservaService.guardarReserva(reserva));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la reserva: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        try {
            return ResponseEntity.ok(reservaService.eliminarReserva(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar la reserva: " + e.getMessage());
        }
    }

    private Map<String, Object> convertirAMap(ReservaSalonSocialModel res) {
        Map<String, Object> map = new HashMap<>();
        map.put("id",               res.getId());
        map.put("idUsuario",        res.getIdUsuario());
        map.put("idSalon",          res.getIdSalon());
        map.put("fechaYHoraReserva",res.getFechaYHoraReserva().toString());
        return map;
    }
}