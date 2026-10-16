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
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.VisitaAutorizadaModel;
import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
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
    public record Visita(String id, Tipo tipo, String numeroDocumento, String nombreCompleto,
                         String numeroApartamento, String placa, String numeroParqueadero,
                         String entrada, String salida, String autorizadaPor) {}

    /** Datos de un visitante que ya vino antes, para autocompletar el formulario. */
    public record Visitante(String numeroDocumento, String nombre, String apellido, Long celular,
                            boolean bloqueado, String ultimaPlaca, int visitasPrevias) {}

    /**
     * @param idAutorizacion       visita autorizada por el propietario (opcional): completa los datos que falten
     * @param documentoRegistrador documento del funcionario que registra (sale del JWT, para auditoría)
     * @param autorizadoPor        quién dio el paso (residente que autorizó); obligatorio si no hay
     *                             visita autorizada previamente
     */
    public record Ingreso(String numeroDocumento, String nombre, String apellido, String celular,
                          String numeroApartamento, boolean conVehiculo, String placa, String numeroParqueadero,
                          String idAutorizacion, String documentoRegistrador, String autorizadoPor) {
        public Ingreso(String numeroDocumento, String nombre, String apellido, String celular,
                       String numeroApartamento, boolean conVehiculo, String placa, String numeroParqueadero,
                       String autorizadoPor) {
            this(numeroDocumento, nombre, apellido, celular, numeroApartamento, conVehiculo, placa, numeroParqueadero,
                    null, null, autorizadoPor);
        }
    }

    /** Quién vive en un apartamento, para que portería sepa a quién pedir autorización. */
    public record Habitantes(String numeroApartamento, List<String> propietarios) {}

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
    @Autowired private VisitaAutorizadaService visitaAutorizadaService;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UsuarioApartamentoRepository usuarioApartamentoRepository;

    // ── Consultas ──────────────────────────────────────────────────────────

    public Visitante buscarVisitante(String numeroDocumento) {
        String documento = documento(numeroDocumento);
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

        // Con autorización del propietario: lo que portería no escribió sale de ahí.
        VisitaAutorizadaModel autorizacion = vacio(datos.idAutorizacion()) ? null
                : visitaAutorizadaService.vigenteParaIngreso(datos.idAutorizacion());
        if (autorizacion != null && vacio(datos.numeroDocumento()) && vacio(autorizacion.getNumeroDocumento()))
            throw new ValidationException("El propietario no dejó el documento de " + autorizacion.nombreCompleto()
                    + ": pídeselo y escríbelo para registrar el ingreso");
        String documento = documento(vacio(datos.numeroDocumento()) && autorizacion != null
                ? autorizacion.getNumeroDocumento() : datos.numeroDocumento());
        String nombreIngresado = autorizacion != null && vacio(datos.nombre()) ? autorizacion.getNombreVisitante() : datos.nombre();
        String apellidoIngresado = autorizacion != null && vacio(datos.apellido()) ? autorizacion.getApellidoVisitante() : datos.apellido();

        ApartamentoModel apartamento;
        if (autorizacion != null) {
            apartamento = apartamentoRepository.findById(autorizacion.getIdApartamento())
                    .orElseThrow(() -> new NotFoundException("El apartamento de la autorización ya no existe"));
        } else {
            if (vacio(datos.numeroApartamento()))
                throw new ValidationException("Indica a qué apartamento va el visitante");
            apartamento = apartamentoRepository.findByNumero(datos.numeroApartamento().trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new NotFoundException("El apartamento " + datos.numeroApartamento().trim() + " no existe"));
            // Sin elegirla, si el documento tiene autorización vigente hoy para ese apartamento, se asocia sola.
            autorizacion = visitaAutorizadaService.vigenteParaDocumento(documento, apartamento.getId()).orElse(null);
        }
        RequiereActivo.apartamento(apartamento);

        // Quién dio el paso: con visita autorizada, el propietario que la creó;
        // si no, el vigilante debe indicar quién autorizó (citófono, en persona).
        String autorizadoPor = autorizacion != null
                ? java.util.Objects.requireNonNullElse(visitaAutorizadaService.nombreAutor(autorizacion.getId()),
                        "Visita autorizada por el propietario")
                : (vacio(datos.autorizadoPor()) ? null : datos.autorizadoPor().trim());
        if (autorizadoPor == null)
            throw new ValidationException("Indica quién autorizó el ingreso (el residente que dio el paso)");
        if (autorizadoPor.length() > 150)
            throw new ValidationException("El nombre de quien autorizó es demasiado largo");

        ResidenteModel visitante = residenteRepository.findByNumeroDocumento(documento)
                .orElseGet(() -> crearVisitante(documento, nombreIngresado, apellidoIngresado, datos.celular()));
        if (!visitante.isActivo())
            throw new ValidationException(visitante.getNombre() + " " + visitante.getApellido()
                    + " tiene el ingreso bloqueado. Consulta con la administración.");
        if (!registroVisitanteRepository.findByIdResidenteAndFechaHoraSalidaIsNull(visitante.getId()).isEmpty()
                || !registroParqueaderoRepository.findByIdResidenteAndFechaHoraSalidaIsNull(visitante.getId()).isEmpty())
            throw new ValidationException(visitante.getNombre() + " " + visitante.getApellido()
                    + " ya está dentro del conjunto. Registra su salida antes de un nuevo ingreso.");

        String ahora = LocalDateTime.now().format(FORMATO);
        String nombre = visitante.getNombre() + " " + visitante.getApellido();
        String idAutorizacion = autorizacion == null ? null : autorizacion.getId();
        String idRegistrador = vacio(datos.documentoRegistrador()) ? null
                : usuarioRepository.findByNumeroDocumento(datos.documentoRegistrador()).map(UsuarioModel::getId).orElse(null);
        String conAutorizacion = " · autorizó " + autorizadoPor;

        if (!datos.conVehiculo()) {
            RegistroVisitanteModel registro = new RegistroVisitanteModel(visitante.getId(), apartamento.getId(), ahora);
            registro.setIdVisitaAutorizada(idAutorizacion);
            registro.setIdRegistradoPor(idRegistrador);
            registro.setAutorizadoPor(autorizadoPor);
            registroVisitanteRepository.save(registro);
            return "Ingreso registrado: " + nombre + " → apartamento " + apartamento.getNumero() + conAutorizacion;
        }

        String placa = VehiculoModel.normalizarPlaca(vacio(datos.placa()) && autorizacion != null
                ? autorizacion.getPlaca() : datos.placa());
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
        registro.setIdVisitaAutorizada(idAutorizacion);
        registro.setIdRegistradoPor(idRegistrador);
        registro.setAutorizadoPor(autorizadoPor);
        registroParqueaderoRepository.save(registro);
        return "Ingreso registrado: " + nombre + " (" + placa + ") → apartamento " + apartamento.getNumero()
                + ", parqueadero " + parqueadero.getNumero() + conAutorizacion;
    }

    /** Registros viejos no tienen autorizado_por: se usa el autor de la visita autorizada. */
    private String quienAutorizo(String autorizadoPor, String idVisitaAutorizada) {
        return autorizadoPor != null ? autorizadoPor : visitaAutorizadaService.nombreAutor(idVisitaAutorizada);
    }

    /**
     * Libro de portería, consulta puntual: solo los NOMBRES de los
     * propietarios de UN apartamento (sin documento, correo ni teléfono),
     * lo mínimo para saber a quién pedir la autorización.
     */
    public Habitantes habitantes(String numeroApartamento) {
        if (vacio(numeroApartamento)) throw new ValidationException("Indica el apartamento");
        ApartamentoModel apartamento = apartamentoRepository.findByNumero(numeroApartamento.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new NotFoundException("El apartamento " + numeroApartamento.trim() + " no existe"));
        List<String> nombres = usuarioApartamentoRepository.findUsuarioIdsByApartamento(apartamento.getId()).stream()
                .map(usuarioRepository::findById)
                .flatMap(java.util.Optional::stream)
                .filter(UsuarioModel::isActivo)
                .map(u -> u.getNombre() + " " + u.getApellido())
                .sorted()
                .toList();
        return new Habitantes(apartamento.getNumero(), nombres);
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

    private String documento(String valor) {
        String documento = ResidenteModel.normalizarDocumento(valor);
        if (documento == null)
            throw new ValidationException("El número de documento es obligatorio");
        if (documento.length() < 4 || documento.length() > 20)
            throw new ValidationException("El número de documento debe tener entre 4 y 20 caracteres");
        return documento;
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private ResidenteModel crearVisitante(String documento, String nombreNuevo, String apellidoNuevo, String celularTexto) {
        if (vacio(nombreNuevo) || vacio(apellidoNuevo))
            throw new ValidationException("Es la primera visita de este documento: escribe su nombre y apellido");
        Long celular = null;
        if (!vacio(celularTexto)) {
            String digitos = celularTexto.replaceAll("[^0-9]", "");
            if (digitos.length() < 7 || digitos.length() > 15)
                throw new ValidationException("El celular no es válido");
            celular = Long.valueOf(digitos);
        }
        ResidenteModel nuevo = new ResidenteModel(documento, nombreNuevo.trim(), apellidoNuevo.trim(), celular,
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
                numeroApartamento(r.getIdApartamento()), null, null, r.getFechaHoraEntrada(), r.getFechaHoraSalida(),
                quienAutorizo(r.getAutorizadoPor(), r.getIdVisitaAutorizada()));
    }

    private Visita aVisita(RegistroParqueaderoVisitanteModel r) {
        ResidenteModel v = residenteRepository.findById(r.getIdResidente()).orElse(null);
        String parqueadero = parqueaderoRepository.findById(r.getIdParqueadero()).map(ParqueaderoModel::getNumero).orElse(null);
        return new Visita(r.getId(), Tipo.VEHICULO, v == null ? null : v.getNumeroDocumento(), nombre(v),
                numeroApartamento(r.getIdApartamento()), r.getPlaca(), parqueadero, r.getFechaHoraEntrada(), r.getFechaHoraSalida(),
                quienAutorizo(r.getAutorizadoPor(), r.getIdVisitaAutorizada()));
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
