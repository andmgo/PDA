package gescazone.web.dto;

/** Una celda de la matriz rol x permiso, tal como la devuelve gescazone-api en el login. */
public record PermisoCelda(String codigo, boolean puedeVer, boolean puedeEditar) {}
