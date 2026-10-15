package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.model.VisitaAutorizadaModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.domain.repository.VisitaAutorizadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Visitas autorizadas por el propietario (Fase 6): en vez de que portería
 * llame al propietario cada vez que llega alguien, el propietario registra
 * de antemano quién viene y en qué fechas.
 */
@Service
public class VisitaAutorizadaService {

    public enum Estado { PROGRAMADA, VIGENTE, VENCIDA, CANCELADA }

    /** Autorización como la ven el propietario y portería. */
    public record Autorizacion(String id, String numeroApartamento, String nombreVisitante, String apellidoVisitante,
                               String numeroDocumento, String placa, LocalDate fechaDesde, LocalDate fechaHasta,
                               String nota, Estado estado, long ingresos, String autorizadaPor) {}

    public record Nueva(String idApartamento, String nombreVisitante, String apellidoVisitante, String numeroDocumento,
                        String placa, LocalDate fechaDesde, LocalDate fechaHasta, String nota) {}

    @Autowired private VisitaAutorizadaRepository visitaAutorizadaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ApartamentoRepository apartamentoRepository;
    @Autowired private UsuarioApartamentoRepository usuarioApartamentoRepository;

    /** Separado para poder fijar "hoy" en las pruebas. */
    LocalDate hoy() {
        return LocalDate.now();
    }

    // ── Propietario ────────────────────────────────────────────────────────

    public Autorizacion crear(String documentoUsuario, Nueva datos) {
        UsuarioModel usuario = usuario(documentoUsuario);
        if (datos == null || datos.idApartamento() == null || datos.idApartamento().isBlank())
            throw new ValidationException("Elige el apartamento al que viene la visita");
        ApartamentoModel apartamento = RequiereActivo.apartamento(apartamentoRepository.findById(datos.idApartamento())
                .orElseThrow(() -> new NotFoundException("El apartamento no existe")));
        if (!usuarioApartamentoRepository.existe(usuario.getId(), apartamento.getId()))
            throw new ValidationException("Solo puedes autorizar visitas a tus apartamentos");

        VisitaAutorizadaModel visita = new VisitaAutorizadaModel();
        visita.setIdApartamento(apartamento.getId());
        visita.setIdAutorizadaPor(usuario.getId());
        visita.setNombreVisitante(datos.nombreVisitante());
        visita.setApellidoVisitante(datos.apellidoVisitante());
        visita.setNumeroDocumento(ResidenteModel.normalizarDocumento(datos.numeroDocumento()));
        String placa = VehiculoModel.normalizarPlaca(datos.placa());
        visita.setPlaca(placa == null || placa.isEmpty() ? null : placa);
        visita.setFechaDesde(datos.fechaDesde());
        visita.setFechaHasta(datos.fechaHasta() != null ? datos.fechaHasta() : datos.fechaDesde());
        visita.setNota(datos.nota());
        visita.setFechaCreacion(LocalDateTime.now());
        visita.validarNueva(hoy());

        return aAutorizacion(visitaAutorizadaRepository.save(visita));
    }

    /** Autorizaciones que creó la persona logueada (las más recientes primero, máximo 60). */
    public List<Autorizacion> mias(String documentoUsuario) {
        return visitaAutorizadaRepository.findByAutorizadaPor(usuario(documentoUsuario).getId()).stream()
                .limit(60).map(this::aAutorizacion).toList();
    }

    public String cancelar(String documentoUsuario, String idAutorizacion) {
        UsuarioModel usuario = usuario(documentoUsuario);
        VisitaAutorizadaModel visita = existente(idAutorizacion);
        if (!usuario.getId().equals(visita.getIdAutorizadaPor()))
            throw new ValidationException("Solo quien creó la autorización puede cancelarla");
        visita.cancelar(hoy());
        visitaAutorizadaRepository.save(visita);
        return "Autorización de " + visita.nombreCompleto() + " cancelada";
    }

    // ── Portería ───────────────────────────────────────────────────────────

    /** Todas las visitas autorizadas para hoy en el conjunto. */
    public List<Autorizacion> esperadasHoy() {
        return visitaAutorizadaRepository.findVigentesEn(hoy()).stream().map(this::aAutorizacion).toList();
    }

    /** Autorización vigente hoy para este documento (y apartamento, si se indica). */
    public Optional<VisitaAutorizadaModel> vigenteParaDocumento(String numeroDocumento, String idApartamento) {
        String documento = ResidenteModel.normalizarDocumento(numeroDocumento);
        if (documento == null) return Optional.empty();
        return visitaAutorizadaRepository.findVigentesEn(hoy()).stream()
                .filter(v -> documento.equals(v.getNumeroDocumento()))
                .filter(v -> idApartamento == null || idApartamento.equals(v.getIdApartamento()))
                .findFirst();
    }

    /** Autorización con la que portería registra un ingreso: debe existir y estar vigente hoy. */
    public VisitaAutorizadaModel vigenteParaIngreso(String idAutorizacion) {
        VisitaAutorizadaModel visita = existente(idAutorizacion);
        if (visita.isCancelada())
            throw new ValidationException("El propietario canceló esta autorización");
        if (!visita.vigenteEn(hoy()))
            throw new ValidationException("Esta autorización es del " + visita.getFechaDesde()
                    + " al " + visita.getFechaHasta() + "; hoy no está vigente");
        return visita;
    }

    /** Nombre de quien autorizó (para mostrar en las listas de portería). */
    public String nombreAutor(String idAutorizacion) {
        if (idAutorizacion == null) return null;
        return visitaAutorizadaRepository.findById(idAutorizacion)
                .flatMap(v -> usuarioRepository.findById(v.getIdAutorizadaPor()))
                .map(u -> u.getNombre() + " " + u.getApellido())
                .orElse(null);
    }

    // ── Apoyo ──────────────────────────────────────────────────────────────

    private UsuarioModel usuario(String documento) {
        return usuarioRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
    }

    private VisitaAutorizadaModel existente(String id) {
        if (id == null || id.isBlank()) throw new ValidationException("Falta indicar la autorización");
        return visitaAutorizadaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe esa autorización de visita"));
    }

    private Autorizacion aAutorizacion(VisitaAutorizadaModel v) {
        LocalDate hoy = hoy();
        Estado estado = v.isCancelada() ? Estado.CANCELADA
                : v.vencida(hoy) ? Estado.VENCIDA
                : v.vigenteEn(hoy) ? Estado.VIGENTE
                : Estado.PROGRAMADA;
        String numeroApartamento = apartamentoRepository.findById(v.getIdApartamento())
                .map(ApartamentoModel::getNumero).orElse("—");
        String autor = usuarioRepository.findById(v.getIdAutorizadaPor())
                .map(u -> u.getNombre() + " " + u.getApellido()).orElse("—");
        return new Autorizacion(v.getId(), numeroApartamento, v.getNombreVisitante(), v.getApellidoVisitante(),
                v.getNumeroDocumento(), v.getPlaca(), v.getFechaDesde(), v.getFechaHasta(), v.getNota(), estado,
                visitaAutorizadaRepository.contarIngresos(v.getId()), autor);
    }
}
