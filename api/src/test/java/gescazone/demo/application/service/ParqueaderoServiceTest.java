package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.repository.ParqueaderoRepository;
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
class ParqueaderoServiceTest {

    @Mock
    private ParqueaderoRepository parqueaderoRepository;

    @InjectMocks
    private ParqueaderoService parqueaderoService;

    private ParqueaderoModel parqueaderoValido() {
        ParqueaderoModel p = new ParqueaderoModel();
        p.setNumero(" P-01 ");
        p.setEstado(new EstadoModel("Disponible"));
        return p;
    }

    @Test
    void crear_sinEstado_lanzaExcepcion() {
        ParqueaderoModel p = parqueaderoValido();
        p.setEstado(null);
        assertThatThrownBy(() -> parqueaderoService.crear(p))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estado");
    }

    @Test
    void crear_conNumeroDuplicado_lanzaExcepcion() {
        ParqueaderoModel p = parqueaderoValido();
        when(parqueaderoRepository.existsByNumero("P-01")).thenReturn(true);
        assertThatThrownBy(() -> parqueaderoService.crear(p))
                .isInstanceOf(IllegalArgumentException.class);
        verify(parqueaderoRepository, never()).save(any());
    }

    @Test
    void crear_valido_recortaYGuarda() {
        ParqueaderoModel p = parqueaderoValido();
        when(parqueaderoRepository.existsByNumero("P-01")).thenReturn(false);

        String resultado = parqueaderoService.crear(p);

        assertThat(resultado).isEqualTo("Parqueadero creado con éxito");
        assertThat(p.getNumero()).isEqualTo("P-01");
        verify(parqueaderoRepository).save(p);
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(parqueaderoRepository.existsByNumero("P-01")).thenReturn(false);
        assertThatThrownBy(() -> parqueaderoService.eliminar("P-01"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void setEstado_parqueaderoInexistente_lanzaExcepcion() {
        when(parqueaderoRepository.findByNumero("P-01")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> parqueaderoService.setEstado("P-01", "Ocupado"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe");
    }

    @Test
    void setEstado_valido_actualizaEstadoYGuarda() {
        ParqueaderoModel p = parqueaderoValido();
        when(parqueaderoRepository.findByNumero("P-01")).thenReturn(Optional.of(p));

        String resultado = parqueaderoService.setEstado("P-01", "Ocupado");

        assertThat(resultado).isEqualTo("Estado del parqueadero actualizado con éxito");
        assertThat(p.getEstado().getNombreEstado()).isEqualTo("Ocupado");
        verify(parqueaderoRepository).save(p);
    }

    @Test
    void getEstado_parqueaderoInexistente_lanzaExcepcion() {
        when(parqueaderoRepository.findByNumero("P-01")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> parqueaderoService.getEstado("P-01"))
                .isInstanceOf(NotFoundException.class);
    }
}
