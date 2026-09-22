package gescazone.demo.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        // @Value no se resuelve fuera de un contexto Spring — se fija a mano.
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "clave-de-prueba-suficientemente-larga-para-hs256-0123456789");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86_400_000L);
    }

    private Authentication autenticacionPara(String numeroDocumento, String authority) {
        UserDetails userDetails = User.withUsername(numeroDocumento)
                .password("hash")
                .authorities(authority)
                .build();
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Test
    void generateToken_yLuegoExtraerNumeroDocumento_devuelveElMismoUsuario() {
        String token = jwtTokenProvider.generateToken(autenticacionPara("123456", "ROLE_ADMINISTRADOR"));

        assertThat(jwtTokenProvider.getNumeroDocumentoFromToken(token)).isEqualTo("123456");
    }

    @Test
    void validateToken_conTokenRecienGenerado_esValido() {
        String token = jwtTokenProvider.generateToken(autenticacionPara("123456", "ROLE_ADMINISTRADOR"));
        assertThat(jwtTokenProvider.validateToken(token)).isTrue();
    }

    @Test
    void validateToken_conTokenMalformado_retornaFalse() {
        assertThat(jwtTokenProvider.validateToken("esto-no-es-un-jwt")).isFalse();
    }

    @Test
    void validateToken_conTokenVacio_retornaFalse() {
        assertThat(jwtTokenProvider.validateToken("")).isFalse();
    }

    @Test
    void validateToken_conTokenYaExpirado_retornaFalse() {
        // Expiración negativa => la fecha de expiración queda en el pasado al generarlo.
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", -1000L);
        String token = jwtTokenProvider.generateToken(autenticacionPara("123456", "ROLE_ADMINISTRADOR"));

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }

    @Test
    void validateToken_firmadoConOtroSecreto_retornaFalse() {
        String token = jwtTokenProvider.generateToken(autenticacionPara("123456", "ROLE_ADMINISTRADOR"));

        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "otra-clave-completamente-distinta-tambien-larga-9876543210");

        assertThat(jwtTokenProvider.validateToken(token)).isFalse();
    }
}
