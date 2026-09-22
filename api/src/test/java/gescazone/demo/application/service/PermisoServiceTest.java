package gescazone.demo.application.service;

import gescazone.demo.domain.model.NivelPermiso;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermisoServiceTest {

    @Mock
    private RolPermisoRepository rolPermisoRepository;

    @InjectMocks
    private PermisoService permisoService;

    private RolPermisoModel celda(String rol, String codigo, boolean ver, boolean editar) {
        RolPermisoModel c = new RolPermisoModel();
        c.setNombreRol(rol);
        c.setCodigoPermiso(codigo);
        c.setPuedeVer(ver);
        c.setPuedeEditar(editar);
        return c;
    }

    @Test
    void tienePermiso_rolConAccesoVer_retornaTrue() {
        when(rolPermisoRepository.matrizCompleta())
                .thenReturn(List.of(celda("PROPIETARIO", "PAGOS_Y_CARTERA", true, true)));

        assertThat(permisoService.tienePermiso("PROPIETARIO", "PAGOS_Y_CARTERA", NivelPermiso.VER)).isTrue();
    }

    @Test
    void tienePermiso_soloConVerYSeExigeEditar_retornaFalse() {
        when(rolPermisoRepository.matrizCompleta())
                .thenReturn(List.of(celda("PROPIETARIO", "SALONES", true, false)));

        assertThat(permisoService.tienePermiso("PROPIETARIO", "SALONES", NivelPermiso.EDITAR)).isFalse();
    }

    @Test
    void tienePermiso_celdaNoExisteEnMatriz_retornaFalse() {
        when(rolPermisoRepository.matrizCompleta()).thenReturn(List.of());
        assertThat(permisoService.tienePermiso("VIGILANTE", "CONTROL_ACCESOS", NivelPermiso.VER)).isFalse();
    }

    @Test
    void tienePermiso_rolNulo_retornaFalseSinConsultarRepositorio() {
        assertThat(permisoService.tienePermiso(null, "GESTION_DATOS", NivelPermiso.VER)).isFalse();
        verify(rolPermisoRepository, times(0)).matrizCompleta();
    }

    @Test
    void tienePermiso_esInsensibleAMayusculasEnElRol() {
        when(rolPermisoRepository.matrizCompleta())
                .thenReturn(List.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));

        assertThat(permisoService.tienePermiso("administrador", "GESTION_DATOS", NivelPermiso.VER)).isTrue();
    }

    @Test
    void llamadasSucesivas_usanCacheYConsultanElRepositorioUnaSolaVez() {
        when(rolPermisoRepository.matrizCompleta())
                .thenReturn(List.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));

        permisoService.tienePermiso("ADMINISTRADOR", "GESTION_DATOS", NivelPermiso.VER);
        permisoService.tienePermiso("ADMINISTRADOR", "GESTION_DATOS", NivelPermiso.EDITAR);
        permisoService.tienePermiso("ADMINISTRADOR", "GESTION_DATOS", NivelPermiso.VER);

        verify(rolPermisoRepository, times(1)).matrizCompleta();
    }

    @Test
    void invalidarCache_forzaRecargaEnLaSiguienteConsulta() {
        when(rolPermisoRepository.matrizCompleta())
                .thenReturn(List.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));

        permisoService.tienePermiso("ADMINISTRADOR", "GESTION_DATOS", NivelPermiso.VER);
        permisoService.invalidarCache();
        permisoService.tienePermiso("ADMINISTRADOR", "GESTION_DATOS", NivelPermiso.VER);

        verify(rolPermisoRepository, times(2)).matrizCompleta();
    }
}
