package gescazone.demo.application.service;

import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.repository.ReservaSalonSocialRepository;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Reglas de fecha de las reservas del salón social: nada en el pasado,
 * nunca el 24 y 31 de diciembre ni el 1 de enero, y solo dentro del año
 * actual (otro año va por solicitud de excepción). Se valida antes de
 * consultar la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Reglas de fecha de las reservas")
class ReservaReglasPruebas {

    @Mock private ReservaSalonSocialRepository reservaRepository;
    @Mock private SalonSocialRepository salonRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PermisoService permisoService;
    @Mock private CarteraService carteraService;
    @InjectMocks private ReservaSalonSocialService servicio;

    private static final int PROXIMO_ANIO = LocalDate.now().getYear() + 1;

    private ReservaSalonSocialModel reserva(LocalDateTime fecha) {
        return new ReservaSalonSocialModel("u1", "s1", fecha);
    }

    @Test
    @DisplayName("no se reserva en una fecha pasada")
    void fechaPasada() {
        assertThatThrownBy(() -> servicio.guardarReserva(reserva(LocalDateTime.now().minusHours(1))))
                .hasMessageContaining("fecha pasada");
        verifyNoInteractions(reservaRepository, salonRepository);
    }

    @ParameterizedTest(name = "{0}/{1} → bloqueada")
    @CsvSource({"12, 24", "12, 31", "1, 1"})
    @DisplayName("24 y 31 de diciembre y 1 de enero están bloqueados siempre")
    void fechasBloqueadas(int mes, int dia) {
        LocalDateTime fecha = LocalDate.of(PROXIMO_ANIO, mes, dia).atTime(18, 0);
        assertThatThrownBy(() -> servicio.guardarReserva(reserva(fecha)))
                .hasMessageContaining("24 y 31 de diciembre ni el 1 de enero");
        verifyNoInteractions(reservaRepository);
    }

    @Test
    @DisplayName("una fecha del año siguiente pide solicitud de excepción")
    void otroAnio() {
        LocalDateTime fecha = LocalDate.of(PROXIMO_ANIO, 6, 15).atTime(18, 0);
        assertThatThrownBy(() -> servicio.guardarReserva(reserva(fecha)))
                .hasMessageContaining("solicitud de excepción");
    }

    @Test
    @DisplayName("sin salón o sin usuario no se reserva")
    void datosObligatorios() {
        LocalDateTime manana = LocalDateTime.now().plusDays(1);
        assertThatThrownBy(() -> servicio.guardarReserva(new ReservaSalonSocialModel("u1", null, manana)))
                .hasMessageContaining("salón");
        assertThatThrownBy(() -> servicio.guardarReserva(new ReservaSalonSocialModel(null, "s1", manana)))
                .hasMessageContaining("usuario");
    }
}
