package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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

/**
 * Pagos y cartera: resumen del conjunto, validaciones de entrada y pasarela.
 * (El nombre termina en "Pruebas" para no chocar con CarteraServiceTest de
 * la API; surefire lo incluye por el patrón configurado en este proyecto.)
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("Servicio de cartera")
class CarteraServicePruebas {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 16);
    private static final BigDecimal CUOTA = new BigDecimal("350000");

    @Mock private CarteraRepository carteraRepository;
    @Mock private ApartamentoRepository apartamentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioApartamentoRepository usuarioApartamentoRepository;
    @Mock private PasarelaPagos pasarela;
    @Spy @InjectMocks private CarteraService cartera;

    @BeforeEach
    void preparar() {
        doReturn(HOY).when(cartera).hoy();
        ConfiguracionCarteraModel conf = new ConfiguracionCarteraModel();
        conf.setCuotaAdministracion(CUOTA);
        when(carteraRepository.configuracion()).thenReturn(conf);
        UsuarioModel admin = new UsuarioModel();
        admin.setId("u-admin");
        when(usuarioRepository.findByNumeroDocumento("123456780")).thenReturn(Optional.of(admin));
    }

    private ApartamentoModel apto(String id, String numero) {
        ApartamentoModel a = new ApartamentoModel();
        a.setId(id);
        a.setNumero(numero);
        a.setActivo(true);
        return a;
    }

    private CargoModel cargo(String id, String idApto, LocalDate vence) {
        CargoModel c = CargoModel.cuotaAdministracion(idApto, "2026-10", CUOTA, vence);
        c.setId(id);
        return c;
    }

    @Nested
    @DisplayName("resumen del conjunto")
    class Resumen {

        @Test
        @DisplayName("suma lo por cobrar y lo vencido, y ordena primero a los más atrasados")
        void totalesYOrden() {
            when(apartamentoRepository.findAll()).thenReturn(List.of(apto("a1", "101"), apto("a2", "102"), apto("a3", "103")));
            when(carteraRepository.cargosVigentes(null)).thenReturn(List.of(
                    cargo("c1", "a1", HOY.plusDays(10)),   // 101: debe, pero no vencido
                    cargo("c2", "a2", HOY.minusDays(5)),   // 102: vencido
                    cargo("c3", "a2", HOY.minusDays(35)),  // 102: vencido
                    cargo("c4", "a3", HOY.minusDays(5)))); // 103: pagado
            when(carteraRepository.pagadoPorCargo(anyList())).thenReturn(Map.of("c4", CUOTA));

            var r = cartera.resumen();

            assertThat(r.porCobrar()).isEqualByComparingTo("1050000");
            assertThat(r.vencido()).isEqualByComparingTo("700000");
            assertThat(r.apartamentosEnMora()).isEqualTo(1);
            assertThat(r.apartamentosAlDia()).isEqualTo(2);
            assertThat(r.apartamentos()).extracting(CarteraService.FilaCartera::numeroApartamento)
                    .containsExactly("102", "101", "103");
        }

        @Test
        @DisplayName("los apartamentos desactivados no aparecen")
        void sinInactivos() {
            ApartamentoModel inactivo = apto("a9", "999");
            inactivo.setActivo(false);
            when(apartamentoRepository.findAll()).thenReturn(List.of(apto("a1", "101"), inactivo));
            when(carteraRepository.cargosVigentes(null)).thenReturn(List.of());

            assertThat(cartera.resumen().apartamentos()).extracting(CarteraService.FilaCartera::numeroApartamento)
                    .containsExactly("101");
        }
    }

    @Nested
    @DisplayName("validaciones de entrada")
    class Validaciones {

        @ParameterizedTest(name = "mes \"{0}\" → rechazado")
        @ValueSource(strings = {"2026-13", "octubre", "2026/10", "26-10"})
        @DisplayName("generar cuotas exige un mes AAAA-MM válido")
        void mesInvalido(String mes) {
            assertThatThrownBy(() -> cartera.generarCuotas(mes)).hasMessageContaining("AAAA-MM");
        }

        @Test
        @DisplayName("sin mes, genera el del mes actual")
        void mesPorDefecto() {
            when(apartamentoRepository.findAll()).thenReturn(List.of());
            assertThat(cartera.generarCuotas(null)).contains("octubre 2026");
        }

        @ParameterizedTest(name = "medio \"{0}\" → rechazado")
        @ValueSource(strings = {"CHEQUE", "", "wompi"})
        @DisplayName("un pago manual solo admite transferencia, consignación o efectivo")
        void medioInvalido(String medio) {
            when(carteraRepository.cargo("c1")).thenReturn(Optional.of(cargo("c1", "a1", HOY)));
            when(carteraRepository.pagadoPorCargo(anyList())).thenReturn(Map.of());
            assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c1", null, medio, "x"))
                    .isInstanceOf(IllegalArgumentException.class);
            verify(carteraRepository, never()).guardarPago(any());
        }

        @Test
        @DisplayName("no se paga un cobro anulado ni uno que no existe")
        void cobroAnuladoOInexistente() {
            CargoModel anulado = cargo("c1", "a1", HOY);
            anulado.anular();
            when(carteraRepository.cargo("c1")).thenReturn(Optional.of(anulado));
            when(carteraRepository.cargo("c2")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c1", null, "EFECTIVO", null))
                    .hasMessageContaining("anulado");
            assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c2", null, "EFECTIVO", null))
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("un cobro ya pagado no admite más pagos")
        void yaPagado() {
            when(carteraRepository.cargo("c1")).thenReturn(Optional.of(cargo("c1", "a1", HOY)));
            when(carteraRepository.pagadoPorCargo(anyList())).thenReturn(Map.of("c1", CUOTA));
            assertThatThrownBy(() -> cartera.registrarPagoManual("123456780", "c1", null, "EFECTIVO", null))
                    .hasMessageContaining("ya está pagado");
        }
    }

    @Nested
    @DisplayName("pasarela de pagos")
    class Pasarela {

        @Test
        @DisplayName("una transacción aún en proceso deja el pago pendiente")
        void transaccionPendiente() {
            PagoModel pago = PagoModel.enLinea("c1", CUOTA, "u1");
            when(carteraRepository.pagoPorReferencia(pago.getReferencia())).thenReturn(Optional.of(pago));
            when(pasarela.consultar("tx-1")).thenReturn(Optional.of(
                    new PasarelaPagos.Transaccion("tx-1", pago.getReferencia(), 35_000_000L, false, false)));

            assertThat(cartera.confirmarTransaccion("tx-1")).isEqualTo("PENDIENTE");
            assertThat(pago.getEstado()).isEqualTo(PagoModel.Estado.PENDIENTE);
            verify(carteraRepository, never()).guardarPago(any());
        }

        @Test
        @DisplayName("una transacción rechazada marca el pago como rechazado (no suma al saldo)")
        void transaccionRechazada() {
            PagoModel pago = PagoModel.enLinea("c1", CUOTA, "u1");
            when(carteraRepository.pagoPorReferencia(pago.getReferencia())).thenReturn(Optional.of(pago));
            when(pasarela.consultar("tx-2")).thenReturn(Optional.of(
                    new PasarelaPagos.Transaccion("tx-2", pago.getReferencia(), 35_000_000L, false, true)));

            assertThat(cartera.confirmarTransaccion("tx-2")).isEqualTo("RECHAZADO");
        }

        @Test
        @DisplayName("una transacción que la pasarela no conoce se rechaza")
        void transaccionDesconocida() {
            when(pasarela.consultar("falsa")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> cartera.confirmarTransaccion("falsa")).isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("un evento con firma inválida no toca ningún pago")
        void eventoFalso() {
            when(pasarela.eventoAutentico(any())).thenReturn(false);
            assertThatThrownBy(() -> cartera.procesarEvento(Map.of("event", "transaction.updated")))
                    .hasMessageContaining("firma inválida");
            verify(carteraRepository, never()).guardarPago(any());
        }
    }
}
