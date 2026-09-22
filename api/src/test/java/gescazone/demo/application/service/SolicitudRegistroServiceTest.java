package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.SolicitudRegistroModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.SolicitudRegistroRepository;
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
class SolicitudRegistroServiceTest {

    @Mock
    private SolicitudRegistroRepository solicitudRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private SolicitudRegistroService solicitudRegistroService;

    @Test
    void crear_documentoDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(true);

        assertThatThrownBy(() -> solicitudRegistroService.crear(
                "123", "Ana", "Pérez", "ana@correo.com", "secreta1", "Cédula de Ciudadanía"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con el documento");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crear_correoDuplicado_lanzaExcepcion() {
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        assertThatThrownBy(() -> solicitudRegistroService.crear(
                "123", "Ana", "Pérez", "ana@correo.com", "secreta1", "Cédula de Ciudadanía"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con el correo");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void crear_valida_hasheaContrasenaYGuardaPendiente() {
        when(usuarioRepository.existsByNumeroDocumento("123")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(passwordEncoder.encode("secreta1")).thenReturn("hash-bcrypt");

        String resultado = solicitudRegistroService.crear(
                "123", "Ana", "Pérez", "ana@correo.com", "secreta1", "Cédula de Ciudadanía");

        assertThat(resultado).contains("Solicitud enviada");
        verify(solicitudRepository).save(argThat(s ->
                "PENDIENTE".equals(s.getEstado()) && "hash-bcrypt".equals(s.getContrasenaHash())));
    }

    @Test
    void aprobar_solicitudNoExiste_lanzaExcepcion() {
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> solicitudRegistroService.aprobar("sol-1", "admin-1", "PROPIETARIO"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void aprobar_solicitudYaRevisada_lanzaExcepcion() {
        SolicitudRegistroModel solicitud = new SolicitudRegistroModel(
                "123", "Ana", "Pérez", "ana@correo.com", "hash", "Cédula de Ciudadanía");
        solicitud.setEstado("APROBADA");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> solicitudRegistroService.aprobar("sol-1", "admin-1", "PROPIETARIO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya fue revisada");
    }

    @Test
    void aprobar_sinRol_lanzaExcepcion() {
        assertThatThrownBy(() -> solicitudRegistroService.aprobar("sol-1", "admin-1", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("rol");
        verifyNoInteractions(solicitudRepository);
    }

    @Test
    void aprobar_pendiente_creaUsuarioConRolElegidoSinRehashearYMarcaAprobada() {
        SolicitudRegistroModel solicitud = new SolicitudRegistroModel(
                "123", "Ana", "Pérez", "ana@correo.com", "hash-ya-guardado", "Cédula de Ciudadanía");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));

        String resultado = solicitudRegistroService.aprobar("sol-1", "admin-1", "PROPIETARIO");

        assertThat(resultado).contains("aprobada");
        verify(usuarioService).crearAprobado(argThat(u ->
                "123".equals(u.getNumeroDocumento())
                        && "hash-ya-guardado".equals(u.getContrasena())
                        && "PROPIETARIO".equals(u.getRol().getNombreRol())));
        verify(passwordEncoder, never()).encode(any());
        verify(solicitudRepository).save(argThat(s ->
                "APROBADA".equals(s.getEstado()) && "admin-1".equals(s.getRevisadoPorIdUsuario())));
    }

    @Test
    void rechazar_pendiente_marcaRechazadaConMotivo() {
        SolicitudRegistroModel solicitud = new SolicitudRegistroModel(
                "123", "Ana", "Pérez", "ana@correo.com", "hash", "Cédula de Ciudadanía");
        when(solicitudRepository.findById("sol-1")).thenReturn(Optional.of(solicitud));

        String resultado = solicitudRegistroService.rechazar("sol-1", "admin-1", "datos incompletos");

        assertThat(resultado).contains("rechazada");
        verify(solicitudRepository).save(argThat(s ->
                "RECHAZADA".equals(s.getEstado()) && "datos incompletos".equals(s.getMotivoRechazo())));
    }
}
