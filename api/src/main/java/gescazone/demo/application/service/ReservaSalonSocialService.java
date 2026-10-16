package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.NivelPermiso;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.ReservaSalonSocialRepository;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservaSalonSocialService {

    @Autowired
    private ReservaSalonSocialRepository reservaRepository;
    @Autowired
    private SalonSocialRepository salonRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PermisoService permisoService;

    @Autowired
    private CarteraService carteraService;

    /** Reserva como la ve su dueño en "Mis reservas" (app). */
    public record MiReserva(String id, String idSalon, String numeroSalon, LocalDateTime fechaYHora, boolean pasada) {}

    /** Reservas de la persona logueada: próximas primero, luego las pasadas (máximo 30). */
    public List<MiReserva> misReservas(String numeroDocumento) {
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
        LocalDateTime ahora = LocalDateTime.now();
        Comparator<ReservaSalonSocialModel> orden = Comparator
                .comparing((ReservaSalonSocialModel r) -> r.getFechaYHoraReserva().isBefore(ahora))
                .thenComparing((a, b) -> a.getFechaYHoraReserva().isBefore(ahora)
                        ? b.getFechaYHoraReserva().compareTo(a.getFechaYHoraReserva())
                        : a.getFechaYHoraReserva().compareTo(b.getFechaYHoraReserva()));
        return reservaRepository.findByIdUsuario(usuario.getId()).stream()
                .sorted(orden).limit(30)
                .map(r -> new MiReserva(r.getId(), r.getIdSalon(), numeroSalon(r.getIdSalon()),
                        r.getFechaYHoraReserva(), r.getFechaYHoraReserva().isBefore(ahora)))
                .toList();
    }

    /**
     * Días ya reservados de un salón (desde hoy), para deshabilitarlos en el
     * calendario. Sin datos de quién reservó: el propietario no necesita
     * conocer las reservas de sus vecinos.
     */
    public List<LocalDate> diasOcupados(String idSalon) {
        if (idSalon == null || idSalon.isBlank())
            throw new ValidationException("Indica el salón");
        return reservaRepository.findReservasFuturasPorSalon(idSalon, LocalDate.now().atStartOfDay()).stream()
                .map(r -> r.getFechaYHoraReserva().toLocalDate())
                .distinct().sorted().toList();
    }

    /**
     * Antes cualquiera con el permiso RESERVAS podía borrar o cambiar la
     * reserva de otro (bastaba conocer su ID). Ahora solo su dueño o quien
     * administra (GESTION_DATOS editar); el dueño no puede tocar una reserva
     * que ya pasó.
     */
    private void exigirDuenoOAdministracion(ReservaSalonSocialModel reserva, String documentoSolicitante) {
        UsuarioModel solicitante = usuarioRepository.findByNumeroDocumento(documentoSolicitante)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
        if (esAdministracion(solicitante)) return;
        if (!solicitante.getId().equals(reserva.getIdUsuario()))
            throw new ValidationException("Solo puedes modificar o cancelar tus propias reservas");
        if (reserva.getFechaYHoraReserva().isBefore(LocalDateTime.now()))
            throw new ValidationException("Esta reserva ya pasó");
    }

    private boolean esAdministracion(UsuarioModel usuario) {
        return usuario.getRol() != null
                && permisoService.tienePermiso(usuario.getRol().getNombreRol(), "GESTION_DATOS", NivelPermiso.EDITAR);
    }

    /** Cancelar / eliminar con la regla de dueño o administración. */
    @Transactional
    public String eliminarReserva(String id, String documentoSolicitante) {
        if (id == null)
            throw new IllegalArgumentException("El ID no puede ser nulo");
        ReservaSalonSocialModel reserva = reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe una reserva con el ID: " + id));
        exigirDuenoOAdministracion(reserva, documentoSolicitante);
        carteraService.anularCobroReserva(id); // falla si ya está pagada
        reservaRepository.deleteById(id);
        return "Reserva cancelada";
    }

    /** Actualizar con la regla de dueño o administración; el dueño no puede pasarla a otra persona. */
    public String actualizarReserva(ReservaSalonSocialModel cambios, String documentoSolicitante) {
        ReservaSalonSocialModel existente = reservaRepository.findById(cambios.getId())
                .orElseThrow(() -> new NotFoundException("La reserva a actualizar no existe"));
        exigirDuenoOAdministracion(existente, documentoSolicitante);
        UsuarioModel solicitante = usuarioRepository.findByNumeroDocumento(documentoSolicitante).orElseThrow();
        if (!esAdministracion(solicitante)) cambios.setIdUsuario(existente.getIdUsuario());
        return guardarReserva(cambios);
    }

    public List<ReservaSalonSocialModel> listarReservas() {
        return reservaRepository.findAll();
    }

    public ReservaSalonSocialModel buscarPorId(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID no puede ser nulo");
        return reservaRepository.findById(id).orElse(null);
    }

    /**
     * Reserva a nombre de quien inició sesión: el usuario sale del documento
     * del JWT (nunca de un ID enviado por el cliente).
     */
    @Transactional
    public String crearPropia(String numeroDocumentoAutenticado, String idSalon, LocalDateTime fechaYHora) {
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumentoAutenticado)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
        ReservaSalonSocialModel reserva = new ReservaSalonSocialModel(usuario.getId(), idSalon, fechaYHora);
        String mensaje = guardarReserva(reserva);
        // Cobro de la reserva a su apartamento (si hay valor configurado)
        carteraService.cobrarReserva(reserva.getId(), usuario.getId(), numeroSalon(idSalon), fechaYHora.toLocalDate());
        return mensaje;
    }

    /** Documento legible del usuario de una reserva (para listados); el ID si ya no existe. */
    public String numeroDocumentoUsuario(String idUsuario) {
        return usuarioRepository.findById(idUsuario).map(UsuarioModel::getNumeroDocumento).orElse(idUsuario);
    }

    /** Número legible del salón de una reserva (para listados); el ID si ya no existe. */
    public String numeroSalon(String idSalon) {
        return salonRepository.findById(idSalon).map(s -> s.getNumero()).orElse(idSalon);
    }

    public String guardarReserva(ReservaSalonSocialModel reserva) {
        return guardarReservaInterna(reserva, false);
    }

    /**
     * Usado por SolicitudReservaService.aprobar() para crear la reserva real
     * de una solicitud excepcional ya aprobada — salta la restricción de año
     * actual (ese es justo el punto de la excepción). Las fechas bloqueadas
     * (24/31 dic, 1 ene) siguen aplicando siempre, sin excepción posible.
     */
    public String guardarReservaAprobada(ReservaSalonSocialModel reserva) {
        return guardarReservaInterna(reserva, true);
    }

    private String guardarReservaInterna(ReservaSalonSocialModel reserva, boolean omitirValidacionAnio) {
        if (reserva == null)
            throw new IllegalArgumentException("La reserva no puede ser nula");
        if (reserva.getFechaYHoraReserva() == null)
            throw new IllegalArgumentException("La fecha y hora de la reserva son obligatorias");
        if (reserva.getIdSalon() == null)
            throw new IllegalArgumentException("El salón es obligatorio");
        if (reserva.getIdUsuario() == null)
            throw new IllegalArgumentException("El usuario es obligatorio");
        if (reserva.getFechaYHoraReserva().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("No se puede reservar en una fecha pasada");
        if (esFechaBloqueada(reserva.getFechaYHoraReserva()))
            throw new IllegalArgumentException(
                    "El salón no está disponible el 24 y 31 de diciembre ni el 1 de enero.");
        if (!omitirValidacionAnio && reserva.getFechaYHoraReserva().getYear() != LocalDate.now().getYear())
            throw new IllegalArgumentException(
                    "Solo se permiten reservas dentro del año actual (" + LocalDate.now().getYear() +
                    "). Para reservar en otro año, envía una solicitud de excepción para que un administrador la apruebe.");

        RequiereActivo.salon(salonRepository.findById(reserva.getIdSalon())
                .orElseThrow(() -> new NotFoundException("El salón especificado no existe")));
        RequiereActivo.usuario(usuarioRepository.findById(reserva.getIdUsuario())
                .orElseThrow(() -> new NotFoundException("El usuario especificado no existe")));

        boolean esNuevaReserva = reserva.getId() == null;
        boolean debeValidarDisponibilidad = esNuevaReserva;

        if (!esNuevaReserva) {
            ReservaSalonSocialModel reservaExistente = reservaRepository.findById(reserva.getId())
                    .orElseThrow(() -> new NotFoundException("La reserva a actualizar no existe"));

            boolean cambioSalon = !reservaExistente.getIdSalon().equals(reserva.getIdSalon());
            boolean cambioFecha = !reservaExistente.getFechaYHoraReserva().toLocalDate()
                    .equals(reserva.getFechaYHoraReserva().toLocalDate());

            debeValidarDisponibilidad = cambioSalon || cambioFecha;
        }

        if (debeValidarDisponibilidad) {
            if (reservaRepository.existsReservaEnMismoDia(reserva.getIdSalon(), reserva.getFechaYHoraReserva())) {
                throw new IllegalArgumentException(
                        "El salón ya está reservado para el día " +
                        reserva.getFechaYHoraReserva().toLocalDate() +
                        ". Solo se permite una reserva por salón por día.");
            }
        }

        ReservaSalonSocialModel guardada = reservaRepository.save(reserva);
        if (guardada != null && reserva.getId() == null) reserva.setId(guardada.getId());
        return esNuevaReserva ? "Reserva creada exitosamente" : "Reserva actualizada exitosamente";
    }

    public String guardarReserva(ReservaSalonSocialModel reserva, String idUsuario, String idSalon) {
        if (reserva == null)
            throw new IllegalArgumentException("La reserva no puede ser nula");
        if (idUsuario == null)
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        if (idSalon == null)
            throw new IllegalArgumentException("El ID del salón es obligatorio");

        RequiereActivo.usuario(usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el ID: " + idUsuario)));
        RequiereActivo.salon(salonRepository.findById(idSalon)
                .orElseThrow(() -> new NotFoundException("No existe un salón con el ID: " + idSalon)));

        reserva.setIdUsuario(idUsuario);
        reserva.setIdSalon(idSalon);
        return guardarReserva(reserva);
    }

    public List<ReservaSalonSocialModel> buscarPorIdSalon(String idSalon) {
        if (idSalon == null)
            throw new IllegalArgumentException("El ID del salón no puede ser nulo");
        return reservaRepository.findByIdSalon(idSalon);
    }

    public List<ReservaSalonSocialModel> buscarPorNumeroSalon(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número de salón no puede estar vacío");
        return reservaRepository.findBySalonNumero(numero);
    }

    public List<ReservaSalonSocialModel> buscarPorIdUsuario(String idUsuario) {
        if (idUsuario == null)
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        return reservaRepository.findByIdUsuario(idUsuario);
    }

    public List<ReservaSalonSocialModel> buscarPorIdHabitante(String idHabitante) {
        if (idHabitante == null)
            throw new IllegalArgumentException("El ID del habitante no puede ser nulo");
        return reservaRepository.findByIdUsuario(idHabitante);
    }

    public List<ReservaSalonSocialModel> buscarPorDocumentoUsuario(String documento) {
        if (documento == null || documento.trim().isEmpty())
            throw new IllegalArgumentException("El documento no puede estar vacío");
        return reservaRepository.findByUsuarioNumeroDocumento(documento);
    }

    public List<ReservaSalonSocialModel> buscarPorDocumentoHabitante(String documento) {
        return buscarPorDocumentoUsuario(documento);
    }

    public List<ReservaSalonSocialModel> buscarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null)
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        if (inicio.isAfter(fin))
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        return reservaRepository.findByFechaYHoraReservaBetween(inicio, fin);
    }

    public List<ReservaSalonSocialModel> buscarReservasFuturasSalon(String idSalon) {
        if (idSalon == null)
            throw new IllegalArgumentException("El ID del salón no puede ser nulo");
        return reservaRepository.findReservasFuturasPorSalon(idSalon, LocalDateTime.now());
    }

    public List<ReservaSalonSocialModel> buscarReservasFuturasUsuario(String idUsuario) {
        if (idUsuario == null)
            throw new IllegalArgumentException("El ID del usuario no puede ser nulo");
        return reservaRepository.findReservasFuturasPorUsuario(idUsuario, LocalDateTime.now());
    }

    /**
     * 24 y 31 de diciembre, 1 de enero — bloqueados todos los años, sin
     * excepción. Público porque SolicitudReservaService también lo usa para
     * rechazar solicitudes de fechas que jamás se podrían aprobar.
     */
    public boolean esFechaBloqueada(LocalDateTime fecha) {
        int mes = fecha.getMonthValue();
        int dia = fecha.getDayOfMonth();
        return (mes == 12 && (dia == 24 || dia == 31)) || (mes == 1 && dia == 1);
    }

    public boolean verificarDisponibilidad(String idSalon, LocalDateTime fechaHora) {
        if (idSalon == null)
            throw new IllegalArgumentException("El ID del salón no puede ser nulo");
        if (fechaHora == null)
            throw new IllegalArgumentException("La fecha y hora no pueden ser nulas");
        salonRepository.findById(idSalon)
                .orElseThrow(() -> new NotFoundException("El salón especificado no existe"));
        return !reservaRepository.existsByIdSalonAndFechaYHoraReserva(idSalon, fechaHora);
    }
}