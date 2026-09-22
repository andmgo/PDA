package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.model.SolicitudReservaModel;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.SolicitudReservaRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cola de solicitudes de reserva excepcional (fuera del año actual) — un
 * administrador aprueba o rechaza dentro de la misma app, sin correo real
 * (decisión del usuario). Al aprobar, crea la reserva de verdad vía
 * ReservaSalonSocialService.guardarReservaAprobada().
 */
@Service
public class SolicitudReservaService {

    @Autowired
    private SolicitudReservaRepository solicitudRepository;

    @Autowired
    private SalonSocialRepository salonRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaSalonSocialService reservaSalonSocialService;

    public String crear(String idUsuario, String idSalon, LocalDateTime fechaSolicitada, String justificacion) {
        if (idUsuario == null)
            throw new IllegalArgumentException("El usuario es obligatorio");
        if (idSalon == null)
            throw new IllegalArgumentException("El salón es obligatorio");
        if (fechaSolicitada == null)
            throw new IllegalArgumentException("La fecha solicitada es obligatoria");
        if (fechaSolicitada.isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("No se puede solicitar una fecha pasada");
        if (reservaSalonSocialService.esFechaBloqueada(fechaSolicitada))
            throw new IllegalArgumentException(
                    "El salón no está disponible el 24 y 31 de diciembre ni el 1 de enero, ni siquiera por excepción.");
        if (fechaSolicitada.getYear() == LocalDate.now().getYear())
            throw new IllegalArgumentException(
                    "Esa fecha ya está dentro del año actual — puedes reservarla directamente, no hace falta una solicitud.");

        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el ID: " + idUsuario));
        salonRepository.findById(idSalon)
                .orElseThrow(() -> new NotFoundException("No existe un salón con el ID: " + idSalon));

        SolicitudReservaModel solicitud = new SolicitudReservaModel(idUsuario, idSalon, fechaSolicitada, justificacion);
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud enviada. Un administrador la revisará.";
    }

    public List<SolicitudReservaModel> listarPendientes() {
        return solicitudRepository.findByEstado("PENDIENTE");
    }

    public List<SolicitudReservaModel> listarTodas() {
        return solicitudRepository.findAll();
    }

    public String aprobar(String idSolicitud, String idAdminRevisor) {
        SolicitudReservaModel solicitud = obtenerPendiente(idSolicitud);

        ReservaSalonSocialModel reserva = new ReservaSalonSocialModel(
                solicitud.getIdUsuario(), solicitud.getIdSalon(), solicitud.getFechaSolicitada());
        reservaSalonSocialService.guardarReservaAprobada(reserva);

        solicitud.setEstado("APROBADA");
        solicitud.setRevisadoPorIdUsuario(idAdminRevisor);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud aprobada y reserva creada";
    }

    public String rechazar(String idSolicitud, String idAdminRevisor, String motivo) {
        SolicitudReservaModel solicitud = obtenerPendiente(idSolicitud);

        solicitud.setEstado("RECHAZADA");
        solicitud.setRevisadoPorIdUsuario(idAdminRevisor);
        solicitud.setMotivoRechazo(motivo);
        solicitud.setFechaRevision(LocalDateTime.now());
        solicitudRepository.save(solicitud);
        return "Solicitud rechazada";
    }

    private SolicitudReservaModel obtenerPendiente(String idSolicitud) {
        if (idSolicitud == null)
            throw new IllegalArgumentException("El ID de la solicitud es obligatorio");
        SolicitudReservaModel solicitud = solicitudRepository.findById(idSolicitud)
                .orElseThrow(() -> new NotFoundException("No existe la solicitud con ID: " + idSolicitud));
        if (!"PENDIENTE".equals(solicitud.getEstado()))
            throw new IllegalArgumentException("La solicitud ya fue revisada (" + solicitud.getEstado() + ")");
        return solicitud;
    }
}
