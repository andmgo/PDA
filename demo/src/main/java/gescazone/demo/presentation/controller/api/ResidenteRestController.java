package gescazone.demo.presentation.controller.api;

import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.TipoResidenteModel;
import gescazone.demo.application.service.ResidenteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/residentes")
@CrossOrigin(origins = "*")
public class ResidenteRestController {

    @Autowired
    private ResidenteService residenteService;

    @GetMapping("/todos")
    public ResponseEntity<List<Map<String, Object>>> obtenerTodos() {
        try {
            return ResponseEntity.ok(residenteService.consultarTodos()
                    .stream().map(this::convertirAMap).toList());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{numeroDocumento}")
    public ResponseEntity<Map<String, Object>> obtenerPorDocumento(@PathVariable Integer numeroDocumento) {
        try {
            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.notFound().build();
            return ResponseEntity.ok(convertirAMap(residente));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tipo/{nombreTipoResidente}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorTipo(@PathVariable String nombreTipoResidente) {
        try {
            return ResponseEntity.ok(residenteService.consultarPorTipo(nombreTipoResidente)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/tipo-documento/{nombreTipoDocumento}")
    public ResponseEntity<List<Map<String, Object>>> obtenerPorTipoDocumento(@PathVariable String nombreTipoDocumento) {
        try {
            return ResponseEntity.ok(residenteService.consultarPorTipoDeDocumento(nombreTipoDocumento)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Map<String, Object>>> buscarPorNombre(@RequestParam String nombre) {
        try {
            return ResponseEntity.ok(residenteService.buscarPorNombre(nombre)
                    .stream().map(this::convertirAMap).toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/buscar/apellido")
    public ResponseEntity<List<Map<String, Object>>> buscarPorApellido(@RequestParam String apellido) {
        try {
            return ResponseEntity.ok(residenteService.buscarPorApellido(apellido)
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
            String nombreTipoDocumento = (String) datos.get("nombreTipoDocumento");
            String nombreTipoResidente = (String) datos.get("nombreTipoResidente");

            if (!datos.containsKey("numeroDocumento"))
                return ResponseEntity.badRequest().body("El número de documento es obligatorio");
            if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty())
                return ResponseEntity.badRequest().body("El tipo de documento es obligatorio");
            if (nombreTipoResidente == null || nombreTipoResidente.trim().isEmpty())
                return ResponseEntity.badRequest().body("El tipo de residente es obligatorio");

            ResidenteModel residente = new ResidenteModel();
            residente.setNumeroDocumento(((Number) datos.get("numeroDocumento")).intValue());
            residente.setNombre((String) datos.get("nombre"));
            residente.setApellido((String) datos.get("apellido"));
            if (datos.containsKey("celular") && datos.get("celular") != null)
                residente.setCelular(((Number) datos.get("celular")).longValue());
            residente.setTipoDocumento(new TipoDocumentoModel(nombreTipoDocumento.trim()));
            residente.setTipoResidente(new TipoResidenteModel(nombreTipoResidente.trim()));

            return ResponseEntity.status(HttpStatus.CREATED).body(residenteService.crear(residente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el residente: " + e.getMessage());
        }
    }

    @PutMapping("/actualizar/{numeroDocumento}")
    public ResponseEntity<String> actualizar(@PathVariable Integer numeroDocumento,
                                             @RequestBody Map<String, Object> datos) {
        try {
            ResidenteModel residente = residenteService.consultar(numeroDocumento);
            if (residente == null)
                return ResponseEntity.badRequest().body("No existe un residente con el documento: " + numeroDocumento);

            if (datos.containsKey("nombre"))
                residente.setNombre((String) datos.get("nombre"));
            if (datos.containsKey("apellido"))
                residente.setApellido((String) datos.get("apellido"));
            if (datos.containsKey("celular") && datos.get("celular") != null)
                residente.setCelular(((Number) datos.get("celular")).longValue());
            if (datos.containsKey("nombreTipoDocumento") && datos.get("nombreTipoDocumento") != null)
                residente.setTipoDocumento(new TipoDocumentoModel(((String) datos.get("nombreTipoDocumento")).trim()));
            if (datos.containsKey("nombreTipoResidente") && datos.get("nombreTipoResidente") != null)
                residente.setTipoResidente(new TipoResidenteModel(((String) datos.get("nombreTipoResidente")).trim()));

            return ResponseEntity.ok(residenteService.actualizar(residente));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el residente: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar/{numeroDocumento}")
    public ResponseEntity<String> eliminar(@PathVariable Integer numeroDocumento) {
        try {
            return ResponseEntity.ok(residenteService.eliminar(numeroDocumento));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el residente: " + e.getMessage());
        }
    }

    private Map<String, Object> convertirAMap(ResidenteModel res) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", res.getId());
        map.put("numeroDocumento", res.getNumeroDocumento());
        map.put("nombre", res.getNombre());
        map.put("apellido", res.getApellido());
        map.put("celular", res.getCelular());
        if (res.getTipoDocumento() != null)
            map.put("nombreTipoDocumento", res.getTipoDocumento().getNombreTipoDocumento());
        if (res.getTipoResidente() != null)
            map.put("nombreTipoResidente", res.getTipoResidente().getNombreTipoResidente());
        return map;
    }
}