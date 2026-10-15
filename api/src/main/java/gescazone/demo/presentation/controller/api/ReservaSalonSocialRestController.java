package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.ReservaSalonSocialService;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservas")
public class ReservaSalonSocialRestController {

    private final ReservaSalonSocialService reservaService;

    public ReservaSalonSocialRestController(ReservaSalonSocialService reservaService) {
        this.reservaService = reservaService;
    }

    @GetMapping("/todas")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodas() {
        return ResponseEntity.ok(reservaService.listarReservas()
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable String id) {
        ReservaSalonSocialModel r = reservaService.buscarPorId(id);
        if (r == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(convertirAMap(r));
    }

    @GetMapping("/salon/{idSalon}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorSalon(@PathVariable String idSalon) {
        return ResponseEntity.ok(reservaService.buscarPorIdSalon(idSalon)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(reservaService.buscarPorIdUsuario(idUsuario)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/fechas")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        return ResponseEntity.ok(reservaService.buscarPorRangoFechas(inicio, fin)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/salon/{idSalon}/futuras")
    public ResponseEntity<List<Map<String, Object>>> obtenerFuturasSalon(@PathVariable String idSalon) {
        return ResponseEntity.ok(reservaService.buscarReservasFuturasSalon(idSalon)
                .stream().map(this::convertirAMap).toList());
    }

    @GetMapping("/usuario/{idUsuario}/futuras")
    public ResponseEntity<List<Map<String, Object>>> obtenerFuturasUsuario(@PathVariable String idUsuario) {
        return ResponseEntity.ok(reservaService.buscarReservasFuturasUsuario(idUsuario)
                .stream().map(this::convertirAMap).toList());
    }

    /**
     * Crea la reserva a nombre de quien está autenticado — el idUsuario NO
     * viene del cliente, se resuelve del JWT. Reemplaza al viejo POST
     * /crear (recibía idUsuario del cliente sin verificarlo contra quién
     * hacía la petición; nunca hizo falta, y con eso ya no hacía falta el
     * paso extra de verificación con Google OAuth2 que reservas.js exigía
     * antes de crear cualquier reserva — el usuario del proyecto pidió
     * quitarlo, ver reservas.js/reservas.html).
     */
    @PostMapping("/crear-propia")
    public ResponseEntity<String> crearPropia(@RequestBody Map<String, Object> datos,
                                              Authentication authentication) {
        String idSalon      = (String) datos.get("idSalon");
        String fechaHoraStr = (String) datos.get("fechaYHoraReserva");

        if (idSalon == null || idSalon.isBlank())
            return ResponseEntity.badRequest().body("El ID del salón es obligatorio");
        if (fechaHoraStr == null || fechaHoraStr.isBlank())
            return ResponseEntity.badRequest().body("La fecha y hora de la reserva son obligatorias");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservaService.crearPropia(authentication.getName(), idSalon, LocalDateTime.parse(fechaHoraStr)));
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<String> actualizar(@PathVariable String id,
                                             @RequestBody Map<String, Object> datos) {
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
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<String> eliminar(@PathVariable String id) {
        return ResponseEntity.ok(reservaService.eliminarReserva(id));
    }

    /**
     * reservas.js espera objetos anidados, no strings planos:
     *
     *   reserva.idReserva                → res.getId()
     *   reserva.usuario.numeroDocumento  → número de documento real (resuelto aquí, no el UUID)
     *   reserva.salon.numero             → número de salón real (resuelto aquí, no el UUID)
     *   reserva.fechaYHoraReserva        → res.getFechaYHoraReserva().toString()
     */
    private Map<String, Object> convertirAMap(ReservaSalonSocialModel res) {
        Map<String, Object> usuario = new HashMap<>();
        usuario.put("numeroDocumento", reservaService.numeroDocumentoUsuario(res.getIdUsuario()));

        Map<String, Object> salon = new HashMap<>();
        salon.put("numero", reservaService.numeroSalon(res.getIdSalon()));

        Map<String, Object> map = new HashMap<>();
        map.put("idReserva",         res.getId());
        map.put("usuario",           usuario);
        map.put("salon",             salon);
        map.put("fechaYHoraReserva", res.getFechaYHoraReserva() != null
                ? res.getFechaYHoraReserva().toString() : null);
        return map;
    }
}
