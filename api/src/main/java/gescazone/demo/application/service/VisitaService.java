package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.TipoResidenteModel;
import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.domain.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Portería en un solo paso (Fase 3). Antes, para dejar entrar a alguien había
 * que crearlo primero como Residente tipo "Visitante" en Gestión de Datos y
 * después registrar la entrada en otra pantalla — y además un visitante solo
 * podía visitar un mismo apartamento UNA vez en la vida (el servicio viejo
 * rechazaba cualquier registro previo, aunque ya hubiera salido). Aquí:
 * el visitante se crea solo si no existe, solo se bloquea a quien ya está
 * adentro, y el parqueadero de visitante se asigna automáticamente.
 *
 * Reutiliza las tablas existentes (registro_visitante para peatones,
 * registro_parqueadero_visitante para vehículos) para no perder el historial.
 */
@Service
public class VisitaService {

    public enum Tipo { PEATON, VEHICULO }

    /** Una visita (peatonal o en vehículo) tal como la ve portería. */
    public record Visita(String id, Tipo tipo, Integer numeroDocumento, String nombreCompleto,
                         String numeroApartamento, String placa, String numeroParqueadero,
                         String entrada, String salida) {}

    /** Datos de un visitante que ya vino antes, para autocompletar el formulario. */
    public record Visitante(Integer numeroDocumento, String nombre, String apellido, Long celular,
                            boolean bloqueado, String ultimaPlaca, int visitasPrevias) {}

    public record Ingreso(String numeroDocumento, String nombre, String apellido, String celular,
                          String numeroApartamento, boolean conVehiculo, String placa, String numeroParqueadero) {}

    static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String TIPO_VISITANTE = "Visitante";
    private static final String ESTADO_DISPONIBLE = "Disponible";
    // Placas de visitantes: más flexible que la validación de residentes
    // (puede ser de otra ciudad/país, diplomática, etc.) — 4 a 7 caracteres.
    private static final Pattern PLACA_VISITANTE = Pattern.compile("^[A-Z0-9]{4,7}$");

    @Autowired private ResidenteRepository residenteRepository;
    @Autowired private ApartamentoRepository apartamentoRepository;
    @Autowired private ParqueaderoRepository parqueaderoRepository;
    @Autowired private RegistroVisitanteRepository registroVisitanteRepository;
    @Autowired private RegistroParqueaderoVisitanteRepository registroParqueaderoRepository;
    @Autowired private VehiculoRepository vehiculoRepository;

    // ── Consultas ──────────────────────────────────────────────────────────

    public Visitante buscarVisitante(String numeroDocumento) {
        Integer documento = documento(numeroDocumento);
        ResidenteModel residente = residenteRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new NotFoundException("No hay visitas previas con el documento " + documento));
        List<RegistroParqueaderoVisitanteModel> conVehiculo = registroParqueaderoRepository.findByIdResidente(residente.getId());
        String ultimaPlaca = conVehiculo.stream()
                .filter(r -> r.getPlaca() != null)
                .max(Comparator.comparing(r -> r.getFechaHoraEntrada() == null ? "" : r.getFechaHoraEntrada()))
                .map(RegistroParqueaderoVisitanteModel::getPlaca).orElse(null);
        int visitas = conVehiculo.size() + registroVisitanteRepository.findByIdResidente(residente.getId()).size();
        return new Visitante(residente.getNumeroDocumento(), residente.getNombre(), residente.getApellido(),
                residente.getCelular(), !residente.isActivo(), ultimaPlaca, visitas);
    }

    /** Quiénes están dentro del conjunto ahora mismo (sin salida registrada). */
    public List<Visita> dentro() {
        return ordenar(Stream.concat(
                registroVisitanteRepository.findByFechaHoraSalidaIsNull().stream().map(this::aVisita),
                registroParqueaderoRepository.findByFechaHoraSalidaIsNull().stream().map(this::aVisita)));
    }

    /** Todas las visitas que entraron hoy (incluye las que ya salieron). */
    public List<Visita> deHoy() {
        String hoy = LocalDate.now().toString();
        return ordenar(Stream.concat(
                registroVisitanteRepository.findLatestRegistros().stream()
                        .filter(r -> r.getFechaHoraEntrada() != null && r.getFechaHoraEntrada().startsWith(hoy))
                        .map(this::aVisita),
                registroParqueaderoRepository.findLatestRegistros().stream()
                        .filter(r -> r.getFechaHoraEntrada() != null && r.getFechaHoraEntrada().startsWith(hoy))
                        .map(this::aVisita)));
    }

    /** Parqueaderos que se le pueden dar a un visitante en este momento. */
    public List<String> parqueaderosLibres() {
        Set<String> ocupados = new HashSet<>(vehiculoRepository.findIdsParqueaderoAsignados());
        registroParqueaderoRepository.findByFechaHoraSalidaIsNull()
                .forEach(r -> ocupados.add(r.getIdParqueadero()));
        return parqueaderoRepository.findAll().stream()
                .filter(ParqueaderoModel::isActivo)
                .filter(p -> p.getEstado() == null || ESTADO_DISPONIBLE.equalsIgnoreCase(p.getEstado().getNombreEstado()))
                .filter(p -> !ocupados.contains(p.getId()))
                .map(ParqueaderoModel::getNumero)
                .sorted(Comparator.comparingLong(VisitaService::parteNumerica).thenComparing(Comparator.naturalOrder()))
                .toList();
    }

    /** "P-98" → 98, para ordenar P-98 antes que P-100 (orden de texto los invierte). */
    private static long parteNumerica(String numero) {
        String digitos = numero == null ? "" : numero.replaceAll("\\D", "");
        try {
            return digitos.isEmpty() ? Long.MAX_VALUE : Long.parseLong(digitos);
        } catch (NumberFormatException e) {
            return Long.MAX_VALUE;
        }
    }

    // ── Ingreso y salida ───────────────────────────────────────────────────

    @Transactional
    public String registrarIngreso(Ingreso datos) {
        if (datos == null) throw new ValidationException("Faltan los datos del visitante");
        Integer documento = documento(datos.numeroDocumento());

        if (datos.numeroApartamento() == null || datos.numeroApartamento().isBlank())
            throw new ValidationException("Indica a qué apartamento va el visitante");
        ApartamentoModel apartamento = apartamentoRepository.findByNumero(datos.numeroApartamento().trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("El apartamento " + datos.numeroApartamento().trim() + " no existe"));
        if (!apartamento.isActivo())
            throw new ValidationException("El apartamento " + apartamento.getNumero() + " está desactivado");

        ResidenteModel visitante = residenteRepository.findByNumeroDocumento(documento)
                .orElseGet(() -> crearVisitante(documento, datos));
        if (!visitante.isActivo())
            throw new ValidationException(visitante.getNombre() + " " + visitante.getApellido()
                    + " tiene el ingreso bloqueado. Consulta con la administración.");
        if (!registroVisitanteRepository.findByIdResidenteAndFechaHoraSalidaIsNull(visitante.getId()).isEmpty()
                || !registroParqueaderoRepository.findByIdResidenteAndFechaHoraSalidaIsNull(visitante.getId()).isEmpty())
            throw new ValidationException(visitante.getNombre() + " " + visitante.getApellido()
                    + " ya está dentro del conjunto. Registra su salida antes de un nuevo ingreso.");

        String ahora = LocalDateTime.now().format(FORMATO);
        String nombre = visitante.getNombre() + " " + visitante.getApellido();

        if (!datos.conVehiculo()) {
            registroVisitanteRepository.save(new RegistroVisitanteModel(visitante.getId(), apartamento.getId(), ahora));
            return "Ingreso registrado: " + nombre + " → apartamento " + apartamento.getNumero();
        }

        String placa = VehiculoModel.normalizarPlaca(datos.placa());
        if (placa == null || !PLACA_VISITANTE.matcher(placa).matches())
            throw new ValidationException("Escribe una placa válida (ej. ABC123)");
        boolean placaAdentro = registroParqueaderoRepository.findByFechaHoraSalidaIsNull().stream()
                .anyMatch(r -> placa.equals(r.getPlaca()));
        if (placaAdentro)
            throw new ValidationException("El vehículo " + placa + " ya está dentro del conjunto");

        ParqueaderoModel parqueadero = elegirParqueadero(datos.numeroParqueadero());
        RegistroParqueaderoVisitanteModel registro =
                new RegistroParqueaderoVisitanteModel(visitante.getId(), parqueadero.getId(), ahora, placa);
        registro.setIdApartamento(apartamento.getId());
        registroParqueaderoRepository.save(registro);
        return "Ingreso registrado: " + nombre + " (" + placa + ") → apartamento " + apartamento.getNumero()
                + ", parqueadero " + parqueadero.getNumero();
    }

    public String registrarSalida(Tipo tipo, String idRegistro) {
        if (tipo == null || idRegistro == null || idRegistro.isBlank())
            throw new ValidationException("Falta indicar qué visita sale");
        String ahora = LocalDateTime.now().format(FORMATO);
        if (tipo == Tipo.PEATON) {
            RegistroVisitanteModel r = registroVisitanteRepository.findById(idRegistro)
                    .orElseThrow(() -> new NotFoundException("No se encontró la visita"));
            r.registrarSalida(ahora);
            registroVisitanteRepository.save(r);
        } else {
            RegistroParqueaderoVisitanteModel r = registroParqueaderoRepository.findById(idRegistro)
                    .orElseThrow(() -> new NotFoundException("No se encontró la visita"));
            r.registrarSalida(ahora);
            registroParqueaderoRepository.save(r);
        }
        return "Salida registrada";
    }

    // ── Apoyo ──────────────────────────────────────────────────────────────

    private Integer documento(String valor) {
        String limpio = valor == null ? "" : valor.replaceAll("[^0-9]", "");
        if (limpio.isEmpty())
            throw new ValidationException("El número de documento es obligatorio (solo números)");
        try {
            return Integer.valueOf(limpio);
        } catch (NumberFormatException e) {
            // residentes.numero_documento es INTEGER desde el esquema original.
            throw new ValidationException("Ese número de documento es demasiado largo para el sistema actual");
        }
    }

    private ResidenteModel crearVisitante(Integer documento, Ingreso datos) {
        if (datos.nombre() == null || datos.nombre().isBlank() || datos.apellido() == null || datos.apellido().isBlank())
            throw new ValidationException("Es la primera visita de este documento: escribe su nombre y apellido");
        Long celular = null;
        if (datos.celular() != null && !datos.celular().isBlank()) {
            String digitos = datos.celular().replaceAll("[^0-9]", "");
            if (digitos.length() < 7 || digitos.length() > 15)
                throw new ValidationException("El celular no es válido");
            celular = Long.valueOf(digitos);
        }
        ResidenteModel nuevo = new ResidenteModel(documento, datos.nombre().trim(), datos.apellido().trim(), celular,
                new TipoDocumentoModel("CC"), new TipoResidenteModel(TIPO_VISITANTE));
        return residenteRepository.save(nuevo);
    }

    private ParqueaderoModel elegirParqueadero(String numeroPedido) {
        List<String> libres = parqueaderosLibres();
        if (numeroPedido != null && !numeroPedido.isBlank()) {
            String numero = numeroPedido.trim().toUpperCase(Locale.ROOT);
            ParqueaderoModel pedido = parqueaderoRepository.findByNumero(numero)
                    .orElseThrow(() -> new NotFoundException("El parqueadero " + numero + " no existe"));
            if (!libres.contains(pedido.getNumero()))
                throw new ValidationException("El parqueadero " + numero + " no está libre en este momento");
            return pedido;
        }
        if (libres.isEmpty())
            throw new ValidationException("No hay parqueaderos libres para visitantes en este momento");
        return parqueaderoRepository.findByNumero(libres.get(0))
                .orElseThrow(() -> new NotFoundException("El parqueadero " + libres.get(0) + " no existe"));
    }

    private Visita aVisita(RegistroVisitanteModel r) {
        ResidenteModel v = residenteRepository.findById(r.getIdResidente()).orElse(null);
        return new Visita(r.getId(), Tipo.PEATON, v == null ? null : v.getNumeroDocumento(), nombre(v),
                numeroApartamento(r.getIdApartamento()), null, null, r.getFechaHoraEntrada(), r.getFechaHoraSalida());
    }

    private Visita aVisita(RegistroParqueaderoVisitanteModel r) {
        ResidenteModel v = residenteRepository.findById(r.getIdResidente()).orElse(null);
        String parqueadero = parqueaderoRepository.findById(r.getIdParqueadero()).map(ParqueaderoModel::getNumero).orElse(null);
        return new Visita(r.getId(), Tipo.VEHICULO, v == null ? null : v.getNumeroDocumento(), nombre(v),
                numeroApartamento(r.getIdApartamento()), r.getPlaca(), parqueadero, r.getFechaHoraEntrada(), r.getFechaHoraSalida());
    }

    private String nombre(ResidenteModel v) {
        return v == null ? "—" : v.getNombre() + " " + v.getApellido();
    }

    private String numeroApartamento(String idApartamento) {
        if (idApartamento == null) return "—";
        return apartamentoRepository.findById(idApartamento).map(ApartamentoModel::getNumero).orElse("—");
    }

    private List<Visita> ordenar(Stream<Visita> visitas) {
        List<Visita> lista = new ArrayList<>(visitas.toList());
        lista.sort(Comparator.comparing((Visita v) -> v.entrada() == null ? "" : v.entrada()).reversed());
        return lista;
    }
}
