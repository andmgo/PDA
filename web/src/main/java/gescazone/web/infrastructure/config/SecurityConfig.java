package gescazone.web.infrastructure.config;

import gescazone.web.dto.LoginResultado;
import gescazone.web.infrastructure.client.AuthApiClient;
import gescazone.web.infrastructure.security.ApiAuthenticationProvider;
import gescazone.web.infrastructure.security.SesionUsuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.function.Supplier;

/**
 * Login por sesión (form login) igual que antes de la separación, pero el
 * AuthenticationProvider ya no consulta la BD (ver ApiAuthenticationProvider)
 * — le pregunta a gescazone-api. Las reglas de ruta MVC consultan
 * SesionUsuario.tienePermiso(...) (dato ya cacheado desde el login) en vez
 * de PermisoService (que vive solo en la Api).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired private ApiAuthenticationProvider apiAuthenticationProvider;
    @Autowired private SesionUsuario sesionUsuario;
    @Autowired private AuthApiClient authApiClient;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    private AuthorizationManager<RequestAuthorizationContext> permiso(String codigo, boolean requiereEditar) {
        return (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
            Authentication auth = authentication.get();
            if (auth == null || !auth.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }
            return new AuthorizationDecision(sesionUsuario.tienePermiso(codigo, requiereEditar));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authenticationProvider(apiAuthenticationProvider)

            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                    "/login", "/registro", "/registro/completar-google", "/logout",
                    "/css/**", "/js/**", "/img/**", "/fonts/**",
                    "/favicon.ico", "/error"
                ).permitAll()

                .requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()

                .requestMatchers("/gestionDeDatos")
                    .access(permiso("GESTION_DATOS", false))

                .requestMatchers("/pagosYCartera")
                    .access(permiso("PAGOS_Y_CARTERA", false))

                .requestMatchers("/controlDeAccesos")
                    .access(permiso("CONTROL_ACCESOS", false))

                .requestMatchers("/rolesYPermisos")
                    .access(permiso("GESTION_DATOS", true))

                .requestMatchers("/paquetes")
                    .access(permiso("PAQUETES", false))

                // Proxy hacia gescazone-api — el gate fino de verdad (por rol y
                // nivel) lo hace la Api con el JWT; aquí solo se exige sesión.
                .requestMatchers("/api/**").authenticated()

                .anyRequest().authenticated()
            )

            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(formSuccessHandler())
                .failureHandler(formFailureHandler())
                .permitAll()
            )

            .oauth2Login(oauth -> oauth
                .loginPage("/login")
                .authorizationEndpoint(ep -> ep
                    .authorizationRequestRepository(
                        new HttpSessionOAuth2AuthorizationRequestRepository()
                    )
                )
                .successHandler(oauth2SuccessHandler())
                .failureHandler(oauth2FailureHandler())
            )

            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("GESCAZONE-SESSION")
                .clearAuthentication(true)
                .permitAll()
            )

            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/logout", "/oauth2/**")
            )

            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
                .expiredUrl("/login?error=session_expired")
            )

            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, e) -> {
                    request.setAttribute("codigo",  403);
                    request.setAttribute("mensaje", "No tienes permisos para acceder a esta sección.");
                    request.setAttribute("ruta",    request.getRequestURI());
                    request.getRequestDispatcher("/error").forward(request, response);
                })
            );

        return http.build();
    }

    // ── Handlers ─────────────────────────────────────────────────────────────

    private org.springframework.security.web.authentication.AuthenticationSuccessHandler
            formSuccessHandler() {
        return (request, response, authentication) -> {
            String rolVista = sesionUsuario.getNombreRolParaVista();

            HttpSession session = request.getSession();
            session.setAttribute("rolUsuario",      rolVista);
            session.setAttribute("usuarioLogueado", authentication.getName());

            response.sendRedirect("Funcionario".equals(rolVista) ? "/controlDeAccesos" : "/inicio");
        };
    }

    private org.springframework.security.web.authentication.AuthenticationFailureHandler
            formFailureHandler() {
        return (request, response, exception) -> {
            String error = switch (exception.getClass().getSimpleName()) {
                case "BadCredentialsException"        -> "bad_credentials";
                case "DisabledException"              -> "disabled";
                case "SessionAuthenticationException" -> "session_limit";
                default                               -> "true";
            };
            response.sendRedirect("/login?error=" + error);
        };
    }

    private org.springframework.security.web.authentication.AuthenticationSuccessHandler
            oauth2SuccessHandler() {
        return (request, response, authentication) -> {
            OidcUser oidcUser = (OidcUser) authentication.getPrincipal();
            HttpSession session = request.getSession(false);

            String proposito = session != null ? (String) session.getAttribute("oauth2Proposito") : null;

            // "Continuar con Google" (login) o "Registrarte con Google" —
            // distinto del flujo de abajo (verificación de identidad de un
            // usuario YA logueado, antes de pagar/reservar).
            if (session != null && ("LOGIN".equals(proposito) || "REGISTRO".equals(proposito))) {
                session.removeAttribute("oauth2Proposito");
                guardarDatosGoogleEnSesion(session, oidcUser);
                SecurityContextHolder.clearContext();
                session.removeAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

                if ("LOGIN".equals(proposito)) {
                    try {
                        LoginResultado resultado = authApiClient.loginGoogle(oidcUser.getEmail());
                        iniciarSesionDesdeGoogle(session, resultado);
                        response.sendRedirect("Funcionario".equals(resultado.nombreRol()) ? "/controlDeAccesos" : "/inicio");
                        return;
                    } catch (HttpClientErrorException.NotFound e) {
                        // Sin cuenta todavía — sigue a completar registro más abajo.
                    } catch (Exception e) {
                        response.sendRedirect("/login?error=google");
                        return;
                    }
                }
                response.sendRedirect("/registro/completar-google");
                return;
            }

            if (session != null) {
                session.setAttribute("oauth2CorreoVerificado", oidcUser.getEmail());

                Authentication authOriginal =
                        (Authentication) session.getAttribute("authOriginalAntesDeOAuth2");
                if (authOriginal != null) {
                    SecurityContextHolder.getContext().setAuthentication(authOriginal);
                    session.setAttribute(
                        HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                        SecurityContextHolder.getContext()
                    );
                    session.removeAttribute("authOriginalAntesDeOAuth2");
                }
            }
            response.sendRedirect("/oauth2/callback/accion");
        };
    }

    private void guardarDatosGoogleEnSesion(HttpSession session, OidcUser oidcUser) {
        session.setAttribute("googleNombre", oidcUser.getGivenName() != null ? oidcUser.getGivenName() : oidcUser.getFullName());
        session.setAttribute("googleApellido", oidcUser.getFamilyName() != null ? oidcUser.getFamilyName() : "");
        session.setAttribute("googleCorreo", oidcUser.getEmail());
    }

    /** Mismo criterio que ApiAuthenticationProvider — solo que sin contraseña. */
    private void iniciarSesionDesdeGoogle(HttpSession session, LoginResultado resultado) {
        sesionUsuario.iniciarSesion(resultado);

        String authority = "ROLE_" + resultado.nombreRol().toUpperCase();
        Authentication nuevaAuth = new UsernamePasswordAuthenticationToken(
                resultado.numeroDocumento(), null, Collections.singletonList(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(nuevaAuth);
        session.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext()
        );

        session.setAttribute("rolUsuario", sesionUsuario.getNombreRolParaVista());
        session.setAttribute("usuarioLogueado", resultado.numeroDocumento());
    }

    private org.springframework.security.web.authentication.AuthenticationFailureHandler
            oauth2FailureHandler() {
        return (request, response, exception) -> {
            HttpSession session = request.getSession(false);
            String proposito = (session != null) ? (String) session.getAttribute("oauth2Proposito") : null;

            if ("LOGIN".equals(proposito)) {
                if (session != null) session.removeAttribute("oauth2Proposito");
                response.sendRedirect("/login?error=google");
                return;
            }
            if ("REGISTRO".equals(proposito)) {
                if (session != null) session.removeAttribute("oauth2Proposito");
                response.sendRedirect("/registro?error=google");
                return;
            }

            // Único caso restante: verificación de identidad al agregar un
            // método de pago (pagosYCartera.js) — las reservas ya no pasan
            // por OAuth2 en absoluto.
            response.sendRedirect("/pagosYCartera?oauth=error");
        };
    }
}
