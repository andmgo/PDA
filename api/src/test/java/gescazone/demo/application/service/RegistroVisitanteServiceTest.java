package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
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
class RegistroVisitanteServiceTest {

    @Mock
    private RegistroVisitanteRepository registroVisitanteRepository;

    @Mock
    private ResidenteRepository residenteRepository;

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @InjectMocks
    private RegistroVisitanteService registroVisitanteService;

    @Test
    void crear_residenteInexistente_lanzaExcepcion() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.empty());

        RegistroVisitanteModel registro = new RegistroVisitanteModel();
        registro.setFechaHoraEntrada("2026-01-01 10:00:00");

        assertThatThrownBy(() -> registroVisitanteService.crear(registro, "res-1", "apto-1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No se encontró el residente");
    }

    @Test
    void crear_yaExisteRegistroActivo_lanzaExcepcion() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(new ApartamentoModel()));
        when(registroVisitanteRepository.existsByIdResidenteAndIdApartamento("res-1", "apto-1")).thenReturn(true);

        RegistroVisitanteModel registro = new RegistroVisitanteModel();
        registro.setFechaHoraEntrada("2026-01-01 10:00:00");

        assertThatThrownBy(() -> registroVisitanteService.crear(registro, "res-1", "apto-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un registro");

        verify(registroVisitanteRepository, never()).save(any());
    }

    @Test
    void crear_sinFechaHoraEntrada_lanzaExcepcion() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(new ApartamentoModel()));
        when(registroVisitanteRepository.existsByIdResidenteAndIdApartamento("res-1", "apto-1")).thenReturn(false);

        RegistroVisitanteModel registro = new RegistroVisitanteModel();

        assertThatThrownBy(() -> registroVisitanteService.crear(registro, "res-1", "apto-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha y hora de entrada");
    }

    @Test
    void crear_valido_guardaConIdsAsignados() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(new ApartamentoModel()));
        when(registroVisitanteRepository.existsByIdResidenteAndIdApartamento("res-1", "apto-1")).thenReturn(false);

        RegistroVisitanteModel registro = new RegistroVisitanteModel();
        registro.setFechaHoraEntrada(" 2026-01-01 10:00:00 ");

        String resultado = registroVisitanteService.crear(registro, "res-1", "apto-1");

        assertThat(resultado).isEqualTo("Registro de visitante creado con éxito");
        assertThat(registro.getIdResidente()).isEqualTo("res-1");
        assertThat(registro.getIdApartamento()).isEqualTo("apto-1");
        assertThat(registro.getFechaHoraEntrada()).isEqualTo("2026-01-01 10:00:00");
        verify(registroVisitanteRepository).save(registro);
    }

    @Test
    void registrarSalida_registroInexistente_lanzaExcepcion() {
        when(registroVisitanteRepository.findById("reg-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> registroVisitanteService.registrarSalida("reg-1", "2026-01-01 12:00:00"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void registrarSalida_registroYaTeniaSalida_lanzaExcepcion() {
        RegistroVisitanteModel registro = new RegistroVisitanteModel("res-1", "apto-1",
                "2026-01-01 10:00:00", "2026-01-01 11:00:00");
        when(registroVisitanteRepository.findById("reg-1")).thenReturn(Optional.of(registro));

        assertThatThrownBy(() -> registroVisitanteService.registrarSalida("reg-1", "2026-01-01 12:00:00"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(registroVisitanteRepository.existsById("reg-1")).thenReturn(false);
        assertThatThrownBy(() -> registroVisitanteService.eliminar("reg-1"))
                .isInstanceOf(NotFoundException.class);
    }
}
