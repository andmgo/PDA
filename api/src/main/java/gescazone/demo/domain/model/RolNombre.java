package gescazone.demo.domain.model;

/**
 * Catálogo de roles de negocio del sistema. Único punto donde se declaran
 * los nombres de rol usados en autorización — antes estaban repetidos como
 * strings literales en SecurityConfig y CustomUserDetailsService.
 */
public enum RolNombre {
    ADMINISTRADOR,
    PROPIETARIO,
    FUNCIONARIO;

    /** Nombre de authority que espera Spring Security (prefijo ROLE_). */
    public String toAuthority() {
        return "ROLE_" + name();
    }

    /** Convierte el nombre de rol persistido (texto libre en RolModel) a su authority. */
    public static String toAuthority(String nombreRolPersistido) {
        return "ROLE_" + nombreRolPersistido.toUpperCase();
    }
}
