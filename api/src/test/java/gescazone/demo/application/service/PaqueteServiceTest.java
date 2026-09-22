package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.PaqueteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.PaqueteRepository;
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
class PaqueteServiceTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @InjectMocks
    private PaqueteService paqueteService;

    @Test
    void registrar_apartamentoInexistente_lanzaExcepcion() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paqueteService.registrar("apto-1", "Juan Pérez", "123456"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe un apartamento");
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void registrar_nombreVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.registrar("apto-1", " ", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        verifyNoInteractions(apartamentoRepository, paqueteRepository);
    }

    @Test
    void registrar_cedulaVacia_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.registrar("apto-1", "Juan Pérez", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cédula");
    }

    @Test
    void registrar_valido_guardaYRetornaMensaje() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(new ApartamentoModel()));

        String resultado = paqueteService.registrar("apto-1", "Juan Pérez", "123456");

        assertThat(resultado).contains("registrado");
        verify(paqueteRepository).save(argThat(p ->
                p.getIdApartamento().equals("apto-1")
                        && p.getNombreReceptor().equals("Juan Pérez")
                        && p.getCedulaReceptor().equals("123456")
                        && !p.isEntregado()));
    }

    @Test
    void marcarEntregado_noExiste_lanzaExcepcion() {
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paqueteService.marcarEntregado("paq-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void marcarEntregado_yaEntregado_lanzaExcepcion() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        paquete.marcarEntregado();
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));

        assertThatThrownBy(() -> paqueteService.marcarEntregado("paq-1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void marcarEntregado_pendiente_loMarcaYGuarda() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));

        String resultado = paqueteService.marcarEntregado("paq-1");

        assertThat(resultado).contains("entregado");
        assertThat(paquete.isEntregado()).isTrue();
        verify(paqueteRepository).save(paquete);
    }

    @Test
    void consultarPorApartamento_idVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.consultarPorApartamento(""))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void consultarPendientes_delegaAlRepositorio() {
        paqueteService.consultarPendientes();
        verify(paqueteRepository).findByEntregadoFalse();
    }
}
