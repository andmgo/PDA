package gescazone.demo.presentation.controller.api;

import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.application.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioRestController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            return ResponseEntity.ok(usuarioService.consultarTodos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{numeroDocumento}")
    public ResponseEntity<Map<String, Object>> obtenerPorDocumento(@PathVariable String numeroDocumento) {
        try {
            UsuarioModel usuario = usuarioService.consultar(numeroDocumento);
            if (usuario == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(convertirAMap(usuario));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/crear")
    public ResponseEntity<String> crear(@RequestBody Map<String, Object> datos) {
        try {
            String numeroDocumento = (String) datos.get("numeroDocumento");
            String contrasena = (String) datos.get("contrasena");
            String nombreRol = (String) datos.get("nombreRol");
            String nombreTipoDocumento = (String) datos.get("nombreTipoDocumento");

            if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
                return ResponseEntity.badRequest().body("El número de documento es obligatorio");
            if (contrasena == null || contrasena.trim().isEmpty())
                return ResponseEntity.badRequest().body("La contraseña es obligatoria");
            if (nombreRol == null || nombreRol.trim().isEmpty())
                return ResponseEntity.badRequest().body("El rol es obligatorio");
            if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty())
                return ResponseEntity.badRequest().body("El tipo de documento es obligatorio");

            UsuarioModel usuario = new UsuarioModel();
            usuario.setNumeroDocumento(numeroDocumento);
            usuario.setNombre((String) datos.get("nombre"));
            usuario.setApellido((String) datos.get("apellido"));
            usuario.setCorreo((String) datos.get("correo"));
            usuario.setContrasena(contrasena);
            usuario.setRol(new RolModel(nombreRol.trim()));
            usuario.setTipoDocumento(new TipoDocumentoModel(nombreTipoDocumento.trim()));

            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el usuario: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{numeroDocumento}")
    public ResponseEntity<String> actualizar(@PathVariable String numeroDocumento,
                                             @RequestBody Map<String, Object> datos) {
        try {
            UsuarioModel usuario = usuarioService.consultar(numeroDocumento);
            if (usuario == null)
                return ResponseEntity.badRequest().body("No existe un usuario con el documento: " + numeroDocumento);

            if (datos.containsKey("nombre"))
                usuario.setNombre((String) datos.get("nombre"));
            if (datos.containsKey("apellido"))
                usuario.setApellido((String) datos.get("apellido"));
            if (datos.containsKey("correo"))
                usuario.setCorreo((String) datos.get("correo"));
            if (datos.containsKey("nombreRol") && datos.get("nombreRol") != null)
                usuario.setRol(new RolModel(((String) datos.get("nombreRol")).trim()));
            if (datos.containsKey("nombreTipoDocumento") && datos.get("nombreTipoDocumento") != null)
                usuario.setTipoDocumento(new TipoDocumentoModel(((String) datos.get("nombreTipoDocumento")).trim()));

            return ResponseEntity.ok(usuarioService.actualizar(usuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el usuario: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{numeroDocumento}")
    public ResponseEntity<String> eliminar(@PathVariable String numeroDocumento) {
        try {
            return ResponseEntity.ok(usuarioService.eliminar(numeroDocumento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el usuario: " + e.getMessage());
        }
    }

    @PostMapping("/cambiar-contrasena")
    public ResponseEntity<String> cambiarContrasena(@RequestBody Map<String, String> datos) {
        try {
            String numeroDocumento = datos.get("numeroDocumento");
            String contrasenaActual = datos.get("contrasenaActual");
            String contrasenaNueva = datos.get("contrasenaNueva");
            String confirmarContrasena = datos.get("confirmarContrasena");

            if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
                return ResponseEntity.badRequest().body("El número de documento es obligatorio");
            if (contrasenaActual == null || contrasenaActual.trim().isEmpty())
                return ResponseEntity.badRequest().body("La contraseña actual es obligatoria");
            if (contrasenaNueva == null || contrasenaNueva.trim().isEmpty())
                return ResponseEntity.badRequest().body("La contraseña nueva es obligatoria");
            if (!contrasenaNueva.equals(confirmarContrasena))
                return ResponseEntity.badRequest().body("Las contraseñas nuevas no coinciden");

            return ResponseEntity.ok(usuarioService.cambiarContrasena(numeroDocumento, contrasenaActual, contrasenaNueva));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar la contraseña: " + e.getMessage());
        }
    }

    private Map<String, Object> convertirAMap(UsuarioModel usuario) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", usuario.getId());
        map.put("numeroDocumento", usuario.getNumeroDocumento());
        map.put("nombre", usuario.getNombre());
        map.put("apellido", usuario.getApellido());
        map.put("correo", usuario.getCorreo());
        if (usuario.getRol() != null)
            map.put("nombreRol", usuario.getRol().getNombreRol());
        if (usuario.getTipoDocumento() != null)
            map.put("nombreTipoDocumento", usuario.getTipoDocumento().getNombreTipoDocumento());
        return map;
    }
}