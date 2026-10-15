package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.infrastructure.persistence.entity.EstadoCuentaEntity;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoDocumentoEntity;
import gescazone.demo.infrastructure.persistence.jpa.EstadoCuentaJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoDocumentoJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogoResolverTest {

    @Mock private RolJpaRepository rolJpaRepository;
    @Mock private TipoDocumentoJpaRepository tipoDocumentoJpaRepository;
    @Mock private EstadoCuentaJpaRepository estadoCuentaJpaRepository;

    @InjectMocks
    private CatalogoResolver resolver;

    @Test
    void normalizar_ignoraMayusculasTildesYEspacios() {
        assertThat(CatalogoResolver.normalizar("  Al   Día ")).isEqualTo("al dia");
        assertThat(CatalogoResolver.normalizar("ADMINISTRADOR")).isEqualTo(CatalogoResolver.normalizar("Administrador"));
    }

    @Test
    void rol_conOtrasMayusculas_devuelveElExistenteSinCrearUnoNuevo() {
        RolEntity admin = new RolEntity("ADMINISTRADOR");
        when(rolJpaRepository.findAll()).thenReturn(List.of(admin, new RolEntity("PROPIETARIO")));

        assertThat(resolver.rol("Administrador")).isSameAs(admin);
        verify(rolJpaRepository, never()).save(any());
    }

    @Test
    void estadoCuenta_sinTilde_encuentraElOficialConTilde() {
        EstadoCuentaEntity alDia = new EstadoCuentaEntity("Al día");
        when(estadoCuentaJpaRepository.findAll()).thenReturn(List.of(alDia, new EstadoCuentaEntity("En mora")));

        assertThat(resolver.estadoCuenta("Al dia")).isSameAs(alDia);
    }

    @Test
    void tipoDocumento_nombreLargo_seTraduceAlCodigo() {
        TipoDocumentoEntity cc = new TipoDocumentoEntity("CC");
        when(tipoDocumentoJpaRepository.findAll()).thenReturn(List.of(cc, new TipoDocumentoEntity("CE")));

        assertThat(resolver.tipoDocumento("Cédula de Ciudadanía")).isSameAs(cc);
    }

    @Test
    void valorInexistente_rechazaListandoLasOpcionesYNoLoCrea() {
        when(rolJpaRepository.findAll()).thenReturn(List.of(new RolEntity("ADMINISTRADOR"), new RolEntity("PROPIETARIO")));

        assertThatThrownBy(() -> resolver.rol("Superusuario"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Superusuario")
                .hasMessageContaining("ADMINISTRADOR, PROPIETARIO");
        verify(rolJpaRepository, never()).save(any());
    }
}
