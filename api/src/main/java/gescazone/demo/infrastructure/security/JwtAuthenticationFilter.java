package gescazone.demo.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro JWT — la Api es stateless, así que esta es la ÚNICA forma de
 * autenticar una request (salvo /api/auth/login, que la emite). El token
 * viaja como header "Authorization: Bearer <token>", nunca como cookie
 * (gescazone-web lo reenvía server-a-server; nunca queda expuesto al
 * navegador).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String PREFIJO_BEARER = "Bearer ";

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
            String token = extraerTokenDeHeader(request);

            if (token != null
                    && jwtTokenProvider.validateToken(token)
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                String numeroDocumento = jwtTokenProvider.getNumeroDocumentoFromToken(token);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(numeroDocumento);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.warn("No se pudo autenticar la petición vía JWT: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extraerTokenDeHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(PREFIJO_BEARER)) {
            return header.substring(PREFIJO_BEARER.length());
        }
        return null;
    }
}
