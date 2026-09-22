package gescazone.demo.application.service;

import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private UsuarioModel usuarioValido() {
        UsuarioModel u = new UsuarioModel();
        u.setNumeroDocumento(" 123 ");
        u.setNombre(" Ana ");
        u.setApellido(" Gómez ");
        u.setCorreo(" ana@gescazone.com ");
        u.setContrasena("clave123");
        u.setTipoDocumento(new TipoDocumentoModel("CC"));
        u.setRol(new RolModel("PROPIETARIO"));
        return u;
    }

    // ── crear ────────────────────────────────────────────────────────────

    @Test
    void crear_contrasenaCorta_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        u.setContrasena("123");
        assertThatThrownBy(() -> usuarioService.crear(u))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos 6 caracteres");
    }

    @Test
    void crear_sinRol_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        u.setRol(null);
        assertThatThrownBy(() -> usuarioService.crear(u))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rol");
    }

    @Test
    void crear_documentoDuplicado_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.crear(u))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crear_correoDuplicado_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@gescazone.com")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.crear(u))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("correo");
    }

    @Test
    void crear_valido_cifraContrasenaYGuarda() {
        UsuarioModel u = usuarioValido();
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@gescazone.com")).thenReturn(false);
        when(passwordEncoder.encode("clave123")).thenReturn("HASH");

        String resultado = usuarioService.crear(u);

        assertThat(resultado).isEqualTo("Usuario creado exitosamente");
        assertThat(u.getContrasena()).isEqualTo("HASH");
        assertThat(u.getNumeroDocumento()).isEqualTo("123");
        verify(usuarioRepository).save(u);
    }

    // ── crearAprobado ────────────────────────────────────────────────────

    @Test
    void crearAprobado_valido_noRehasheaYGuarda() {
        UsuarioModel u = usuarioValido();
        u.setContrasena("hash-ya-generado");
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@gescazone.com")).thenReturn(false);

        String resultado = usuarioService.crearAprobado(u);

        assertThat(resultado).isEqualTo("Usuario creado exitosamente");
        assertThat(u.getContrasena()).isEqualTo("hash-ya-generado");
        verifyNoInteractions(passwordEncoder);
        verify(usuarioRepository).save(u);
    }

    @Test
    void crearAprobado_documentoDuplicado_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(true);
        assertThatThrownBy(() -> usuarioService.crearAprobado(u))
                .isInstanceOf(IllegalArgumentException.class);
        verify(usuarioRepository, never()).save(any());
    }

    // ── cambiarContrasena ────────────────────────────────────────────────

    @Test
    void cambiarContrasena_actualIncorrecta_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        u.setContrasena("HASH_ACTUAL");
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("mala", "HASH_ACTUAL")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.cambiarContrasena("123", "mala", "nueva123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("incorrecta");

        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void cambiarContrasena_nuevaCorta_lanzaExcepcion() {
        UsuarioModel u = usuarioValido();
        u.setContrasena("HASH_ACTUAL");
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("actual", "HASH_ACTUAL")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.cambiarContrasena("123", "actual", "123"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("al menos 6 caracteres");
    }

    @Test
    void cambiarContrasena_valida_cifraYGuarda() {
        UsuarioModel u = usuarioValido();
        u.setContrasena("HASH_ACTUAL");
        when(usuarioRepository.findByNumeroDocumento("123")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("actual", "HASH_ACTUAL")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("HASH_NUEVA");

        String resultado = usuarioService.cambiarContrasena("123", "actual", "nueva123");

        assertThat(resultado).isEqualTo("Contraseña actualizada exitosamente");
        assertThat(u.getContrasena()).isEqualTo("HASH_NUEVA");
        verify(usuarioRepository).save(u);
    }

    @Test
    void usuarioExiste_conDocumentoVacio_retornaFalseSinConsultarRepositorio() {
        assertThat(usuarioService.usuarioExiste("  ")).isFalse();
        verifyNoInteractions(usuarioRepository);
    }
}
