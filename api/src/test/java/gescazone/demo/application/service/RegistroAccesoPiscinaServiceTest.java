package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.RegistroAccesoPiscinaRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
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
class RegistroAccesoPiscinaServiceTest {

    @Mock
    private RegistroAccesoPiscinaRepository registroRepository;

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @Mock
    private ResidenteRepository residenteRepository;

    @InjectMocks
    private RegistroAccesoPiscinaService registroAccesoPiscinaService;

    @Test
    void registrarIngreso_apartamentoInexistente_lanzaExcepcion() {
        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registroAccesoPiscinaService.registrarIngreso("101", 123))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe el apartamento");

        verifyNoInteractions(residenteRepository);
        verify(registroRepository, never()).save(any());
    }

    @Test
    void registrarIngreso_residenteInexistente_lanzaExcepcion() {
        ApartamentoModel apto = new ApartamentoModel();
        apto.setId("apto-1");
        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.of(apto));
        when(residenteRepository.findByNumeroDocumento(123)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registroAccesoPiscinaService.registrarIngreso("101", 123))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe el residente");
    }

    @Test
    void registrarIngreso_valido_guardaRegistroYRetornaMensaje() {
        ApartamentoModel apto = new ApartamentoModel();
        apto.setId("apto-1");
        ResidenteModel residente = new ResidenteModel();
        residente.setId("res-1");

        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.of(apto));
        when(residenteRepository.findByNumeroDocumento(123)).thenReturn(Optional.of(residente));

        String resultado = registroAccesoPiscinaService.registrarIngreso("101", 123);

        assertThat(resultado).contains("101");
        verify(registroRepository).save(argThat(r ->
                r.getIdApartamento().equals("apto-1") && r.getIdResidente().equals("res-1")));
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(registroRepository.existsById("reg-1")).thenReturn(false);
        assertThatThrownBy(() -> registroAccesoPiscinaService.eliminar("reg-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void verificarAcceso_apartamentoInexistente_lanzaExcepcion() {
        when(apartamentoRepository.existsByNumero("101")).thenReturn(false);
        assertThatThrownBy(() -> registroAccesoPiscinaService.verificarAcceso("101"))
                .isInstanceOf(NotFoundException.class);
    }
}
