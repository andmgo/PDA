package gescazone.demo.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "solicitud_registro")
public class SolicitudRegistroEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero_documento", nullable = false)
    private String numeroDocumento;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false)
    private String correo;

    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(name = "nombre_tipo_documento", nullable = false)
    private String nombreTipoDocumento;

    @Column(nullable = false)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revisado_por_usuario_id")
    private UsuarioEntity revisadoPor;

    @Column(name = "motivo_rechazo")
    private String motivoRechazo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_revision")
    private LocalDateTime fechaRevision;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getContrasenaHash() { return contrasenaHash; }
    public void setContrasenaHash(String contrasenaHash) { this.contrasenaHash = contrasenaHash; }

    public String getNombreTipoDocumento() { return nombreTipoDocumento; }
    public void setNombreTipoDocumento(String nombreTipoDocumento) { this.nombreTipoDocumento = nombreTipoDocumento; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public UsuarioEntity getRevisadoPor() { return revisadoPor; }
    public void setRevisadoPor(UsuarioEntity revisadoPor) { this.revisadoPor = revisadoPor; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
