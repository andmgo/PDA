package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.repository.ReservaSalonSocialRepository;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservaSalonSocialService {

    @Autowired
    private ReservaSalonSocialRepository reservaRepository;
    @Autowired
    private SalonSocialRepository salonRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    public List<ReservaSalonSocialModel> listarReservas() {
        return reservaRepository.findAll();
    }

    public ReservaSalonSocialModel buscarPorId(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID no puede ser nulo");
        return reservaRepository.findById(id).orElse(null);
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

        salonRepository.findById(reserva.getIdSalon())
                .orElseThrow(() -> new NotFoundException("El salón especificado no existe"));
        usuarioRepository.findById(reserva.getIdUsuario())
                .orElseThrow(() -> new NotFoundException("El usuario especificado no existe"));

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

        reservaRepository.save(reserva);
        return esNuevaReserva ? "Reserva creada exitosamente" : "Reserva actualizada exitosamente";
    }

    public String guardarReserva(ReservaSalonSocialModel reserva, String idUsuario, String idSalon) {
        if (reserva == null)
            throw new IllegalArgumentException("La reserva no puede ser nula");
        if (idUsuario == null)
            throw new IllegalArgumentException("El ID del usuario es obligatorio");
        if (idSalon == null)
            throw new IllegalArgumentException("El ID del salón es obligatorio");

        usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el ID: " + idUsuario));
        salonRepository.findById(idSalon)
                .orElseThrow(() -> new NotFoundException("No existe un salón con el ID: " + idSalon));

        reserva.setIdUsuario(idUsuario);
        reserva.setIdSalon(idSalon);
        return guardarReserva(reserva);
    }

    public String eliminarReserva(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID no puede ser nulo");
        reservaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe una reserva con el ID: " + id));
        reservaRepository.deleteById(id);
        return "Reserva eliminada exitosamente";
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