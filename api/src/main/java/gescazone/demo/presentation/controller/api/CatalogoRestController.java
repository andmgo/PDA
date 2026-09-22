package gescazone.demo.presentation.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalogos")
public class CatalogoRestController {

    @GetMapping("/todos")
    public Map<String, Object> todos() {
        Map<String, Object> data = new HashMap<>();

        data.put("roles", List.of(
            Map.of("id", 1, "nombre", "Administrador"),
            Map.of("id", 2, "nombre", "Propietario"),
            Map.of("id", 3, "nombre", "Funcionario")
        ));

        data.put("tiposDocumento", List.of(
            Map.of("id", 1, "nombre", "CC"),
            Map.of("id", 2, "nombre", "TI"),
            Map.of("id", 3, "nombre", "CE")
        ));

        data.put("estados", List.of(
            Map.of("id", 1, "nombre", "Disponible"),
            Map.of("id", 2, "nombre", "Ocupado")
        ));

        data.put("estadosCuenta", List.of(
            Map.of("id", 1, "nombre", "Al día"),
            Map.of("id", 2, "nombre", "En mora")
        ));

        data.put("tiposOcupacion", List.of(
            Map.of("id", 1, "nombre", "Propietario"),
            Map.of("id", 2, "nombre", "Arrendado")
        ));

        data.put("tiposResidente", List.of(
            Map.of("id", 1, "nombre", "Propietario"),
            Map.of("id", 2, "nombre", "Arrendatario"),
            Map.of("id", 3, "nombre", "Visitante")
        ));

        return data;
    }
}