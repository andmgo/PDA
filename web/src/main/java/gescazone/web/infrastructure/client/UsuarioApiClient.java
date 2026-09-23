package gescazone.web.infrastructure.client;

import gescazone.web.dto.UsuarioResumen;
import gescazone.web.infrastructure.security.SesionUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Llamadas puntuales a /api/usuarios/** que hace Web directamente (no vía el proxy JS). */
@Component
public class UsuarioApiClient {

    private final RestClient restClient;

    @Autowired
    private SesionUsuario sesionUsuario;

    public UsuarioApiClient(@Value("${api.base-url}") String apiBaseUrl) {
        this.restClient = RestClient.builder().baseUrl(apiBaseUrl).build();
    }

    public UsuarioResumen consultar(String numeroDocumento) {
        return restClient.get()
                .uri("/api/usuarios/{doc}", numeroDocumento)
                .header("Authorization", "Bearer " + sesionUsuario.getJwt())
                .retrieve()
                .body(UsuarioResumen.class);
    }
}
