package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.service.SolicitudReservaService;
import gescazone.demo.application.service.UsuarioService;
import gescazone.demo.domain.model.SolicitudReservaModel;
import gescazone.demo.domain.model.UsuarioModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Cola de solicitudes de reserva excepcional (fuera del año actual). Crear
 * es un permiso de RESERVAS (cualquiera con acceso a reservas); revisar
 * (aprobar/rechazar/listar) es GESTION_DATOS — solo Administrador, ver
 * SecurityConfig.
 */
@RestController
@RequestMapping("/api/solicitudes-reserva")
public class SolicitudReservaRestController {

    @Autowired
    private SolicitudReservaService solicitudReservaService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<String> crear(@RequestBody Map<String, String> datos, Authentication authentication) {
        UsuarioModel usuarioActual = usuarioActual(authentication);
        LocalDateTime fecha = LocalDateTime.parse(datos.get("fechaSolicitada"));
        String mensaje = solicitudReservaService.crear(
                usuarioActual.getId(), datos.get("idSalon"), fecha, datos.get("justificacion"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudReservaModel>> pendientes() {
        return ResponseEntity.ok(solicitudReservaService.listarPendientes());
    }

    @GetMapping
    public ResponseEntity<List<SolicitudReservaModel>> todas() {
        return ResponseEntity.ok(solicitudReservaService.listarTodas());
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<String> aprobar(@PathVariable String id, Authentication authentication) {
        UsuarioModel admin = usuarioActual(authentication);
        return ResponseEntity.ok(solicitudReservaService.aprobar(id, admin.getId()));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<String> rechazar(@PathVariable String id,
                                           @RequestBody(required = false) Map<String, String> datos,
                                           Authentication authentication) {
        UsuarioModel admin = usuarioActual(authentication);
        String motivo = datos != null ? datos.get("motivo") : null;
        return ResponseEntity.ok(solicitudReservaService.rechazar(id, admin.getId(), motivo));
    }

    private UsuarioModel usuarioActual(Authentication authentication) {
        UsuarioModel usuario = usuarioService.consultar(authentication.getName());
        if (usuario == null) throw new NotFoundException("Usuario autenticado no encontrado");
        return usuario;
    }
}
