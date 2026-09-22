package gescazone.web.infrastructure.security;

import gescazone.web.dto.LoginResultado;
import gescazone.web.infrastructure.client.AuthApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;

/**
 * Reemplaza a DaoAuthenticationProvider + CustomUserDetailsService (que
 * consultaban la BD directo). Web no tiene acceso a la base de datos: en vez
 * de eso, le pide el login a gescazone-api y guarda el JWT + datos del
 * usuario en SesionUsuario para el resto de la sesión.
 */
@Component
public class ApiAuthenticationProvider implements AuthenticationProvider {

    @Autowired
    private AuthApiClient authApiClient;

    @Autowired
    private SesionUsuario sesionUsuario;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String numeroDocumento = authentication.getName();
        String contrasena = String.valueOf(authentication.getCredentials());

        LoginResultado resultado;
        try {
            resultado = authApiClient.login(numeroDocumento, contrasena);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new BadCredentialsException("Documento o contraseña incorrectos");
        } catch (Exception e) {
            throw new BadCredentialsException("No se pudo validar las credenciales. Intenta de nuevo.");
        }

        sesionUsuario.iniciarSesion(resultado);

        String authority = "ROLE_" + resultado.nombreRol().toUpperCase();
        return new UsernamePasswordAuthenticationToken(
                numeroDocumento, null, Collections.singletonList(new SimpleGrantedAuthority(authority)));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
