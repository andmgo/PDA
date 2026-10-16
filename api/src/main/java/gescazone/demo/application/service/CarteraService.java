package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.application.port.PasarelaPagos;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.CargoModel;
import gescazone.demo.domain.model.ConfiguracionCarteraModel;
import gescazone.demo.domain.model.PagoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.CarteraRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Pagos y cartera (Bloque J): cuota de administración igual para todos,
 * cobro por reserva del salón, pagos en línea (pasarela) y pagos manuales
 * de la administración. El saldo siempre se calcula con los pagos
 * APROBADOS; nada de saldos guardados que se puedan desincronizar.
 */
@Service
public class CarteraService {

    public enum EstadoCargo { PAGADO, PENDIENTE, VENCIDO }

    public record PagoVista(String id, String referencia, BigDecimal valor, String medio, String estado,
                            String soporte, LocalDateTime fecha) {}

    public record CargoVista(String id, String concepto, String descripcion, BigDecimal valor, BigDecimal pagado,
                             BigDecimal saldo, LocalDate fechaVencimiento, EstadoCargo estado, List<PagoVista> pagos) {}

    public record EstadoCuenta(String idApartamento, String numeroApartamento, List<String> propietarios,
                               BigDecimal saldo, BigDecimal saldoVencido, boolean enMora, List<CargoVista> cargos) {}

    public record FilaCartera(String idApartamento, String numeroApartamento, List<String> propietarios,
                              BigDecimal saldo, BigDecimal saldoVencido, boolean enMora) {}

    public record ResumenCartera(BigDecimal porCobrar, BigDecimal vencido, long apartamentosEnMora,
                                 long apartamentosAlDia, List<FilaCartera> apartamentos) {}

    public record PagoEnLinea(String referencia, String url) {}

    @Autowired private CarteraRepository carteraRepository;
    @Autowired private ApartamentoRepository apartamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private UsuarioApartamentoRepository usuarioApartamentoRepository;
    @Autowired private PasarelaPagos pasarela;

    /** Separado para poder fijar "hoy" en las pruebas. */
    LocalDate hoy() {
        return LocalDate.now();
    }

    static String pesos(BigDecimal valor) {
        NumberFormat f = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-CO"));
        f.setMaximumFractionDigits(0);
        return f.format(valor).replace(" ", " ");
    }

    public boolean pagoEnLineaHabilitado() {
        return pasarela.configurada();
    }

    // ── Configuración ──────────────────────────────────────────────────────

    public ConfiguracionCarteraModel configuracion() {
        return carteraRepository.configuracion();
    }

    public ConfiguracionCarteraModel guardarConfiguracion(BigDecimal cuota, BigDecimal valorReserva, Integer dia) {
        ConfiguracionCarteraModel c = carteraRepository.configuracion();
        if (cuota != null) c.setCuotaAdministracion(cuota);
        if (valorReserva != null) c.setValorReserva(valorReserva);
        if (dia != null) c.setDiaVencimiento(dia);
        c.validar();
        return carteraRepository.guardarConfiguracion(c);
    }

    // ── Cuotas de administración ───────────────────────────────────────────

    /** Genera la cuota del mes para todos los apartamentos activos (no duplica). */
    @Transactional
    public String generarCuotas(String periodo) {
        String mes = periodo == null || periodo.isBlank() ? YearMonth.from(hoy()).toString() : periodo.trim();
        try {
            YearMonth.parse(mes);
        } catch (Exception e) {
            throw new ValidationException("Mes inválido: usa el formato AAAA-MM (ej. 2026-10)");
        }
        ConfiguracionCarteraModel conf = carteraRepository.configuracion();
        if (conf.getCuotaAdministracion().signum() <= 0)
            throw new ValidationException("Primero define el valor de la cuota de administración");
        LocalDate vence = conf.vencimientoDe(mes);
        int creadas = 0, existentes = 0;
        for (ApartamentoModel apto : apartamentoRepository.findAll()) {
            if (!apto.isActivo()) continue;
            if (carteraRepository.existeCuota(apto.getId(), mes)) {
                existentes++;
                continue;
            }
            carteraRepository.guardarCargo(CargoModel.cuotaAdministracion(apto.getId(), mes, conf.getCuotaAdministracion(), vence));
            creadas++;
        }
        String texto = "Se generaron " + creadas + " cuota" + (creadas == 1 ? "" : "s") + " de "
                + pesos(conf.getCuotaAdministracion()) + " para " + CargoModel.periodoLegible(mes);
        return existentes == 0 ? texto : texto + " (" + existentes + " apartamento" + (existentes == 1 ? "" : "s")
                + " ya la tenía" + (existentes == 1 ? "" : "n") + ")";
    }

    // ── Reservas del salón social ──────────────────────────────────────────

    /**
     * Cobro de una reserva nueva, al apartamento de quien reserva. Si el
     * valor configurado es 0 o la persona no tiene apartamento (personal
     * del conjunto), no se cobra.
     */
    public void cobrarReserva(String idReserva, String idUsuario, String numeroSalon, LocalDate fecha) {
        BigDecimal valor = carteraRepository.configuracion().getValorReserva();
        if (valor.signum() <= 0) return;
        List<String> aptos = usuarioApartamentoRepository.findApartamentoIdsByUsuario(idUsuario);
        if (aptos.isEmpty()) return;
        LocalDate vence = fecha.isBefore(hoy()) ? hoy() : fecha; // se paga a más tardar el día de la reserva
        carteraRepository.guardarCargo(CargoModel.reserva(aptos.get(0), idReserva,
                "Reserva salón " + numeroSalon + " · " + fecha, valor, vence));
    }

    /** Al cancelar una reserva: se anula su cobro, salvo que ya tenga pagos. */
    public void anularCobroReserva(String idReserva) {
        carteraRepository.cargoDeReserva(idReserva).ifPresent(cargo -> {
            BigDecimal pagado = carteraRepository.pagadoPorCargo(List.of(cargo.getId())).getOrDefault(cargo.getId(), BigDecimal.ZERO);
            if (pagado.signum() > 0)
                throw new ValidationException("Esta reserva ya fue pagada: para cancelarla habla con la administración");
            cargo.anular();
            carteraRepository.guardarCargo(cargo);
        });
    }

    // ── Consultas ──────────────────────────────────────────────────────────

    /** Estado de cuenta de cada apartamento de la persona logueada. */
    public List<EstadoCuenta> miCuenta(String documentoUsuario) {
        UsuarioModel usuario = usuario(documentoUsuario);
        return usuarioApartamentoRepository.findApartamentoIdsByUsuario(usuario.getId()).stream()
                .map(this::estadoApartamento).toList();
    }

    public EstadoCuenta estadoApartamento(String idApartamento) {
        ApartamentoModel apto = apartamentoRepository.findById(idApartamento)
                .orElseThrow(() -> new NotFoundException("El apartamento no existe"));
        List<CargoModel> cargos = carteraRepository.cargosVigentes(List.of(idApartamento));
        List<String> ids = cargos.stream().map(CargoModel::getId).toList();
        List<PagoModel> pagos = carteraRepository.pagosDeCargos(ids);
        Map<String, BigDecimal> pagado = carteraRepository.pagadoPorCargo(ids);
        LocalDate hoy = hoy();
        List<CargoVista> vistas = cargos.stream()
                .sorted(Comparator.comparing(CargoModel::getFechaVencimiento).reversed())
                .map(c -> vista(c, pagado.getOrDefault(c.getId(), BigDecimal.ZERO), pagos, hoy))
                .toList();
        BigDecimal saldo = vistas.stream().map(CargoVista::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vencido = vistas.stream().filter(v -> v.estado() == EstadoCargo.VENCIDO)
                .map(CargoVista::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new EstadoCuenta(apto.getId(), apto.getNumero(), propietarios(apto.getId()), saldo, vencido,
                vencido.signum() > 0, vistas);
    }

    /** Cartera de todo el conjunto: lo que se debe y quién está en mora. */
    public ResumenCartera resumen() {
        List<CargoModel> cargos = carteraRepository.cargosVigentes(null);
        Map<String, BigDecimal> pagado = carteraRepository.pagadoPorCargo(cargos.stream().map(CargoModel::getId).toList());
        LocalDate hoy = hoy();
        List<FilaCartera> filas = apartamentoRepository.findAll().stream()
                .filter(ApartamentoModel::isActivo)
                .map(apto -> {
                    BigDecimal saldo = BigDecimal.ZERO, vencido = BigDecimal.ZERO;
                    for (CargoModel c : cargos) {
                        if (!apto.getId().equals(c.getIdApartamento())) continue;
                        BigDecimal p = pagado.getOrDefault(c.getId(), BigDecimal.ZERO);
                        saldo = saldo.add(c.saldo(p));
                        if (c.vencido(hoy, p)) vencido = vencido.add(c.saldo(p));
                    }
                    return new FilaCartera(apto.getId(), apto.getNumero(), propietarios(apto.getId()), saldo, vencido,
                            vencido.signum() > 0);
                })
                .sorted(Comparator.comparing(FilaCartera::saldoVencido).reversed()
                        .thenComparing(FilaCartera::saldo, Comparator.reverseOrder())
                        .thenComparing(FilaCartera::numeroApartamento))
                .toList();
        BigDecimal porCobrar = filas.stream().map(FilaCartera::saldo).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal vencido = filas.stream().map(FilaCartera::saldoVencido).reduce(BigDecimal.ZERO, BigDecimal::add);
        long enMora = filas.stream().filter(FilaCartera::enMora).count();
        return new ResumenCartera(porCobrar, vencido, enMora, filas.size() - enMora, filas);
    }

    // ── Pagos ──────────────────────────────────────────────────────────────

    /** Pago registrado por la administración (transferencia, consignación, efectivo). */
    @Transactional
    public String registrarPagoManual(String documentoAdmin, String idCargo, BigDecimal valor, String medio, String soporte) {
        CargoModel cargo = cargoVigente(idCargo);
        BigDecimal saldo = saldoDe(cargo);
        if (saldo.signum() <= 0) throw new ValidationException("Este cobro ya está pagado");
        BigDecimal abono = valor == null ? saldo : valor;
        if (abono.compareTo(saldo) > 0)
            throw new ValidationException("El pago (" + pesos(abono) + ") es mayor que el saldo (" + pesos(saldo) + ")");
        PagoModel.Medio m;
        try {
            m = PagoModel.Medio.valueOf(medio == null ? "" : medio.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Elige cómo pagó: transferencia, consignación o efectivo");
        }
        PagoModel pago = PagoModel.manual(cargo.getId(), abono, m, soporte, usuario(documentoAdmin).getId());
        carteraRepository.guardarPago(pago);
        return "Pago de " + pesos(abono) + " registrado: " + cargo.getDescripcion();
    }

    /** El propietario paga el saldo de uno de sus cobros en la pasarela. */
    @Transactional
    public PagoEnLinea iniciarPagoEnLinea(String documentoUsuario, String idCargo, String urlRegreso) {
        if (!pasarela.configurada())
            throw new ValidationException("El pago en línea aún no está habilitado. Puedes pagar por transferencia y "
                    + "enviar el comprobante a la administración.");
        UsuarioModel usuario = usuario(documentoUsuario);
        CargoModel cargo = cargoVigente(idCargo);
        if (!usuarioApartamentoRepository.existe(usuario.getId(), cargo.getIdApartamento()))
            throw new ValidationException("Solo puedes pagar cobros de tus apartamentos");
        BigDecimal saldo = saldoDe(cargo);
        if (saldo.signum() <= 0) throw new ValidationException("Este cobro ya está pagado");
        PagoModel pago = carteraRepository.guardarPago(PagoModel.enLinea(cargo.getId(), saldo, usuario.getId()));
        return new PagoEnLinea(pago.getReferencia(), pasarela.urlCheckout(pago, usuario.getCorreo(), urlRegreso));
    }

    /**
     * Confirma un pago con lo que dice la PASARELA (nunca con lo que diga el
     * navegador): se consulta la transacción y se verifica referencia y valor.
     */
    @Transactional
    public String confirmarTransaccion(String idTransaccion) {
        PasarelaPagos.Transaccion t = pasarela.consultar(idTransaccion)
                .orElseThrow(() -> new NotFoundException("La pasarela no reconoce esa transacción"));
        return aplicar(t);
    }

    /** Evento (webhook) de la pasarela, con firma verificada. */
    @Transactional
    public String procesarEvento(Map<String, Object> evento) {
        if (!pasarela.eventoAutentico(evento)) throw new ValidationException("Evento con firma inválida");
        return pasarela.transaccionDeEvento(evento).map(this::aplicar).orElse("Evento ignorado");
    }

    private String aplicar(PasarelaPagos.Transaccion t) {
        PagoModel pago = carteraRepository.pagoPorReferencia(t.referencia())
                .orElseThrow(() -> new NotFoundException("No hay un pago con la referencia " + t.referencia()));
        if (!t.final_()) return "PENDIENTE";
        if (t.aprobada() && t.valorEnCentavos() != pago.valorEnCentavos())
            throw new ValidationException("El valor pagado no coincide con el cobro");
        pago.resolver(t.aprobada(), t.id());
        carteraRepository.guardarPago(pago);
        return pago.getEstado().name();
    }

    // ── Apoyo ──────────────────────────────────────────────────────────────

    private CargoVista vista(CargoModel c, BigDecimal pagado, List<PagoModel> pagos, LocalDate hoy) {
        BigDecimal saldo = c.saldo(pagado);
        EstadoCargo estado = saldo.signum() == 0 ? EstadoCargo.PAGADO
                : c.vencido(hoy, pagado) ? EstadoCargo.VENCIDO : EstadoCargo.PENDIENTE;
        List<PagoVista> suyos = pagos.stream()
                .filter(p -> Objects.equals(p.getIdCargo(), c.getId()))
                .sorted(Comparator.comparing(PagoModel::getFechaCreacion).reversed())
                .map(p -> new PagoVista(p.getId(), p.getReferencia(), p.getValor(), p.getMedio().name(),
                        p.getEstado().name(), p.getSoporte(),
                        p.getFechaAprobacion() != null ? p.getFechaAprobacion() : p.getFechaCreacion()))
                .toList();
        return new CargoVista(c.getId(), c.getConcepto().name(), c.getDescripcion(), c.getValor(), pagado, saldo,
                c.getFechaVencimiento(), estado, suyos);
    }

    private BigDecimal saldoDe(CargoModel cargo) {
        BigDecimal pagado = carteraRepository.pagadoPorCargo(List.of(cargo.getId())).getOrDefault(cargo.getId(), BigDecimal.ZERO);
        return cargo.saldo(pagado);
    }

    private CargoModel cargoVigente(String idCargo) {
        if (idCargo == null || idCargo.isBlank()) throw new ValidationException("Indica el cobro");
        CargoModel cargo = carteraRepository.cargo(idCargo)
                .orElseThrow(() -> new NotFoundException("No existe ese cobro"));
        if (cargo.isAnulado()) throw new ValidationException("Este cobro fue anulado");
        return cargo;
    }

    private List<String> propietarios(String idApartamento) {
        return usuarioApartamentoRepository.findUsuarioIdsByApartamento(idApartamento).stream()
                .map(usuarioRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(u -> u.getNombre() + " " + u.getApellido())
                .sorted().toList();
    }

    private UsuarioModel usuario(String documento) {
        return usuarioRepository.findByNumeroDocumento(documento)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
    }
}
