package gescazone.demo.application.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CarteraServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 16);
    private static final BigDecimal CUOTA = new BigDecimal("350000");

    @Mock private CarteraRepository carteraRepository;
    @Mock private ApartamentoRepository apartamentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioApartamentoRepository usuarioApartamentoRepository;
    @Mock private PasarelaPagos pasarela;
    @Spy @InjectMocks private CarteraService cartera;

    private ApartamentoModel apto(String id, String numero, boolean activo) {
        ApartamentoModel a = new ApartamentoModel();
        a.setId(id);
        a.setNumero(numero);
        a.setActivo(activo);
        return a;
    }

    private UsuarioModel usuario(String id, String doc) {
        UsuarioModel u = new UsuarioModel();
        u.setId(id);
        u.setNumeroDocumento(doc);
        u.setNombre("Elena");
        u.setApellido("Vásquez");
        u.setCorreo("elena@correo.com");
        return u;
    }

    private CargoModel cargo(String id, String idApto, LocalDate vence) {
        CargoModel c = CargoModel.cuotaAdministracion(idApto, "2026-10", CUOTA, vence);
        c.setId(id);
        return c;
    }

    @BeforeEach
    void preparar() {
        doReturn(HOY).when(cartera).hoy();
        ConfiguracionCarteraModel conf = new ConfiguracionCarteraModel();
        conf.setCuotaAdministracion(CUOTA);
        conf.setValorReserva(new BigDecimal("80000"));
        conf.setDiaVencimiento(10);
        when(carteraRepository.configuracion()).thenReturn(conf);
        when(carteraRepository.guardarCargo(any())).thenAnswer(i -> i.getArgument(0));
        when(carteraRepository.guardarPago(any())).thenAnswer(i -> i.getArgument(0));
        when(usuarioRepository.findByNumeroDocumento("300000001")).thenReturn(Optional.of(usuario("u-elena", "300000001")));
        when(usuarioRepository.findByNumeroDocumento("123456780")).thenReturn(Optional.of(usuario("u-admin", "123456780")));
    }

    @Test
    void generarCuotas_soloAptosActivos_yNoDuplica() {
        when(apartamentoRepository.findAll()).thenReturn(List.of(
                apto("a1", "401", true), apto("a2", "402", true), apto("a3", "403", false)));
        when(carteraRepository.existeCuota("a2", "2026-10")).thenReturn(true);

        String r = cartera.generarCuotas("2026-10");

        assertThat(r).contains("Se generaron 1 cuota").contains("octubre 2026").contains("1 apartamento ya la tenía");
        verify(carteraRepository).guardarCargo(argThat(c -> "a1".equals(c.getIdApartamento())
                && c.getFechaVencimiento().equals(LocalDate.of(2026, 10, 10)) && c.getValor().equals(CUOTA)));
        verify(carteraRepository, times(1)).guardarCargo(any());
    }

    @Test
    void generarCuotas_sinValorDefinido_rechaza() {
        carteraRepository.configuracion().setCuotaAdministracion(BigDecimal.ZERO);
        assertThatThrownBy(() -> cartera.generarCuotas("2026-10")).hasMessageContaining("valor de la cuota");
    }

    @Test
    void pagoManual_mayorQueElSaldo_rechaza_yTransferenciaExigeSoporte() {
        when(carteraRepository.cargo("c1")).thenReturn(Optional.of(cargo("c1", "a1", HOY.plusDays(5))));
        when(carteraRepository.pagadoPorCargo(anyList())).thenReturn(Map.of("c1", new BigDecimal("300000")));

        assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c1", new BigDecimal("60000"), "EFECTIVO", null))
                .hasMessageContaining("mayor que el saldo");
        assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c1", null, "TRANSFERENCIA", " "))
                .hasMessageContaining("comprobante");

        String ok = cartera.registrarPagoManual("123456780", "c1", null, "transferencia", "TRX-991");
        assertThat(ok).contains("Pago de").contains("50.000");
        verify(carteraRepository).guardarPago(argThat(p -> p.getEstado() == PagoModel.Estado.APROBADO
                && p.getValor().equals(new BigDecimal("50000")) && "u-admin".equals(p.getIdRegistradoPor())));
    }

    @Test
    void estadoApartamento_calculaSaldoYMora() {
        when(apartamentoRepository.findById("a1")).thenReturn(Optional.of(apto("a1", "401", true)));
        CargoModel vencido = cargo("c1", "a1", HOY.minusDays(6));
        CargoModel alDia = cargo("c2", "a1", HOY.plusDays(20));
        CargoModel pagado = cargo("c3", "a1", HOY.minusDays(40));
        when(carteraRepository.cargosVigentes(List.of("a1"))).thenReturn(List.of(vencido, alDia, pagado));
        when(carteraRepository.pagadoPorCargo(anyList())).thenReturn(Map.of("c3", CUOTA));

        var cuenta = cartera.estadoApartamento("a1");

        assertThat(cuenta.saldo()).isEqualByComparingTo("700000");
        assertThat(cuenta.saldoVencido()).isEqualByComparingTo("350000");
        assertThat(cuenta.enMora()).isTrue();
        assertThat(cuenta.cargos()).extracting(CarteraService.CargoVista::estado).containsExactlyInAnyOrder(
                CarteraService.EstadoCargo.VENCIDO, CarteraService.EstadoCargo.PENDIENTE, CarteraService.EstadoCargo.PAGADO);
    }

    @Test
    void pagoEnLinea_sinPasarela_rechaza_yConPasarela_soloSusCobros() {
        when(carteraRepository.cargo("c1")).thenReturn(Optional.of(cargo("c1", "a1", HOY.plusDays(5))));
        when(pasarela.configurada()).thenReturn(false);
        assertThatThrownBy(() -> cartera.iniciarPagoEnLinea("300000001", "c1", null)).hasMessageContaining("aún no está habilitado");

        when(pasarela.configurada()).thenReturn(true);
        when(usuarioApartamentoRepository.existe("u-elena", "a1")).thenReturn(false);
        assertThatThrownBy(() -> cartera.iniciarPagoEnLinea("300000001", "c1", null)).hasMessageContaining("tus apartamentos");

        when(usuarioApartamentoRepository.existe("u-elena", "a1")).thenReturn(true);
        when(pasarela.urlCheckout(any(), any(), any())).thenReturn("https://checkout.wompi.co/p/?x");
        var r = cartera.iniciarPagoEnLinea("300000001", "c1", "http://localhost:8081/pagosYCartera");
        assertThat(r.url()).startsWith("https://checkout.wompi.co");
        verify(carteraRepository).guardarPago(argThat(p -> p.getEstado() == PagoModel.Estado.PENDIENTE
                && p.getValor().equals(CUOTA)));
    }

    @Test
    void confirmar_aprobadaConValorDistinto_noSeAprueba() {
        PagoModel pago = PagoModel.enLinea("c1", CUOTA, "u-elena");
        when(carteraRepository.pagoPorReferencia(pago.getReferencia())).thenReturn(Optional.of(pago));
        when(pasarela.consultar("tx-1")).thenReturn(Optional.of(
                new PasarelaPagos.Transaccion("tx-1", pago.getReferencia(), 100L, true, true)));

        assertThatThrownBy(() -> cartera.confirmarTransaccion("tx-1")).hasMessageContaining("no coincide");
        assertThat(pago.getEstado()).isEqualTo(PagoModel.Estado.PENDIENTE);

        when(pasarela.consultar("tx-2")).thenReturn(Optional.of(
                new PasarelaPagos.Transaccion("tx-2", pago.getReferencia(), 35_000_000L, true, true)));
        assertThat(cartera.confirmarTransaccion("tx-2")).isEqualTo("APROBADO");
        assertThat(pago.getTransaccionExterna()).isEqualTo("tx-2");
    }

    @Test
    void reserva_seCobraAlApartamento_yNoSeAnulaSiYaSePago() {
        when(usuarioApartamentoRepository.findApartamentoIdsByUsuario("u-elena")).thenReturn(List.of("a1"));
        cartera.cobrarReserva("r1", "u-elena", "S-10", HOY.plusDays(4));
        verify(carteraRepository).guardarCargo(argThat(c -> c.getConcepto() == CargoModel.Concepto.RESERVA
                && "r1".equals(c.getIdReserva()) && c.getValor().equals(new BigDecimal("80000"))));

        CargoModel cargo = CargoModel.reserva("a1", "r1", "Reserva", new BigDecimal("80000"), HOY.plusDays(4));
        cargo.setId("c9");
        when(carteraRepository.cargoDeReserva("r1")).thenReturn(Optional.of(cargo));
        when(carteraRepository.pagadoPorCargo(List.of("c9"))).thenReturn(Map.of("c9", new BigDecimal("80000")));
        assertThatThrownBy(() -> cartera.anularCobroReserva("r1")).hasMessageContaining("ya fue pagada");
    }

    @Test
    void reserva_personalSinApartamento_noSeCobra() {
        when(usuarioApartamentoRepository.findApartamentoIdsByUsuario("u-func")).thenReturn(List.of());
        cartera.cobrarReserva("r2", "u-func", "S-10", HOY.plusDays(4));
        verify(carteraRepository, never()).guardarCargo(any());
    }
}
