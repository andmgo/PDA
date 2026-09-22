package gescazone.demo.infrastructure.persistence.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Clave compuesta de RolPermisoEntity — nombres de campo deben calzar con @IdClass. */
public class RolPermisoId implements Serializable {

    private UUID rol;
    private UUID permiso;

    public RolPermisoId() {}

    public RolPermisoId(UUID rol, UUID permiso) {
        this.rol = rol;
        this.permiso = permiso;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RolPermisoId that)) return false;
        return Objects.equals(rol, that.rol) && Objects.equals(permiso, that.permiso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rol, permiso);
    }
}
