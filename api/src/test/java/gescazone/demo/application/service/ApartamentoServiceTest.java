package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.EstadoCuentaModel;
import gescazone.demo.domain.model.TipoOcupacionModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApartamentoServiceTest {

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @InjectMocks
    private ApartamentoService apartamentoService;

    private ApartamentoModel apartamentoValido() {
        ApartamentoModel a = new ApartamentoModel();
        a.setNumero(" 101 ");
        a.setMedidas(" 50m2 ");
        a.setTipoOcupacion(new TipoOcupacionModel("Propio"));
        a.setEstadoCuenta(new EstadoCuentaModel("Al día"));
        return a;
    }

    // ── crear ────────────────────────────────────────────────────────────

    @Test
    void crear_conApartamentoNulo_lanzaExcepcion() {
        assertThatThrownBy(() -> apartamentoService.crear(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no puede ser nulo");
    }

    @Test
    void crear_sinNumero_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setNumero("  ");
        assertThatThrownBy(() -> apartamentoService.crear(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("número");
    }

    @Test
    void crear_sinTipoOcupacion_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setTipoOcupacion(null);
        assertThatThrownBy(() -> apartamentoService.crear(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de ocupación");
    }

    @Test
    void crear_sinEstadoCuenta_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setEstadoCuenta(null);
        assertThatThrownBy(() -> apartamentoService.crear(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("estado de cuenta");
    }

    @Test
    void crear_conNumeroDuplicado_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        when(apartamentoRepository.existsByNumero("101")).thenReturn(true);

        assertThatThrownBy(() -> apartamentoService.crear(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");

        verify(apartamentoRepository, never()).save(any());
    }

    @Test
    void crear_conDatosValidos_recortaEspaciosYGuarda() {
        ApartamentoModel a = apartamentoValido();
        when(apartamentoRepository.existsByNumero("101")).thenReturn(false);

        String resultado = apartamentoService.crear(a);

        assertThat(resultado).isEqualTo("Apartamento creado con éxito");
        assertThat(a.getNumero()).isEqualTo("101");
        assertThat(a.getMedidas()).isEqualTo("50m2");
        verify(apartamentoRepository).save(a);
    }

    // ── consultar ────────────────────────────────────────────────────────

    @Test
    void consultar_conNumeroVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> apartamentoService.consultar(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void consultar_noEncontrado_retornaNull() {
        when(apartamentoRepository.findByNumero("999")).thenReturn(Optional.empty());
        assertThat(apartamentoService.consultar("999")).isNull();
    }

    @Test
    void consultar_encontrado_retornaModelo() {
        ApartamentoModel a = apartamentoValido();
        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.of(a));
        assertThat(apartamentoService.consultar("101")).isEqualTo(a);
    }

    // ── actualizar ───────────────────────────────────────────────────────

    @Test
    void actualizar_sinId_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setId(null);
        assertThatThrownBy(() -> apartamentoService.actualizar(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID");
    }

    @Test
    void actualizar_idInexistente_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setId("id-1");
        when(apartamentoRepository.findById("id-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apartamentoService.actualizar(a))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe");
    }

    @Test
    void actualizar_numeroYaUsadoPorOtroApartamento_lanzaExcepcion() {
        ApartamentoModel a = apartamentoValido();
        a.setId("id-1");
        when(apartamentoRepository.findById("id-1")).thenReturn(Optional.of(a));

        ApartamentoModel otro = apartamentoValido();
        otro.setId("id-2");
        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> apartamentoService.actualizar(a))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe otro");
    }

    @Test
    void actualizar_conMismoNumeroPropio_permiteGuardar() {
        ApartamentoModel a = apartamentoValido();
        a.setId("id-1");
        when(apartamentoRepository.findById("id-1")).thenReturn(Optional.of(a));
        when(apartamentoRepository.findByNumero("101")).thenReturn(Optional.of(a));

        String resultado = apartamentoService.actualizar(a);

        assertThat(resultado).isEqualTo("Apartamento actualizado con éxito");
        verify(apartamentoRepository).save(a);
    }

    // ── eliminar ─────────────────────────────────────────────────────────

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(apartamentoRepository.existsByNumero("101")).thenReturn(false);
        assertThatThrownBy(() -> apartamentoService.eliminar("101"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe");
        verify(apartamentoRepository, never()).deleteByNumero(any());
    }

    @Test
    void eliminar_existente_eliminaYRetornaMensaje() {
        when(apartamentoRepository.existsByNumero("101")).thenReturn(true);
        String resultado = apartamentoService.eliminar("101");
        assertThat(resultado).isEqualTo("Apartamento eliminado con éxito");
        verify(apartamentoRepository).deleteByNumero("101");
    }

    // ── consultas por catálogo ───────────────────────────────────────────

    @Test
    void consultarPorTipoOcupacion_delegaAlRepositorio() {
        List<ApartamentoModel> esperado = List.of(apartamentoValido());
        when(apartamentoRepository.findByNombreTipoOcupacion("Propio")).thenReturn(esperado);
        assertThat(apartamentoService.consultarPorTipoOcupacion("Propio")).isEqualTo(esperado);
    }
}
