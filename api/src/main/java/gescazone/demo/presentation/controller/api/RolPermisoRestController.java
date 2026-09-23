package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PermisoService;
import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Backend de la pantalla "Roles y permisos": permite crear roles nuevos y
 * editar qué puede ver/editar cada uno, sin tocar código ni redeployar.
 * Cada escritura invalida la caché de PermisoService para que el cambio
 * tenga efecto inmediato en la siguiente petición.
 */
@RestController
@RequestMapping("/api/roles-permisos")
public class RolPermisoRestController {

    @Autowired
    private RolPermisoRepository rolPermisoRepository;

    @Autowired
    private PermisoService permisoService;

    @GetMapping("/matriz")
    public ResponseEntity<?> obtenerMatriz() {
        List<PermisoModel> permisos = rolPermisoRepository.listarPermisos();
        List<String> roles = rolPermisoRepository.listarNombresRoles();
        List<RolPermisoModel> celdas = rolPermisoRepository.matrizCompleta();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permisos", permisos);
        resultado.put("roles", roles);
        resultado.put("celdas", celdas);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/roles")
    public ResponseEntity<String> crearRol(@RequestBody Map<String, String> datos) {
        String nombreRol = datos.get("nombreRol");
        if (nombreRol == null || nombreRol.trim().isEmpty())
            return ResponseEntity.badRequest().body("El nombre del rol no puede estar vacío");

        rolPermisoRepository.crearRol(nombreRol.trim().toUpperCase());
        permisoService.invalidarCache();
        return ResponseEntity.status(HttpStatus.CREATED).body("Rol creado exitosamente");
    }

    @PutMapping("/celda")
    public ResponseEntity<String> guardarCelda(@RequestBody Map<String, Object> datos) {
        String nombreRol = (String) datos.get("nombreRol");
        String codigoPermiso = (String) datos.get("codigoPermiso");
        boolean puedeVer = Boolean.TRUE.equals(datos.get("puedeVer"));
        boolean puedeEditar = Boolean.TRUE.equals(datos.get("puedeEditar"));

        if (nombreRol == null || codigoPermiso == null)
            return ResponseEntity.badRequest().body("nombreRol y codigoPermiso son obligatorios");

        rolPermisoRepository.guardarPermiso(nombreRol, codigoPermiso, puedeVer, puedeEditar);
        permisoService.invalidarCache();
        return ResponseEntity.ok("Permiso actualizado exitosamente");
    }

    @DeleteMapping("/roles/{nombreRol}")
    public ResponseEntity<String> eliminarRol(@PathVariable String nombreRol) {
        rolPermisoRepository.eliminarRol(nombreRol);
        permisoService.invalidarCache();
        return ResponseEntity.ok("Rol eliminado exitosamente");
    }
}
