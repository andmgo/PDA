package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.repository.SalonSocialRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalonSocialServiceTest {

    @Mock
    private SalonSocialRepository salonSocialRepository;

    @InjectMocks
    private SalonSocialService salonSocialService;

    private SalonSocialModel salonValido() {
        SalonSocialModel s = new SalonSocialModel();
        s.setNumero(" S-01 ");
        s.setEstado(new EstadoModel("Disponible"));
        return s;
    }

    @Test
    void crear_sinNumero_lanzaExcepcion() {
        SalonSocialModel s = salonValido();
        s.setNumero(" ");
        assertThatThrownBy(() -> salonSocialService.crear(s))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void crear_conNumeroDuplicado_lanzaExcepcion() {
        SalonSocialModel s = salonValido();
        when(salonSocialRepository.existsByNumero("S-01")).thenReturn(true);
        assertThatThrownBy(() -> salonSocialService.crear(s))
                .isInstanceOf(IllegalArgumentException.class);
        verify(salonSocialRepository, never()).save(any());
    }

    @Test
    void crear_valido_guardaConNumeroRecortado() {
        SalonSocialModel s = salonValido();
        when(salonSocialRepository.existsByNumero("S-01")).thenReturn(false);

        String resultado = salonSocialService.crear(s);

        assertThat(resultado).isEqualTo("Salón social creado con éxito");
        assertThat(s.getNumero()).isEqualTo("S-01");
        verify(salonSocialRepository).save(s);
    }

    @Test
    void actualizar_idInexistente_lanzaExcepcion() {
        SalonSocialModel s = salonValido();
        s.setId("id-1");
        when(salonSocialRepository.findById("id-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> salonSocialService.actualizar(s))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setEstado_valido_actualizaYGuarda() {
        SalonSocialModel s = salonValido();
        when(salonSocialRepository.findByNumero("S-01")).thenReturn(Optional.of(s));

        salonSocialService.setEstado("S-01", "Mantenimiento");

        assertThat(s.getEstado().getNombreEstado()).isEqualTo("Mantenimiento");
        verify(salonSocialRepository).save(s);
    }

    @Test
    void eliminar_existente_eliminaYRetornaMensaje() {
        when(salonSocialRepository.existsByNumero("S-01")).thenReturn(true);
        String resultado = salonSocialService.eliminar("S-01");
        assertThat(resultado).isEqualTo("Salón social eliminado con éxito");
        verify(salonSocialRepository).deleteByNumero("S-01");
    }
}
