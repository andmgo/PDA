package gescazone.demo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // 1. Extraer el token JWT de la Cookie
            String token = extraerTokenDeCookie(request);

            // 2. Si hay token y es válido, autenticar al usuario
            if (token != null && jwtTokenProvider.validateToken(token)) {

                // 3. Extraer el número de documento del token
                String numeroDocumento = jwtTokenProvider.getNumeroDocumentoFromToken(token);  
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(numeroDocumento);

                // 5. Crear el objeto de autenticación de Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null, // sin contraseña, ya está autenticado
                                userDetails.getAuthorities()
                        );

                // 6. Agregar detalles del request (IP, session, etc.)
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Registrar la autenticación en el contexto de Spring Security
                //    A partir de aquí Spring sabe quién es el usuario
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } catch (Exception e) {
            System.out.println("No se pudo autenticar el usuario: " + e.getMessage());
            // No lanzamos excepción → dejamos que Spring Security rechace más adelante
        }

        // 8. Continuar con el siguiente filtro de la cadena
        filterChain.doFilter(request, response);
    }

    // ─────────────────────────────────────────
    // Busca la cookie "JWT-TOKEN" en el request
    // Retorna null si no existe
    // ─────────────────────────────────────────
    private String extraerTokenDeCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if ("JWT-TOKEN".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    // ─────────────────────────────────────────
    // Rutas que el filtro NO debe interceptar
    // (login, register, recursos estáticos)
    // ─────────────────────────────────────────
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/auth/")     // login, register
            || path.startsWith("/css/")      // estáticos
            || path.startsWith("/js/")
            || path.startsWith("/fonts/")
            || path.startsWith("/images/")
            || path.equals("/");             // página principal pública
    }
}