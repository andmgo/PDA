package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.SolicitudRegistroModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.SolicitudRegistroRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cola de solicitudes de autoregistro — un administrador aprueba (asignando
 * el rol) o rechaza. Al aprobar, crea el Usuario real vía
 * UsuarioService.crearAprobado(), reutilizando la contraseña ya hasheada
 * desde el momento de la solicitud (nunca se re-hashea).
 */
@Service
public class SolicitudRegistroService {

    @Autowired
    private SolicitudRegistroRepository solicitudRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    public String crear(String numeroDocumento, String nombre, String apellido, String correo,
                         String contrasena, String nombreTipoDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (apellido == null || apellido.trim().isEmpty())
            throw new IllegalArgumentException("El apellido es obligatorio");
        if (correo == null || correo.trim().isEmpty())
            throw new IllegalArgumentException("El correo es obligatorio");
        if (contrasena == null || contrasena.trim().isEmpty())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        if (contrasena.length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty())
            throw new IllegalArgumentException("Debe seleccionar un tipo de documento");
        if (usuarioRepository.existsByNumeroDocumento(numeroDocumento.trim()))
            throw new IllegalArgumentException("Ya existe un usuario con el documento: " + numeroDocumento);
        if (usuarioRepository.existsByCorreo(correo.trim()))
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + correo);

        SolicitudRegistroModel solicitud = new SolicitudRegistroModel(
                numeroDocumento.trim(), nombre.trim(), apellido.trim(), correo.trim(),
                passwordEncoder.encode(contrasena), nombreTipoDocumento.trim());
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud enviada. Un administrador la revisará.";
    }

    public List<SolicitudRegistroModel> listarPendientes() {
        return solicitudRepository.findByEstado("PENDIENTE");
    }

    public List<SolicitudRegistroModel> listarTodas() {
        return solicitudRepository.findAll();
    }

    public String aprobar(String idSolicitud, String idAdminRevisor, String nombreRol) {
        if (nombreRol == null || nombreRol.trim().isEmpty())
            throw new IllegalArgumentException("Debe seleccionar un rol");
        SolicitudRegistroModel solicitud = obtenerPendiente(idSolicitud);

        UsuarioModel usuario = new UsuarioModel(
                solicitud.getNumeroDocumento(), solicitud.getNombre(), solicitud.getApellido(),
                solicitud.getCorreo(), solicitud.getContrasenaHash(),
                new RolModel(nombreRol.trim()), new TipoDocumentoModel(solicitud.getNombreTipoDocumento()));
        usuarioService.crearAprobado(usuario);

        solicitud.setEstado("APROBADA");
        solicitud.setRevisadoPorIdUsuario(idAdminRevisor);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud aprobada y usuario creado";
    }

    public String rechazar(String idSolicitud, String idAdminRevisor, String motivo) {
        SolicitudRegistroModel solicitud = obtenerPendiente(idSolicitud);

        solicitud.setEstado("RECHAZADA");
        solicitud.setRevisadoPorIdUsuario(idAdminRevisor);
        solicitud.setMotivoRechazo(motivo);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud rechazada";
    }

    private SolicitudRegistroModel obtenerPendiente(String idSolicitud) {
        if (idSolicitud == null)
            throw new IllegalArgumentException("El ID de la solicitud es obligatorio");
        SolicitudRegistroModel solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFoundException("No existe la solicitud con ID: " + idSolicitud));
        if (!"PENDIENTE".equals(solicitud.getEstado()))
            throw new IllegalArgumentException("La solicitud ya fue revisada (" + solicitud.getEstado() + ")");
        return solicitud;
    }
}
