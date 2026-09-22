package gescazone.demo.domain.model;

import java.time.LocalDateTime;

/** Solicitud de registro pendiente de aprobación — ver SolicitudRegistroService. */
public class SolicitudRegistroModel {

    private String id;
    private String numeroDocumento;
    private String nombre;
    private String apellido;
    private String correo;
    private String contrasenaHash;
    private String nombreTipoDocumento;
    private String estado;
    private String revisadoPorIdUsuario;
    private String motivoRechazo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaRevision;

    public SolicitudRegistroModel() {}

    public SolicitudRegistroModel(String numeroDocumento, String nombre, String apellido, String correo,
                                  String contrasenaHash, String nombreTipoDocumento) {
        this.numeroDocumento = numeroDocumento;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.contrasenaHash = contrasenaHash;
        this.nombreTipoDocumento = nombreTipoDocumento;
        this.estado = "PENDIENTE";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public String getRevisadoPorIdUsuario() { return revisadoPorIdUsuario; }
    public void setRevisadoPorIdUsuario(String revisadoPorIdUsuario) { this.revisadoPorIdUsuario = revisadoPorIdUsuario; }

    public String getMotivoRechazo() { return motivoRechazo; }
    public void setMotivoRechazo(String motivoRechazo) { this.motivoRechazo = motivoRechazo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getFechaRevision() { return fechaRevision; }
    public void setFechaRevision(LocalDateTime fechaRevision) { this.fechaRevision = fechaRevision; }
}
