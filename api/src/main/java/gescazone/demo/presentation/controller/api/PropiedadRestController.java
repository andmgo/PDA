package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.PropiedadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Propietarios y vehículos de cada apartamento. Permisos en SecurityConfig:
 * consultar = APARTAMENTOS_PARQUEADEROS_RESIDENTES ver, modificar = editar,
 * buscar por placa = CONTROL_ACCESOS ver (portería), /mia = cualquier
 * usuario autenticado, siempre sobre su propia cuenta (sale del JWT, nunca
 * del cliente).
 */
@RestController
@RequestMapping("/api/propiedad")
public class PropiedadRestController {

    @Autowired
    private PropiedadService propiedadService;

    @GetMapping("/mia")
    public ResponseEntity<List<PropiedadService.DetalleApartamento>> mia(Authentication authentication) {
        return ResponseEntity.ok(propiedadService.deUsuario(authentication.getName()));
    }

    @GetMapping("/vehiculos/placa/{placa}")
    public ResponseEntity<PropiedadService.ResultadoPlaca> porPlaca(@PathVariable String placa) {
        return ResponseEntity.ok(propiedadService.buscarPorPlaca(placa));
    }

    @GetMapping("/apartamentos/{idApartamento}")
    public ResponseEntity<PropiedadService.DetalleApartamento> detalle(@PathVariable String idApartamento) {
        return ResponseEntity.ok(propiedadService.detalle(idApartamento));
    }

    @PostMapping("/apartamentos/{idApartamento}/propietarios")
    public ResponseEntity<String> asignarPropietario(@PathVariable String idApartamento,
                                                     @RequestBody Map<String, String> datos) {
        String mensaje = propiedadService.asignarPropietario(idApartamento, datos.get("identificador"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @DeleteMapping("/apartamentos/{idApartamento}/propietarios/{idUsuario}")
    public ResponseEntity<String> quitarPropietario(@PathVariable String idApartamento, @PathVariable String idUsuario) {
        return ResponseEntity.ok(propiedadService.quitarPropietario(idApartamento, idUsuario));
    }

    @PostMapping("/apartamentos/{idApartamento}/vehiculos")
    public ResponseEntity<String> registrarVehiculo(@PathVariable String idApartamento,
                                                    @RequestBody Map<String, String> datos) {
        String mensaje = propiedadService.registrarVehiculo(idApartamento, datos.get("placa"), datos.get("tipo"),
                datos.get("marca"), datos.get("color"), datos.get("numeroParqueadero"));
        return ResponseEntity.status(HttpStatus.CREATED).body(mensaje);
    }

    @PutMapping("/vehiculos/{id}/desactivar")
    public ResponseEntity<String> desactivarVehiculo(@PathVariable String id) {
        return ResponseEntity.ok(propiedadService.desactivarVehiculo(id));
    }
}
