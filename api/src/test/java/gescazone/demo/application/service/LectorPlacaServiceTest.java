package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LectorPlacaServiceTest {

    @Mock private PropiedadService propiedadService;
    @Mock private VisitaService visitaService;
    @Mock private VisitaAutorizadaService visitaAutorizadaService;
    @InjectMocks private LectorPlacaService lector;

    private VisitaService.Visita visitaDentro(String placa) {
        return new VisitaService.Visita("r1", VisitaService.Tipo.VEHICULO, "555", "Carla Mejía", "401", placa,
                "P-01", "2026-10-16 10:00:00", null, null);
    }

    private VisitaAutorizadaService.Autorizacion autorizacion(String placa) {
        return new VisitaAutorizadaService.Autorizacion("aut-1", "401", "Julián", "Ospina", "1047", placa,
                LocalDate.now(), LocalDate.now(), null, VisitaAutorizadaService.Estado.VIGENTE, 0, "Elena Vásquez");
    }

    private void sinResidente() {
        when(propiedadService.buscarPorPlaca(anyString())).thenThrow(new NotFoundException("no registrada"));
    }

    @Test
    void visitanteQueSigueDentro_tienePrioridad_yNormalizaLaLectura() {
        when(visitaService.dentro()).thenReturn(List.of(visitaDentro("HJK45F")));

        var lectura = lector.consultar("hjk-45f");

        assertThat(lectura.placa()).isEqualTo("HJK45F");
        assertThat(lectura.resultado()).isEqualTo(LectorPlacaService.Resultado.VISITANTE_DENTRO);
        assertThat(lectura.visitaDentro().id()).isEqualTo("r1");
        verifyNoInteractions(propiedadService);
    }

    @Test
    void vehiculoDeResidente() {
        var residente = new PropiedadService.ResultadoPlaca(null, "apto-401", "401", List.of());
        when(propiedadService.buscarPorPlaca("ABC123")).thenReturn(residente);

        var lectura = lector.consultar("ABC 123");

        assertThat(lectura.resultado()).isEqualTo(LectorPlacaService.Resultado.RESIDENTE);
        assertThat(lectura.residente().numeroApartamento()).isEqualTo("401");
    }

    @Test
    void placaDeUnaVisitaAutorizadaParaHoy() {
        sinResidente();
        when(visitaAutorizadaService.esperadasHoy()).thenReturn(List.of(autorizacion(null), autorizacion("XYZ987")));

        var lectura = lector.consultar("XYZ987");

        assertThat(lectura.resultado()).isEqualTo(LectorPlacaService.Resultado.AUTORIZADA);
        assertThat(lectura.autorizacion().autorizadaPor()).isEqualTo("Elena Vásquez");
    }

    @Test
    void placaDesconocida() {
        sinResidente();

        assertThat(lector.consultar("QWE456").resultado()).isEqualTo(LectorPlacaService.Resultado.DESCONOCIDA);
    }

    @Test
    void lecturaQueNoPareceUnaPlaca_rechaza() {
        assertThatThrownBy(() -> lector.consultar("AB"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa inválida");
        assertThatThrownBy(() -> lector.consultar("ABCDEFGH1"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
