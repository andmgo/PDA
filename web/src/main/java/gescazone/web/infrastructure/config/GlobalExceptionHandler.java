package gescazone.web.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 403 - Acceso denegado
    // IMPORTANTE: debe relanzarse para que Spring Security lo maneje correctamente
    @ExceptionHandler(AccessDeniedException.class)
    public String handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request,
            Model model) {
        model.addAttribute("codigo", 403);
        model.addAttribute("mensaje", "No tienes permisos para acceder a esta sección.");
        model.addAttribute("ruta", request.getRequestURI());
        return "error";
    }

    // 404 - Página no encontrada
    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(
            HttpServletRequest request,
            Model model) {
        model.addAttribute("codigo", 404);
        model.addAttribute("mensaje", "La página que buscas no existe.");
        model.addAttribute("ruta", request.getRequestURI());
        return "error";
    }

    // 500 - Error general
    // NO captura AccessDeniedException gracias al orden de handlers
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGeneral(
            Exception ex,
            HttpServletRequest request,
            Model model) {

        // Si por alguna razón llega una AccessDeniedException aquí, la relanzamos
        if (ex instanceof AccessDeniedException) {
            throw (AccessDeniedException) ex;
        }

        model.addAttribute("codigo", 500);
        model.addAttribute("mensaje", "Ocurrió un error inesperado. Por favor intenta de nuevo.");
        model.addAttribute("ruta", request.getRequestURI());
        return "error";
    }
}
