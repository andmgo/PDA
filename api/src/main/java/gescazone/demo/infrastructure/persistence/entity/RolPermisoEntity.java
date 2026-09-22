package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol_permiso")
@IdClass(RolPermisoId.class)
public class RolPermisoEntity {

    // EAGER: catálogos minúsculos, dereferenciados fuera de transacción por
    // PermisoService al armar su caché — ver ApartamentoEntity.
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id")
    private RolEntity rol;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "permiso_id")
    private PermisoEntity permiso;

    @Column(name = "puede_ver", nullable = false)
    private boolean puedeVer;

    @Column(name = "puede_editar", nullable = false)
    private boolean puedeEditar;

    public RolEntity getRol() { return rol; }
    public void setRol(RolEntity rol) { this.rol = rol; }

    public PermisoEntity getPermiso() { return permiso; }
    public void setPermiso(PermisoEntity permiso) { this.permiso = permiso; }

    public boolean isPuedeVer() { return puedeVer; }
    public void setPuedeVer(boolean puedeVer) { this.puedeVer = puedeVer; }

    public boolean isPuedeEditar() { return puedeEditar; }
    public void setPuedeEditar(boolean puedeEditar) { this.puedeEditar = puedeEditar; }
}
