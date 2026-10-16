package gescazone.demo.infrastructure.config;

import gescazone.demo.application.service.PermisoService;
import gescazone.demo.domain.model.NivelPermiso;
import gescazone.demo.infrastructure.security.CustomUserDetailsService;
import gescazone.demo.infrastructure.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Api stateless: sin formLogin, sin oauth2Login, sin sesión HTTP. Toda
 * autenticación entra por JwtAuthenticationFilter (header Authorization:
 * Bearer) salvo /api/auth/login, que la emite. El login por formulario y el
 * flujo de Google OAuth2 ahora viven enteramente en gescazone-web.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = false)
public class SecurityConfig {

    @Autowired private CustomUserDetailsService customUserDetailsService;
    @Autowired private JwtAuthenticationFilter  jwtFilter;
    @Autowired private PermisoService permisoService;

    @Value("${app.cors.allowed-origins}")
    private String[] corsAllowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsAllowedOrigins));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /** Ver Bloque B2: resuelve el permiso contra PermisoService en cada request, nunca hardcodeado. */
    private AuthorizationManager<RequestAuthorizationContext> permiso(String codigo, NivelPermiso nivel) {
        return (Supplier<Authentication> authentication, RequestAuthorizationContext context) -> {
            Authentication auth = authentication.get();
            if (auth == null || !auth.isAuthenticated()) {
                return new AuthorizationDecision(false);
            }
            String nombreRol = auth.getAuthorities().stream()
                    .findFirst()
                    .map(a -> a.getAuthority().replaceFirst("^ROLE_", ""))
                    .orElse(null);
            return new AuthorizationDecision(permisoService.tienePermiso(nombreRol, codigo, nivel));
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            .authorizeHttpRequests(auth -> auth

                .requestMatchers("/api/auth/login", "/api/auth/login-google").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/solicitudes-registro").permitAll()

                .requestMatchers(
                    "/api/apartamentos/**",
                    "/api/parqueaderos/**",
                    "/api/residentes/**"
                ).access(permiso("APARTAMENTOS_PARQUEADEROS_RESIDENTES", NivelPermiso.VER))

                .requestMatchers(
                    "/api/salones/crear",
                    "/api/salones/crear-varios",
                    "/api/salones/actualizar/**",
                    "/api/salones/desactivar/**",
                    "/api/salones/activar/**",
                    "/api/salones/cambiar-estado"
                ).access(permiso("SALONES", NivelPermiso.EDITAR))

                .requestMatchers(
                    "/api/usuarios/crear",
                    "/api/usuarios/actualizar/**",
                    "/api/usuarios/desactivar/**",
                    "/api/usuarios/activar/**",
                    "/api/usuarios/todos",
                    "/api/usuarios/*/resetear-contrasena"
                ).access(permiso("USUARIOS", NivelPermiso.EDITAR))

                // Cambiar la propia contraseña no es "administrar usuarios" — lo
                // puede hacer cualquiera autenticado, tenga o no el permiso
                // USUARIOS-editar (que hoy solo tiene ADMINISTRADOR). El propio
                // controller ya limita la acción a la cuenta del JWT y exige la
                // contraseña actual.
                .requestMatchers("/api/usuarios/cambiar-contrasena").authenticated()

                .requestMatchers("/api/roles-permisos/**")
                    .access(permiso("GESTION_DATOS", NivelPermiso.EDITAR))

                .requestMatchers("/api/salones/**")
                    .access(permiso("SALONES", NivelPermiso.VER))
                .requestMatchers("/api/usuarios/**")
                    .access(permiso("USUARIOS", NivelPermiso.VER))

                // Pagos y cartera (Bloque J). El evento de Wompi es público pero
                // CarteraService solo lo acepta con firma válida.
                .requestMatchers(HttpMethod.POST, "/api/pagos/wompi/eventos").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/pagos/cargos/*/pagar")
                    .access(permiso("PAGOS_Y_CARTERA", NivelPermiso.EDITAR))
                .requestMatchers("/api/pagos/**")
                    .access(permiso("PAGOS_Y_CARTERA", NivelPermiso.VER))
                .requestMatchers(HttpMethod.GET, "/api/cartera/**")
                    .access(permiso("ADMIN_CARTERA", NivelPermiso.VER))
                .requestMatchers("/api/cartera/**")
                    .access(permiso("ADMIN_CARTERA", NivelPermiso.EDITAR))

                .requestMatchers("/api/accesos/**")
                    .access(permiso("CONTROL_ACCESOS", NivelPermiso.VER))

                .requestMatchers("/api/reservas/**")
                    .access(permiso("RESERVAS", NivelPermiso.EDITAR))

                .requestMatchers(HttpMethod.POST, "/api/solicitudes-reserva")
                    .access(permiso("RESERVAS", NivelPermiso.EDITAR))
                .requestMatchers("/api/solicitudes-reserva/**")
                    .access(permiso("GESTION_DATOS", NivelPermiso.EDITAR))

                .requestMatchers("/api/solicitudes-registro/**")
                    .access(permiso("USUARIOS", NivelPermiso.EDITAR))

                // Paquetes. Orden: específico → general. /mios es el propietario
                // (Fase 7); registrar y entregar exigen EDITAR (antes bastaba VER).
                .requestMatchers(HttpMethod.GET, "/api/paquetes/mios")
                    .access(permiso("MIS_PAQUETES", NivelPermiso.VER))
                .requestMatchers(HttpMethod.GET, "/api/paquetes/**")
                    .access(permiso("PAQUETES", NivelPermiso.VER))
                .requestMatchers("/api/paquetes/**")
                    .access(permiso("PAQUETES", NivelPermiso.EDITAR))

                // Propietarios y vehículos (Fase 2). Orden: específico → general.
                // /mia siempre opera sobre la cuenta del JWT; la búsqueda por
                // placa es para portería; el resto es administración.
                .requestMatchers(HttpMethod.GET, "/api/propiedad/mia").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/propiedad/vehiculos/placa/**")
                    .access(permiso("CONTROL_ACCESOS", NivelPermiso.VER))
                .requestMatchers(HttpMethod.GET, "/api/propiedad/**")
                    .access(permiso("APARTAMENTOS_PARQUEADEROS_RESIDENTES", NivelPermiso.VER))
                .requestMatchers("/api/propiedad/**")
                    .access(permiso("APARTAMENTOS_PARQUEADEROS_RESIDENTES", NivelPermiso.EDITAR))

                // Visitas autorizadas por el propietario (Fase 6). Portería lee los
                // "esperados" por /api/accesos/visitas/esperadas (CONTROL_ACCESOS).
                .requestMatchers(HttpMethod.GET, "/api/visitas-autorizadas/**")
                    .access(permiso("VISITAS", NivelPermiso.VER))
                .requestMatchers("/api/visitas-autorizadas/**")
                    .access(permiso("VISITAS", NivelPermiso.EDITAR))

                .requestMatchers("/api/catalogos/**").authenticated()

                .anyRequest().authenticated()
            );

        return http.build();
    }
}
