package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PermisoService;
import gescazone.demo.domain.model.PermisoModel;
import gescazone.demo.domain.model.RolPermisoModel;
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
 * Todo pasa por PermisoService, que invalida su propia caché en cada
 * escritura para que el cambio tenga efecto en la siguiente petición.
 */
@RestController
@RequestMapping("/api/roles-permisos")
public class RolPermisoRestController {

    @Autowired
    private PermisoService permisoService;

    @GetMapping("/matriz")
    public ResponseEntity<?> obtenerMatriz() {
        List<PermisoModel> permisos = permisoService.listarPermisos();
        List<String> roles = permisoService.listarRoles();
        List<RolPermisoModel> celdas = permisoService.matriz();

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("permisos", permisos);
        resultado.put("roles", roles);
        resultado.put("celdas", celdas);
        return ResponseEntity.ok(resultado);
    }

    @PostMapping("/roles")
    public ResponseEntity<String> crearRol(@RequestBody Map<String, String> datos) {
        permisoService.crearRol(datos.get("nombreRol"));
        return ResponseEntity.status(HttpStatus.CREATED).body("Rol creado exitosamente");
    }

    @PutMapping("/celda")
    public ResponseEntity<String> guardarCelda(@RequestBody Map<String, Object> datos) {
        String nombreRol = (String) datos.get("nombreRol");
        String codigoPermiso = (String) datos.get("codigoPermiso");
        boolean puedeVer = Boolean.TRUE.equals(datos.get("puedeVer"));
        boolean puedeEditar = Boolean.TRUE.equals(datos.get("puedeEditar"));

        permisoService.guardarPermiso(nombreRol, codigoPermiso, puedeVer, puedeEditar);
        return ResponseEntity.ok("Permiso actualizado exitosamente");
    }

    @DeleteMapping("/roles/{nombreRol}")
    public ResponseEntity<String> eliminarRol(@PathVariable String nombreRol) {
        permisoService.eliminarRol(nombreRol);
        return ResponseEntity.ok("Rol eliminado exitosamente");
    }
}
