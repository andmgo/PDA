package gescazone.web.dto;

import java.util.List;

/** Respuesta de POST /api/auth/login en gescazone-api. */
public record LoginResultado(
        String token,
        String numeroDocumento,
        String nombre,
        String apellido,
        String correo,
        String nombreRol,
        List<PermisoCelda> permisos
) {}
