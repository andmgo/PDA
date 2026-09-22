package gescazone.web.infrastructure.client;

import gescazone.web.infrastructure.security.SesionUsuario;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Reenvía TODO /api/** hacia gescazone-api adjuntando el JWT de SesionUsuario
 * — así el JavaScript de las plantillas (gestionDeDatos.js, reservas.js,
 * controlDeAccesos.js, pagosYCartera.js, profile.js) sigue llamando a las
 * mismas rutas relativas /api/... sin cambiar una línea. El navegador nunca
 * ve el JWT; el gate fino de permisos lo hace la Api con SecurityConfig+JWT,
 * este controller solo reenvía.
 */
@RestController
public class ApiProxyController {

    private static final Logger log = LoggerFactory.getLogger(ApiProxyController.class);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String apiBaseUrl;

    @Autowired
    private SesionUsuario sesionUsuario;

    public ApiProxyController(@Value("${api.base-url}") String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }

    @RequestMapping("/api/**")
    public ResponseEntity<byte[]> proxy(HttpServletRequest request) {
        try {
            byte[] cuerpoEntrante = request.getInputStream().readAllBytes();

            String query = request.getQueryString();
            String uri = apiBaseUrl + request.getRequestURI() + (query != null ? "?" + query : "");

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .timeout(Duration.ofSeconds(20));

            String contentType = request.getContentType();
            if (contentType != null) {
                builder.header("Content-Type", contentType);
            }
            if (sesionUsuario.getJwt() != null) {
                builder.header("Authorization", "Bearer " + sesionUsuario.getJwt());
            }

            builder.method(request.getMethod(), HttpRequest.BodyPublishers.ofByteArray(cuerpoEntrante));

            HttpResponse<byte[]> respuesta = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());

            ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(respuesta.statusCode());
            respuesta.headers().firstValue("Content-Type")
                    .ifPresent(responseBuilder::header);

            return responseBuilder.body(respuesta.body());
        } catch (IOException | InterruptedException e) {
            log.error("Error reenviando {} {} a gescazone-api", request.getMethod(), request.getRequestURI(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(
                    "No se pudo conectar con la Api".getBytes());
        }
    }
}
