package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.service.SolicitudRegistroService;
import gescazone.demo.application.service.UsuarioService;
import gescazone.demo.domain.model.SolicitudRegistroModel;
import gescazone.demo.domain.model.UsuarioModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Cola de solicitudes de autoregistro (reemplaza al antiguo /api/auth/registro
 * directo). Crear es público — quien se registra todavía no tiene cuenta;
 * revisar (aprobar/rechazar/listar) exige el mismo permiso USUARIOS-EDITAR
 * que ya protege crear/editar/eliminar usuarios, ver SecurityConfig.
 */
@RestController
@RequestMapping("/api/solicitudes-registro")
public class SolicitudRegistroRestController {

    @Autowired
    private SolicitudRegistroService solicitudRegistroService;

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<String> crear(@RequestBody Map<String, String> datos) {
        String mensaje = solicitudRegistroService.crear(
                datos.get("numeroDocumento"), datos.get("nombre"), datos.get("apellido"),
                datos.get("correo"), datos.get("contrasena"), datos.get("nombreTipoDocumento"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @GetMapping("/pendientes")
    public ResponseEntity<List<SolicitudRegistroModel>> pendientes() {
        return ResponseEntity.ok(solicitudRegistroService.listarPendientes());
    }

    @GetMapping
    public ResponseEntity<List<SolicitudRegistroModel>> todas() {
        return ResponseEntity.ok(solicitudRegistroService.listarTodas());
    }

    @PostMapping("/{id}/aprobar")
    public ResponseEntity<String> aprobar(@PathVariable String id, @RequestBody Map<String, String> datos,
                                          Authentication authentication) {
        UsuarioModel admin = usuarioActual(authentication);
        return ResponseEntity.ok(solicitudRegistroService.aprobar(id, admin.getId(), datos.get("nombreRol")));
    }

    @PostMapping("/{id}/rechazar")
    public ResponseEntity<String> rechazar(@PathVariable String id,
                                           @RequestBody(required = false) Map<String, String> datos,
                                           Authentication authentication) {
        UsuarioModel admin = usuarioActual(authentication);
        String motivo = datos != null ? datos.get("motivo") : null;
        return ResponseEntity.ok(solicitudRegistroService.rechazar(id, admin.getId(), motivo));
    }

    private UsuarioModel usuarioActual(Authentication authentication) {
        UsuarioModel usuario = usuarioService.consultar(authentication.getName());
        if (usuario == null) throw new NotFoundException("Usuario autenticado no encontrado");
        return usuario;
    }
}
