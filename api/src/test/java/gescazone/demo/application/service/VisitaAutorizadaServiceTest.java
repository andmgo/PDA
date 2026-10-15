package gescazone.demo.application.service;

import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.VisitaAutorizadaModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.domain.repository.VisitaAutorizadaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VisitaAutorizadaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);

    @Mock private VisitaAutorizadaRepository visitaAutorizadaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ApartamentoRepository apartamentoRepository;
    @Mock private UsuarioApartamentoRepository usuarioApartamentoRepository;

    @Spy @InjectMocks
    private VisitaAutorizadaService servicio;

    private UsuarioModel elena;

    @BeforeEach
    void preparar() {
        doReturn(HOY).when(servicio).hoy();
        elena = new UsuarioModel();
        elena.setId("u-elena");
        elena.setNombre("Elena");
        elena.setApellido("Vásquez");
        when(usuarioRepository.findByNumeroDocumento("300000001")).thenReturn(Optional.of(elena));
        when(usuarioRepository.findById("u-elena")).thenReturn(Optional.of(elena));
        ApartamentoModel apto = new ApartamentoModel();
        apto.setId("apto-401");
        apto.setNumero("401");
        apto.setActivo(true);
        when(apartamentoRepository.findById("apto-401")).thenReturn(Optional.of(apto));
        when(visitaAutorizadaRepository.save(any())).thenAnswer(inv -> {
            VisitaAutorizadaModel v = inv.getArgument(0);
            if (v.getId() == null) v.setId("aut-nueva");
            return v;
        });
    }

    private VisitaAutorizadaService.Nueva nueva(LocalDate desde, LocalDate hasta) {
        return new VisitaAutorizadaService.Nueva("apto-401", " Carla ", "Mejía", "55.512.34", "abc-123", desde, hasta, " ");
    }

    @Test
    void crear_enSuApartamento_normalizaDocumentoYPlaca_yQuedaVigente() {
        when(usuarioApartamentoRepository.existe("u-elena", "apto-401")).thenReturn(true);

        var resultado = servicio.crear("300000001", nueva(HOY, HOY.plusDays(2)));

        assertThat(resultado.estado()).isEqualTo(VisitaAutorizadaService.Estado.VIGENTE);
        assertThat(resultado.numeroApartamento()).isEqualTo("401");
        verify(visitaAutorizadaRepository).save(argThat(v ->
                "5551234".equals(v.getNumeroDocumento()) && "ABC123".equals(v.getPlaca())
                        && "Carla".equals(v.getNombreVisitante()) && v.getNota() == null
                        && "u-elena".equals(v.getIdAutorizadaPor())));
    }

    @Test
    void crear_enApartamentoAjeno_rechaza() {
        when(usuarioApartamentoRepository.existe("u-elena", "apto-401")).thenReturn(false);

        assertThatThrownBy(() -> servicio.crear("300000001", nueva(HOY, HOY)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tus apartamentos");
        verify(visitaAutorizadaRepository, never()).save(any());
    }

    @Test
    void crear_fechaPasada_rechaza() {
        when(usuarioApartamentoRepository.existe("u-elena", "apto-401")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear("300000001", nueva(HOY.minusDays(1), HOY)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fecha pasada");
    }

    @Test
    void crear_masDe30Dias_rechaza() {
        when(usuarioApartamentoRepository.existe("u-elena", "apto-401")).thenReturn(true);

        assertThatThrownBy(() -> servicio.crear("300000001", nueva(HOY, HOY.plusDays(30))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("30 días");
    }

    @Test
    void crear_sinFechaHasta_esSoloEseDia() {
        when(usuarioApartamentoRepository.existe("u-elena", "apto-401")).thenReturn(true);

        var r = servicio.crear("300000001", nueva(HOY.plusDays(3), null));

        assertThat(r.fechaHasta()).isEqualTo(HOY.plusDays(3));
        assertThat(r.estado()).isEqualTo(VisitaAutorizadaService.Estado.PROGRAMADA);
    }

    private VisitaAutorizadaModel existente(LocalDate desde, LocalDate hasta, String autor) {
        VisitaAutorizadaModel v = new VisitaAutorizadaModel();
        v.setId("aut-1");
        v.setIdApartamento("apto-401");
        v.setIdAutorizadaPor(autor);
        v.setNombreVisitante("Carla");
        v.setApellidoVisitante("Mejía");
        v.setNumeroDocumento("5551234");
        v.setFechaDesde(desde);
        v.setFechaHasta(hasta);
        return v;
    }

    @Test
    void cancelar_deOtroPropietario_rechaza() {
        when(visitaAutorizadaRepository.findById("aut-1")).thenReturn(Optional.of(existente(HOY, HOY, "u-otro")));

        assertThatThrownBy(() -> servicio.cancelar("300000001", "aut-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Solo quien creó");
    }

    @Test
    void cancelar_propia_laMarcaCancelada() {
        VisitaAutorizadaModel v = existente(HOY, HOY.plusDays(1), "u-elena");
        when(visitaAutorizadaRepository.findById("aut-1")).thenReturn(Optional.of(v));

        servicio.cancelar("300000001", "aut-1");

        assertThat(v.isCancelada()).isTrue();
        verify(visitaAutorizadaRepository).save(v);
    }

    @Test
    void vigenteParaIngreso_cancelada_rechaza() {
        VisitaAutorizadaModel v = existente(HOY, HOY, "u-elena");
        v.setCancelada(true);
        when(visitaAutorizadaRepository.findById("aut-1")).thenReturn(Optional.of(v));

        assertThatThrownBy(() -> servicio.vigenteParaIngreso("aut-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("canceló");
    }

    @Test
    void vigenteParaIngreso_deOtroDia_rechaza() {
        when(visitaAutorizadaRepository.findById("aut-1"))
                .thenReturn(Optional.of(existente(HOY.plusDays(2), HOY.plusDays(3), "u-elena")));

        assertThatThrownBy(() -> servicio.vigenteParaIngreso("aut-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está vigente");
    }

    @Test
    void vigenteParaDocumento_normalizaYFiltraPorApartamento() {
        when(visitaAutorizadaRepository.findVigentesEn(HOY)).thenReturn(List.of(existente(HOY, HOY, "u-elena")));

        assertThat(servicio.vigenteParaDocumento("5.551.234", "apto-401")).isPresent();
        assertThat(servicio.vigenteParaDocumento("5551234", "apto-otro")).isEmpty();
        assertThat(servicio.vigenteParaDocumento("999", null)).isEmpty();
    }
}
