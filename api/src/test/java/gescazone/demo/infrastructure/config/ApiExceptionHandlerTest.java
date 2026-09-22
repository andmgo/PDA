package gescazone.demo.infrastructure.config;

import gescazone.demo.application.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleNotFound_devuelve404ConElMensaje() {
        ResponseEntity<String> respuesta = handler.handleNotFound(new NotFoundException("No existe el recurso"));
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isEqualTo("No existe el recurso");
    }

    @Test
    void handleValidation_devuelve400ConElMensaje() {
        ResponseEntity<String> respuesta = handler.handleValidation(new IllegalArgumentException("Campo obligatorio"));
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isEqualTo("Campo obligatorio");
    }

    @Test
    void handleIllegalState_devuelve400ConElMensaje() {
        ResponseEntity<String> respuesta = handler.handleIllegalState(new IllegalStateException("Ya fue procesado"));
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isEqualTo("Ya fue procesado");
    }

    @Test
    void handleGeneral_devuelve500ConMensajeGenerico() {
        ResponseEntity<String> respuesta = handler.handleGeneral(new RuntimeException("boom"));
        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(respuesta.getBody()).isEqualTo("Ha ocurrido un error inesperado");
    }
}
