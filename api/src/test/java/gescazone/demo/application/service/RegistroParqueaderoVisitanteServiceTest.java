package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
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
class RegistroParqueaderoVisitanteServiceTest {

    @Mock
    private RegistroParqueaderoVisitanteRepository registroRepository;

    @Mock
    private ParqueaderoRepository parqueaderoRepository;

    @Mock
    private ResidenteRepository residenteRepository;

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @InjectMocks
    private RegistroParqueaderoVisitanteService registroParqueaderoVisitanteService;

    @Test
    void crear_parqueaderoInexistente_lanzaExcepcion() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(parqueaderoRepository.findById("parq-1")).thenReturn(Optional.empty());

        RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel();
        registro.setFechaHoraEntrada("2026-01-01 10:00:00");

        assertThatThrownBy(() -> registroParqueaderoVisitanteService.crear(registro, "res-1", "parq-1", null))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No se encontró el parqueadero");
    }

    @Test
    void crear_sinApartamento_noValidaApartamento() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(parqueaderoRepository.findById("parq-1")).thenReturn(Optional.of(new ParqueaderoModel()));
        when(registroRepository.existsByIdResidenteAndIdParqueadero("res-1", "parq-1")).thenReturn(false);

        RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel();
        registro.setFechaHoraEntrada("2026-01-01 10:00:00");
        registro.setPlaca(" abc123 ");

        String resultado = registroParqueaderoVisitanteService.crear(registro, "res-1", "parq-1", null);

        assertThat(resultado).isEqualTo("Registro de parqueadero creado con éxito");
        assertThat(registro.getPlaca()).isEqualTo("ABC123");
        verifyNoInteractions(apartamentoRepository);
        verify(registroRepository).save(registro);
    }

    @Test
    void crear_registroActivoDuplicado_lanzaExcepcion() {
        when(residenteRepository.findById("res-1")).thenReturn(Optional.of(new ResidenteModel()));
        when(parqueaderoRepository.findById("parq-1")).thenReturn(Optional.of(new ParqueaderoModel()));
        when(registroRepository.existsByIdResidenteAndIdParqueadero("res-1", "parq-1")).thenReturn(true);

        RegistroParqueaderoVisitanteModel registro = new RegistroParqueaderoVisitanteModel();
        registro.setFechaHoraEntrada("2026-01-01 10:00:00");

        assertThatThrownBy(() -> registroParqueaderoVisitanteService.crear(registro, "res-1", "parq-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un registro");

        verify(registroRepository, never()).save(any());
    }

    @Test
    void registrarSalida_registroInexistente_lanzaExcepcion() {
        when(registroRepository.findById("reg-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> registroParqueaderoVisitanteService.registrarSalida("reg-1", "2026-01-01 12:00:00"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorPlaca_placaVacia_lanzaExcepcion() {
        assertThatThrownBy(() -> registroParqueaderoVisitanteService.buscarPorPlaca(" "))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(registroRepository);
    }
}
