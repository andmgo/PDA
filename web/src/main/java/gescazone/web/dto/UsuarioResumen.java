package gescazone.web.dto;

/** Forma plana en la que gescazone-api devuelve un usuario (GET /api/usuarios/{doc}). */
public record UsuarioResumen(
        String id,
        String numeroDocumento,
        String nombre,
        String apellido,
        String correo,
        String nombreRol,
        String nombreTipoDocumento
) {}
