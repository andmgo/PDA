package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.infrastructure.persistence.entity.PermisoEntity;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.RolPermisoEntity;
import gescazone.demo.infrastructure.persistence.entity.UsuarioEntity;
import gescazone.demo.infrastructure.persistence.jpa.PermisoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolPermisoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RolPermisoRepositoryImplTest {

    @Mock
    private RolJpaRepository rolJpaRepository;

    @Mock
    private PermisoJpaRepository permisoJpaRepository;

    @Mock
    private RolPermisoJpaRepository rolPermisoJpaRepository;

    @Mock
    private UsuarioJpaRepository usuarioJpaRepository;

    @InjectMocks
    private RolPermisoRepositoryImpl repositorio;

    private RolEntity rol(String nombre) {
        return new RolEntity(nombre);
    }

    private PermisoEntity permiso(String codigo) {
        PermisoEntity p = new PermisoEntity();
        p.setCodigo(codigo);
        return p;
    }

    private RolPermisoEntity celda(String nombreRol, String codigoPermiso, boolean ver, boolean editar) {
        RolPermisoEntity c = new RolPermisoEntity();
        c.setRol(rol(nombreRol));
        c.setPermiso(permiso(codigoPermiso));
        c.setPuedeVer(ver);
        c.setPuedeEditar(editar);
        return c;
    }

    @Test
    void guardarPermiso_quitaElUnicoPermisoProtegido_lanzaValidationException() {
        when(rolJpaRepository.findByNombreRol("ADMINISTRADOR")).thenReturn(Optional.of(rol("ADMINISTRADOR")));
        when(permisoJpaRepository.findByCodigo("GESTION_DATOS")).thenReturn(Optional.of(permiso("GESTION_DATOS")));
        when(rolPermisoJpaRepository.findByRol_NombreRolAndPermiso_Codigo("ADMINISTRADOR", "GESTION_DATOS"))
                .thenReturn(Optional.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));
        when(rolPermisoJpaRepository.findAll())
                .thenReturn(List.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));

        assertThatThrownBy(() -> repositorio.guardarPermiso("ADMINISTRADOR", "GESTION_DATOS", true, false))
                .isInstanceOf(ValidationException.class);

        verify(rolPermisoJpaRepository, never()).save(any());
    }

    @Test
    void guardarPermiso_otroRolTieneElPermisoProtegido_permiteElCambio() {
        when(rolJpaRepository.findByNombreRol("ADMINISTRADOR")).thenReturn(Optional.of(rol("ADMINISTRADOR")));
        when(permisoJpaRepository.findByCodigo("GESTION_DATOS")).thenReturn(Optional.of(permiso("GESTION_DATOS")));
        when(rolPermisoJpaRepository.findByRol_NombreRolAndPermiso_Codigo("ADMINISTRADOR", "GESTION_DATOS"))
                .thenReturn(Optional.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));
        when(rolPermisoJpaRepository.findAll()).thenReturn(List.of(
                celda("ADMINISTRADOR", "GESTION_DATOS", true, true),
                celda("SUPERVISOR", "GESTION_DATOS", true, true)));

        repositorio.guardarPermiso("ADMINISTRADOR", "GESTION_DATOS", true, false);

        verify(rolPermisoJpaRepository).save(any());
    }

    @Test
    void guardarPermiso_rolNoExiste_lanzaNotFoundException() {
        when(rolJpaRepository.findByNombreRol("FANTASMA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositorio.guardarPermiso("FANTASMA", "GESTION_DATOS", true, true))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void eliminarRol_rolNoExiste_lanzaNotFoundException() {
        when(rolJpaRepository.findByNombreRol("FANTASMA")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> repositorio.eliminarRol("FANTASMA"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void eliminarRol_conUsuariosAsignados_lanzaValidationException() {
        when(rolJpaRepository.findByNombreRol("SUPERVISOR")).thenReturn(Optional.of(rol("SUPERVISOR")));
        when(usuarioJpaRepository.findByRol_NombreRol("SUPERVISOR")).thenReturn(List.of(new UsuarioEntity()));

        assertThatThrownBy(() -> repositorio.eliminarRol("SUPERVISOR"))
                .isInstanceOf(ValidationException.class);

        verify(rolJpaRepository, never()).delete(any());
    }

    @Test
    void eliminarRol_esElUnicoConPermisoProtegido_lanzaValidationException() {
        when(rolJpaRepository.findByNombreRol("ADMINISTRADOR")).thenReturn(Optional.of(rol("ADMINISTRADOR")));
        when(usuarioJpaRepository.findByRol_NombreRol("ADMINISTRADOR")).thenReturn(List.of());
        when(rolPermisoJpaRepository.findByRol_NombreRolAndPermiso_Codigo("ADMINISTRADOR", "GESTION_DATOS"))
                .thenReturn(Optional.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));
        when(rolPermisoJpaRepository.findAll())
                .thenReturn(List.of(celda("ADMINISTRADOR", "GESTION_DATOS", true, true)));

        assertThatThrownBy(() -> repositorio.eliminarRol("ADMINISTRADOR"))
                .isInstanceOf(ValidationException.class);

        verify(rolJpaRepository, never()).delete(any());
    }

    @Test
    void eliminarRol_rolValidoSinUsuariosNiBloqueo_eliminaCeldasYRol() {
        RolEntity supervisor = rol("SUPERVISOR");
        when(rolJpaRepository.findByNombreRol("SUPERVISOR")).thenReturn(Optional.of(supervisor));
        when(usuarioJpaRepository.findByRol_NombreRol("SUPERVISOR")).thenReturn(List.of());
        when(rolPermisoJpaRepository.findByRol_NombreRolAndPermiso_Codigo("SUPERVISOR", "GESTION_DATOS"))
                .thenReturn(Optional.empty());
        lenient().when(rolPermisoJpaRepository.findAll()).thenReturn(List.of());
        List<RolPermisoEntity> celdasDelRol = List.of(celda("SUPERVISOR", "SALONES", true, false));
        when(rolPermisoJpaRepository.findByRol_NombreRol("SUPERVISOR")).thenReturn(celdasDelRol);

        repositorio.eliminarRol("SUPERVISOR");

        verify(rolPermisoJpaRepository).deleteAll(celdasDelRol);
        verify(rolJpaRepository).delete(supervisor);
    }
}
