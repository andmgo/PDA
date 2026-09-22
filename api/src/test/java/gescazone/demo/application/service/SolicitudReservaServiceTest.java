package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.SolicitudReservaModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.SolicitudReservaRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SolicitudReservaServiceTest {

    @Mock
    private SolicitudReservaRepository solicitudRepository;

    @Mock
    private SalonSocialRepository salonRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Spy
    private ReservaSalonSocialService reservaSalonSocialService;

    @InjectMocks
    private SolicitudReservaService solicitudReservaService;

    private LocalDateTime proximoAnioMitad() {
        return LocalDateTime.of(LocalDate.now().getYear() + 1, 6, 15, 10, 0);
    }

    @Test
    void crear_fechaPasada_lanzaExcepcion() {
        assertThatThrownBy(() -> solicitudReservaService.crear(
                "usuario-1", "salon-1", LocalDateTime.now().minusDays(1), "motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha pasada");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crear_fechaBloqueada_lanzaExcepcion() {
        LocalDateTime finDeAnio = LocalDateTime.of(LocalDate.now().getYear() + 1, 12, 31, 10, 0);
        assertThatThrownBy(() -> solicitudReservaService.crear(
                "usuario-1", "salon-1", finDeAnio, "motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está disponible");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crear_fechaDentroDelAnioActual_lanzaExcepcionSugiriendoReservaDirecta() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> solicitudReservaService.crear(
                "usuario-1", "salon-1", fecha, "motivo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("reservarla directamente");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crear_usuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> solicitudReservaService.crear(
                "usuario-1", "salon-1", proximoAnioMitad(), "motivo"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe un usuario");
    }

    @Test
    void crear_valida_guardaConEstadoPendiente() {
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));

        String resultado = solicitudReservaService.crear("usuario-1", "salon-1", proximoAnioMitad(), "motivo");

        assertThat(resultado).contains("Solicitud enviada");
        verify(solicitudRepository).save(argThat(s -> "PENDIENTE".equals(s.getEstado())));
    }

    @Test
    void aprobar_solicitudNoExiste_lanzaExcepcion() {
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> solicitudReservaService.aprobar("sol-1", "admin-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void aprobar_solicitudYaRevisada_lanzaExcepcion() {
        SolicitudReservaModel solicitud = new SolicitudReservaModel("usuario-1", "salon-1", proximoAnioMitad(), "motivo");
        solicitud.setEstado("APROBADA");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudReservaService.aprobar("sol-1", "admin-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya fue revisada");
    }

    @Test
    void aprobar_pendiente_creaReservaYMarcaAprobada() {
        SolicitudReservaModel solicitud = new SolicitudReservaModel("usuario-1", "salon-1", proximoAnioMitad(), "motivo");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));
        doReturn("Reserva creada exitosamente").when(reservaSalonSocialService).guardarReservaAprobada(any());

        String resultado = solicitudReservaService.aprobar("sol-1", "admin-1");

        assertThat(resultado).contains("aprobada");
        verify(reservaSalonSocialService).guardarReservaAprobada(any());
        verify(solicitudRepository).save(argThat(s ->
                "APROBADA".equals(s.getEstado()) && "admin-1".equals(s.getRevisadoPorIdUsuario())));
    }

    @Test
    void rechazar_pendiente_marcaRechazadaConMotivo() {
        SolicitudReservaModel solicitud = new SolicitudReservaModel("usuario-1", "salon-1", proximoAnioMitad(), "motivo");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));

        String resultado = solicitudReservaService.rechazar("sol-1", "admin-1", "no disponible");

        assertThat(resultado).contains("rechazada");
        verify(solicitudRepository).save(argThat(s ->
                "RECHAZADA".equals(s.getEstado()) && "no disponible".equals(s.getMotivoRechazo())));
    }
}
