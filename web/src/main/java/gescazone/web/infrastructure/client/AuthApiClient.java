package gescazone.web.infrastructure.client;

import gescazone.web.dto.LoginResultado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/** Envoltorio delgado sobre RestClient para hablar con /api/auth de gescazone-api. */
@Component
public class AuthApiClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AuthApiClient(@Value("${api.base-url}") String apiBaseUrl,
                          @Value("${internal.api-key}") String internalApiKey) {
        this.restClient = RestClient.builder().baseUrl(apiBaseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    /** Lanza HttpClientErrorException.Unauthorized (401) si las credenciales son inválidas. */
    public LoginResultado login(String numeroDocumento, String contrasena) {
        return restClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("numeroDocumento", numeroDocumento, "contrasena", contrasena))
                .retrieve()
                .body(LoginResultado.class);
    }

    /**
     * Login sin contraseña para "Continuar con Google" — correo ya
     * verificado por gescazone-web vía OIDC. Lanza
     * HttpClientErrorException.NotFound (404) si ningún Usuario tiene ese
     * correo (la vista debe ofrecer completar el registro en ese caso).
     */
    public LoginResultado loginGoogle(String correo) {
        return restClient.post()
                .uri("/api/auth/login-google")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Internal-Key", internalApiKey)
                .body(Map.of("correo", correo))
                .retrieve()
                .body(LoginResultado.class);
    }

    /**
     * Envía la solicitud de registro (queda PENDIENTE hasta que un
     * administrador la apruebe). Lanza HttpClientErrorException.BadRequest
     * (400, cuerpo = mensaje) si los datos no son válidos.
     */
    public void registro(Map<String, String> datos) {
        restClient.post()
                .uri("/api/solicitudes-registro")
                .contentType(MediaType.APPLICATION_JSON)
                .body(datos)
                .retrieve()
                .toBodilessEntity();
    }
}
