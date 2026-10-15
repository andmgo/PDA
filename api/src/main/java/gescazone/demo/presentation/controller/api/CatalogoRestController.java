package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/**
 * Catálogos para llenar los selectores de la web. Antes devolvía listas
 * escritas a mano que NO coincidían con la base ("Administrador" vs
 * "ADMINISTRADOR", tipo de ocupación "Propietario" que no existía,
 * faltaban "Mantenimiento" y "Pasaporte"…) y, al guardar, el repositorio
 * creaba esos valores como filas nuevas. Ahora sale de la base de datos.
 * Formato de cada opción ({id, nombre}) sin cambios para no romper el JS.
 */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoRestController {

    @Autowired
    private CatalogoService catalogoService;

    @GetMapping("/todos")
    public Map<String, Object> todos() {
        Map<String, Object> data = new LinkedHashMap<>();
        catalogoService.todos().forEach((clave, nombres) -> data.put(clave, opciones(nombres)));
        return data;
    }

    private static List<Map<String, Object>> opciones(List<String> nombres) {
        return IntStream.range(0, nombres.size())
                .mapToObj(i -> Map.<String, Object>of("id", i + 1, "nombre", nombres.get(i)))
                .toList();
    }
}
