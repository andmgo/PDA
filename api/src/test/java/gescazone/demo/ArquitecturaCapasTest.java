package gescazone.demo;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guardián de capas: falla si alguien vuelve a saltarse la arquitectura.
 * (Se encontraron 5 imports de domain.repository en controladores y
 * UsuarioService importando infrastructure.mail — corregidos.)
 *
 *   presentation → application → domain ← infrastructure
 *
 * - domain no conoce a nadie (ni application, ni infrastructure, ni presentation).
 * - application solo conoce domain (lo que necesite de afuera lo pide con
 *   una interfaz en application.port, que infrastructure implementa).
 * - presentation habla con application (servicios) y usa modelos de domain,
 *   nunca repositorios ni persistencia.
 */
class ArquitecturaCapasTest {

    private static final Path RAIZ = Path.of("src/main/java/gescazone/demo");

    /**
     * Única excepción consciente: el login (AuthController) usa la
     * infraestructura de seguridad de Spring (emitir JWT, cargar el usuario
     * para Google) — es un adaptador de entrada hablando con un adaptador de
     * seguridad; llevarlo a application metería Spring Security en el núcleo.
     */
    private static final List<String> EXCEPCIONES = List.of(
            "presentation/controller/api/AuthController.java -> gescazone.demo.infrastructure.security.");

    @Test
    void domainNoDependeDeOtrasCapas() throws IOException {
        assertThat(violaciones("domain", "gescazone.demo.application.", "gescazone.demo.infrastructure.",
                "gescazone.demo.presentation.")).isEmpty();
    }

    @Test
    void applicationNoDependeDeInfraestructuraNiPresentacion() throws IOException {
        assertThat(violaciones("application", "gescazone.demo.infrastructure.", "gescazone.demo.presentation."))
                .isEmpty();
    }

    @Test
    void presentationNoUsaRepositoriosNiPersistencia() throws IOException {
        assertThat(violaciones("presentation", "gescazone.demo.domain.repository.", "gescazone.demo.infrastructure."))
                .isEmpty();
    }

    private List<String> violaciones(String capa, String... prohibidos) throws IOException {
        List<String> encontradas = new ArrayList<>();
        try (Stream<Path> archivos = Files.walk(RAIZ.resolve(capa))) {
            for (Path archivo : archivos.filter(p -> p.toString().endsWith(".java")).toList()) {
                String relativo = RAIZ.relativize(archivo).toString().replace('\\', '/');
                for (String linea : Files.readAllLines(archivo)) {
                    String l = linea.trim();
                    if (!l.startsWith("import ")) continue;
                    for (String prohibido : prohibidos) {
                        if (l.contains(prohibido) && EXCEPCIONES.stream().noneMatch(e ->
                                e.startsWith(relativo + " -> ") && l.contains(e.substring(e.indexOf("-> ") + 3)))) {
                            encontradas.add(relativo + ": " + l);
                        }
                    }
                }
            }
        }
        return encontradas;
    }
}
