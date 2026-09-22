package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.ReservaSalonSocialRepository;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaSalonSocialServiceTest {

    @Mock
    private ReservaSalonSocialRepository reservaRepository;

    @Mock
    private SalonSocialRepository salonRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private ReservaSalonSocialService reservaSalonSocialService;

    private ReservaSalonSocialModel reservaValida(LocalDateTime fecha) {
        return new ReservaSalonSocialModel("usuario-1", "salon-1", fecha);
    }

    @Test
    void guardarReserva_conFechaPasada_lanzaExcepcion() {
        ReservaSalonSocialModel r = reservaValida(LocalDateTime.now().minusDays(1));
        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha pasada");
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void guardarReserva_salonInexistente_lanzaExcepcion() {
        ReservaSalonSocialModel r = reservaValida(LocalDateTime.now().plusDays(1));
        when(salonRepository.findById("salon-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("salón especificado no existe");
    }

    @Test
    void guardarReserva_salonYaReservadoEseDia_lanzaExcepcion() {
        ReservaSalonSocialModel r = reservaValida(LocalDateTime.now().plusDays(1));
        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(reservaRepository.existsReservaEnMismoDia(eq("salon-1"), any())).thenReturn(true);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está reservado");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void guardarReserva_nuevaYDisponible_guardaYRetornaMensajeDeCreacion() {
        ReservaSalonSocialModel r = reservaValida(LocalDateTime.now().plusDays(1));
        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(reservaRepository.existsReservaEnMismoDia(eq("salon-1"), any())).thenReturn(false);

        String resultado = reservaSalonSocialService.guardarReserva(r);

        assertThat(resultado).isEqualTo("Reserva creada exitosamente");
        verify(reservaRepository).save(r);
    }

    @Test
    void guardarReserva_actualizacionSinCambiarSalonNiFecha_noRevalidaDisponibilidad() {
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        ReservaSalonSocialModel existente = reservaValida(fecha);
        existente.setId("reserva-1");

        ReservaSalonSocialModel actualizada = reservaValida(fecha.plusHours(1));
        actualizada.setId("reserva-1");

        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(reservaRepository.findById("reserva-1")).thenReturn(Optional.of(existente));

        String resultado = reservaSalonSocialService.guardarReserva(actualizada);

        assertThat(resultado).isEqualTo("Reserva actualizada exitosamente");
        verify(reservaRepository, never()).existsReservaEnMismoDia(any(), any());
        verify(reservaRepository).save(actualizada);
    }

    @Test
    void guardarReserva_actualizacionCambiandoFecha_revalidaDisponibilidad() {
        LocalDateTime fechaOriginal = LocalDateTime.now().plusDays(1);
        ReservaSalonSocialModel existente = reservaValida(fechaOriginal);
        existente.setId("reserva-1");

        ReservaSalonSocialModel actualizada = reservaValida(fechaOriginal.plusDays(2));
        actualizada.setId("reserva-1");

        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(reservaRepository.findById("reserva-1")).thenReturn(Optional.of(existente));
        when(reservaRepository.existsReservaEnMismoDia(eq("salon-1"), any())).thenReturn(false);

        reservaSalonSocialService.guardarReserva(actualizada);

        verify(reservaRepository).existsReservaEnMismoDia(eq("salon-1"), any());
    }

    @Test
    void verificarDisponibilidad_salonInexistente_lanzaExcepcion() {
        when(salonRepository.findById("salon-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> reservaSalonSocialService.verificarDisponibilidad("salon-1", LocalDateTime.now()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void verificarDisponibilidad_sinReservaExistente_retornaTrue() {
        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        LocalDateTime fecha = LocalDateTime.now().plusDays(1);
        when(reservaRepository.existsByIdSalonAndFechaYHoraReserva("salon-1", fecha)).thenReturn(false);

        assertThat(reservaSalonSocialService.verificarDisponibilidad("salon-1", fecha)).isTrue();
    }

    @Test
    void guardarReserva_24DeDiciembre_lanzaExcepcion() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 12, 24, 15, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("24 y 31 de diciembre");
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void guardarReserva_31DeDiciembre_lanzaExcepcion() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 12, 31, 10, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("24 y 31 de diciembre");
    }

    @Test
    void guardarReserva_1DeEnero_lanzaExcepcion() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 1, 1, 10, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("1 de enero");
    }

    @Test
    void guardarReserva_fueraDelAnioActual_lanzaExcepcionSugiriendoSolicitud() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 6, 15, 10, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReserva(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("año actual")
                .hasMessageContaining("solicitud de excepción");
        verifyNoInteractions(reservaRepository);
    }

    @Test
    void guardarReservaAprobada_fueraDelAnioActual_omiteValidacionDeAnioYGuarda() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 6, 15, 10, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        when(salonRepository.findById("salon-1")).thenReturn(Optional.of(new SalonSocialModel()));
        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(new UsuarioModel()));
        when(reservaRepository.existsReservaEnMismoDia(eq("salon-1"), any())).thenReturn(false);

        String resultado = reservaSalonSocialService.guardarReservaAprobada(r);

        assertThat(resultado).isEqualTo("Reserva creada exitosamente");
        verify(reservaRepository).save(r);
    }

    @Test
    void guardarReservaAprobada_fechaBloqueada_igualLanzaExcepcion() {
        LocalDateTime fecha = LocalDateTime.of(LocalDate.now().getYear() + 1, 12, 31, 10, 0);
        ReservaSalonSocialModel r = reservaValida(fecha);

        assertThatThrownBy(() -> reservaSalonSocialService.guardarReservaAprobada(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("24 y 31 de diciembre");
        verifyNoInteractions(reservaRepository);
    }
}
