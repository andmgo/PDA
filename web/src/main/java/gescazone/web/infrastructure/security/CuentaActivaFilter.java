package gescazone.web.infrastructure.security;

import gescazone.web.infrastructure.client.AuthApiClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Saca de inmediato a quien fue desactivado con la sesión ya abierta. Antes
 * solo se bloqueaba el login: una sesión iniciada seguía funcionando hasta
 * cerrarse sola. En cada navegación (no en recursos estáticos) se le pregunta
 * al API si la cuenta sigue activa, como máximo una vez cada INTERVALO_MS
 * por sesión para no agregar una llamada extra a cada clic.
 */
@Component
public class CuentaActivaFilter extends OncePerRequestFilter {

    private static final long INTERVALO_MS = 30_000;
    private static final String ATRIBUTO_ULTIMA_VERIFICACION = "cuentaActivaVerificadaEn";

    @Autowired private SesionUsuario sesionUsuario;
    @Autowired private AuthApiClient authApiClient;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String ruta = request.getRequestURI();
        return ruta.startsWith("/css/") || ruta.startsWith("/js/") || ruta.startsWith("/img/")
                || ruta.startsWith("/fonts/") || ruta.equals("/favicon.ico")
                || ruta.equals("/login") || ruta.equals("/logout") || ruta.equals("/error");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null && sesionUsuario.estaAutenticado()) {
            Long ultima = (Long) session.getAttribute(ATRIBUTO_ULTIMA_VERIFICACION);
            long ahora = System.currentTimeMillis();
            if (ultima == null || ahora - ultima > INTERVALO_MS) {
                if (!authApiClient.cuentaActiva(sesionUsuario.getJwt())) {
                    SecurityContextHolder.clearContext();
                    session.invalidate();
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Cuenta desactivada");
                    } else {
                        response.sendRedirect("/login?error=disabled");
                    }
                    return;
                }
                session.setAttribute(ATRIBUTO_ULTIMA_VERIFICACION, ahora);
            }
        }
        chain.doFilter(request, response);
    }
}
