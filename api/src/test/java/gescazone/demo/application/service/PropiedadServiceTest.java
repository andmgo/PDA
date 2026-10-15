package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.domain.repository.VehiculoRepository;
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
class PropiedadServiceTest {

    @Mock private UsuarioApartamentoRepository usuarioApartamentoRepository;
    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private ApartamentoRepository apartamentoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private ParqueaderoRepository parqueaderoRepository;

    @InjectMocks
    private PropiedadService propiedadService;

    private ApartamentoModel apartamento(String id, String numero, boolean activo) {
        ApartamentoModel a = new ApartamentoModel();
        a.setId(id);
        a.setNumero(numero);
        a.setActivo(activo);
        return a;
    }

    private UsuarioModel usuario(String id, String documento, boolean activo) {
        UsuarioModel u = new UsuarioModel();
        u.setId(id);
        u.setNumeroDocumento(documento);
        u.setNombre("Ana");
        u.setApellido("Pérez");
        u.setActivo(activo);
        return u;
    }

    // ── Propietarios ──

    @Test
    void asignarPropietario_porDocumento_vinculaAlUsuario() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(usuario("u-1", "1001", true)));

        String resultado = propiedadService.asignarPropietario("apto-1", " 1001 ");

        assertThat(resultado).contains("301");
        verify(usuarioApartamentoRepository).asignar("u-1", "apto-1");
    }

    @Test
    void asignarPropietario_porCorreo_buscaPorCorreoSiNoHayDocumento() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioRepository.findByNumeroDocumento("ana@correo.com")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario("u-1", "1001", true)));

        propiedadService.asignarPropietario("apto-1", "ana@correo.com");

        verify(usuarioApartamentoRepository).asignar("u-1", "apto-1");
    }

    @Test
    void asignarPropietario_yaVinculado_rechaza() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(usuario("u-1", "1001", true)));
        when(usuarioApartamentoRepository.existe("u-1", "apto-1")).thenReturn(true);

        assertThatThrownBy(() -> propiedadService.asignarPropietario("apto-1", "1001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya es propietario");
        verify(usuarioApartamentoRepository, never()).asignar(any(), any());
    }

    @Test
    void asignarPropietario_usuarioDesactivado_rechaza() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(usuario("u-1", "1001", false)));

        assertThatThrownBy(() -> propiedadService.asignarPropietario("apto-1", "1001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");
    }

    @Test
    void asignarPropietario_apartamentoDesactivado_rechaza() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", false)));

        assertThatThrownBy(() -> propiedadService.asignarPropietario("apto-1", "1001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");
        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void asignarPropietario_usuarioInexistente_noEncontrado() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());
        when(usuarioRepository.findByCorreo(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propiedadService.asignarPropietario("apto-1", "999"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void quitarPropietario_noVinculado_noEncontrado() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioApartamentoRepository.existe("u-1", "apto-1")).thenReturn(false);

        assertThatThrownBy(() -> propiedadService.quitarPropietario("apto-1", "u-1"))
                .isInstanceOf(NotFoundException.class);
        verify(usuarioApartamentoRepository, never()).quitar(any(), any());
    }

    // ── Vehículos ──

    @Test
    void registrarVehiculo_placaConGuion_seGuardaNormalizada() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(vehiculoRepository.findActivoByPlaca("ABC123")).thenReturn(Optional.empty());

        propiedadService.registrarVehiculo("apto-1", "abc-123", "carro", " Mazda ", "", null);

        verify(vehiculoRepository).save(argThat(v ->
                v.getPlaca().equals("ABC123")
                        && v.getTipo() == VehiculoModel.Tipo.CARRO
                        && v.getMarca().equals("Mazda")
                        && v.getColor() == null
                        && v.getIdParqueadero() == null
                        && v.isActivo()));
    }

    @Test
    void registrarVehiculo_placaYaActivaEnOtroApartamento_rechaza() {
        VehiculoModel existente = new VehiculoModel("ABC123", VehiculoModel.Tipo.CARRO, "apto-2");
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(apartamentoRepository.findById("apto-2")).thenReturn(Optional.of(apartamento("apto-2", "502", true)));
        when(vehiculoRepository.findActivoByPlaca("ABC123")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> propiedadService.registrarVehiculo("apto-1", "ABC123", "CARRO", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("502");
        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void registrarVehiculo_tipoInvalido_rechaza() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));

        assertThatThrownBy(() -> propiedadService.registrarVehiculo("apto-1", "ABC123", "BUS", null, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("CARRO o MOTO");
    }

    @Test
    void registrarVehiculo_conParqueadero_loAsocia() {
        ParqueaderoModel parqueadero = new ParqueaderoModel();
        parqueadero.setId("p-1");
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(vehiculoRepository.findActivoByPlaca("XYZ12D")).thenReturn(Optional.empty());
        when(parqueaderoRepository.findByNumero("P-07")).thenReturn(Optional.of(parqueadero));

        propiedadService.registrarVehiculo("apto-1", "xyz 12d", "MOTO", null, null, "p-07");

        verify(vehiculoRepository).save(argThat(v -> "p-1".equals(v.getIdParqueadero())));
    }

    @Test
    void desactivarVehiculo_yaInactivo_rechaza() {
        VehiculoModel vehiculo = new VehiculoModel("ABC123", VehiculoModel.Tipo.CARRO, "apto-1");
        vehiculo.desactivar();
        when(vehiculoRepository.findById("v-1")).thenReturn(Optional.of(vehiculo));

        assertThatThrownBy(() -> propiedadService.desactivarVehiculo("v-1"))
                .isInstanceOf(IllegalStateException.class);
    }

    // ── Placa (portería) ──

    @Test
    void buscarPorPlaca_registrada_devuelveApartamentoYPropietarios() {
        VehiculoModel vehiculo = new VehiculoModel("ABC123", VehiculoModel.Tipo.CARRO, "apto-1");
        vehiculo.setId("v-1");
        when(vehiculoRepository.findActivoByPlaca("ABC123")).thenReturn(Optional.of(vehiculo));
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apartamento("apto-1", "301", true)));
        when(usuarioApartamentoRepository.findUsuarioIdsByApartamento("apto-1")).thenReturn(List.of("u-1"));
        when(usuarioRepository.findById("u-1")).thenReturn(Optional.of(usuario("u-1", "1001", true)));

        PropiedadService.ResultadoPlaca resultado = propiedadService.buscarPorPlaca("abc 123");

        assertThat(resultado.numeroApartamento()).isEqualTo("301");
        assertThat(resultado.vehiculo().placa()).isEqualTo("ABC123");
        assertThat(resultado.propietarios()).extracting(PropiedadService.Propietario::numeroDocumento)
                .containsExactly("1001");
    }

    @Test
    void buscarPorPlaca_noRegistrada_noEncontrado() {
        when(vehiculoRepository.findActivoByPlaca("ZZZ999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> propiedadService.buscarPorPlaca("ZZZ-999"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("ZZZ999");
    }
}
