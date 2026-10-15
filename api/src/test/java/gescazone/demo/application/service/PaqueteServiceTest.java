package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.PaqueteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.PaqueteRepository;
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
class PaqueteServiceTest {

    @Mock
    private PaqueteRepository paqueteRepository;

    @Mock
    private ApartamentoRepository apartamentoRepository;

    @Mock
    private gescazone.demo.domain.repository.UsuarioRepository usuarioRepository;

    @Mock
    private gescazone.demo.domain.repository.UsuarioApartamentoRepository usuarioApartamentoRepository;

    @Mock
    private gescazone.demo.application.port.NotificadorCorreo notificadorCorreo;

    @InjectMocks
    private PaqueteService paqueteService;

    // ── Fase 7 ──

    private gescazone.demo.domain.model.UsuarioModel usuario(String id, String nombre, String apellido, String correo) {
        var u = new gescazone.demo.domain.model.UsuarioModel();
        u.setId(id);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setCorreo(correo);
        u.setActivo(true);
        return u;
    }

    @Test
    void registrar_guardaDescripcionYQuienRecibe_yAvisaALosPropietarios() {
        ApartamentoModel apto = new ApartamentoModel();
        apto.setId("apto-1");
        apto.setNumero("401");
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apto));
        when(usuarioRepository.findByNumeroDocumento("200000001"))
                .thenReturn(Optional.of(usuario("func-1", "Andrea", "Lozano", null)));
        when(usuarioApartamentoRepository.findUsuarioIdsByApartamento("apto-1")).thenReturn(java.util.List.of("u-elena"));
        when(usuarioRepository.findById("u-elena"))
                .thenReturn(Optional.of(usuario("u-elena", "Elena", "Vásquez", "elena@correo.com")));

        paqueteService.registrar("apto-1", "Elena Vásquez", "300000001", " Caja de Amazon ", "200000001");

        verify(paqueteRepository).save(argThat(p ->
                "Caja de Amazon".equals(p.getDescripcion()) && "func-1".equals(p.getIdRegistradoPor())));
        verify(notificadorCorreo).avisarPaqueteLlegado("elena@correo.com", "Elena", "401", "Caja de Amazon");
    }

    @Test
    void marcarEntregado_sinNombre_seEntregaAlDestinatario_yGuardaElFuncionario() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));
        when(usuarioRepository.findByNumeroDocumento("200000001"))
                .thenReturn(Optional.of(usuario("func-1", "Andrea", "Lozano", null)));

        String resultado = paqueteService.marcarEntregado("paq-1", " ", "200000001");

        assertThat(resultado).isEqualTo("Paquete entregado a Juan Pérez");
        assertThat(paquete.getEntregadoA()).isEqualTo("Juan Pérez");
        assertThat(paquete.getIdEntregadoPor()).isEqualTo("func-1");
    }

    @Test
    void marcarEntregado_aOtraPersona_loRegistra() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));

        paqueteService.marcarEntregado("paq-1", "María (empleada)", null);

        assertThat(paquete.getEntregadoA()).isEqualTo("María (empleada)");
    }

    @Test
    void mios_soloLosDeSusApartamentos_pendientesPrimero() {
        when(usuarioRepository.findByNumeroDocumento("300000001"))
                .thenReturn(Optional.of(usuario("u-elena", "Elena", "Vásquez", null)));
        when(usuarioApartamentoRepository.findApartamentoIdsByUsuario("u-elena")).thenReturn(java.util.List.of("apto-1"));
        PaqueteModel entregado = new PaqueteModel("apto-1", "Elena", "1");
        entregado.setId("p-entregado");
        entregado.marcarEntregado();
        PaqueteModel pendiente = new PaqueteModel("apto-1", "Elena", "1");
        pendiente.setId("p-pendiente");
        when(paqueteRepository.findByApartamentoIds(java.util.List.of("apto-1"))).thenReturn(java.util.List.of(entregado, pendiente));
        ApartamentoModel apto = new ApartamentoModel();
        apto.setNumero("401");
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apto));

        var mios = paqueteService.mios("300000001");

        assertThat(mios).extracting(PaqueteService.Paquete::id).containsExactly("p-pendiente", "p-entregado");
        assertThat(mios.get(0).numeroApartamento()).isEqualTo("401");
    }

    @Test
    void registrar_apartamentoInexistente_lanzaExcepcion() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paqueteService.registrar("apto-1", "Juan Pérez", "123456"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No existe un apartamento");
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void registrar_nombreVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.registrar("apto-1", " ", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        verifyNoInteractions(apartamentoRepository, paqueteRepository);
    }

    @Test
    void registrar_cedulaVacia_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.registrar("apto-1", "Juan Pérez", ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cédula");
    }

    @Test
    void registrar_valido_guardaYRetornaMensaje() {
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(new ApartamentoModel()));

        String resultado = paqueteService.registrar("apto-1", "Juan Pérez", "123456");

        assertThat(resultado).contains("registrado");
        verify(paqueteRepository).save(argThat(p ->
                p.getIdApartamento().equals("apto-1")
                        && p.getNombreReceptor().equals("Juan Pérez")
                        && p.getCedulaReceptor().equals("123456")
                        && !p.isEntregado()));
    }

    @Test
    void registrarPorNumeroApartamento_resuelveElIdYGuarda() {
        ApartamentoModel apto = new ApartamentoModel();
        apto.setId("apto-1");
        when(apartamentoRepository.findByNumero("301")).thenReturn(Optional.of(apto));
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apto));

        paqueteService.registrarPorNumeroApartamento(" 301 ", "Juan Pérez", "123456");

        verify(paqueteRepository).save(argThat(p -> p.getIdApartamento().equals("apto-1")));
    }

    @Test
    void registrar_apartamentoDesactivado_rechaza() {
        ApartamentoModel apto = new ApartamentoModel();
        apto.setNumero("301");
        apto.setActivo(false);
        when(apartamentoRepository.findById("apto-1")).thenReturn(Optional.of(apto));

        assertThatThrownBy(() -> paqueteService.registrar("apto-1", "Juan Pérez", "123456"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("desactivado");
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void registrarPorNumeroApartamento_inexistente_noEncontrado() {
        when(apartamentoRepository.findByNumero("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paqueteService.registrarPorNumeroApartamento("999", "Juan Pérez", "123456"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("999");
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void marcarEntregado_noExiste_lanzaExcepcion() {
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> paqueteService.marcarEntregado("paq-1"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void marcarEntregado_yaEntregado_lanzaExcepcion() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        paquete.marcarEntregado();
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));

        assertThatThrownBy(() -> paqueteService.marcarEntregado("paq-1"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void marcarEntregado_pendiente_loMarcaYGuarda() {
        PaqueteModel paquete = new PaqueteModel("apto-1", "Juan Pérez", "123456");
        when(paqueteRepository.findById("paq-1")).thenReturn(Optional.of(paquete));

        String resultado = paqueteService.marcarEntregado("paq-1");

        assertThat(resultado).contains("entregado");
        assertThat(paquete.isEntregado()).isTrue();
        verify(paqueteRepository).save(paquete);
    }

    @Test
    void consultarPorApartamento_idVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> paqueteService.consultarPorApartamento(""))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(paqueteRepository);
    }

    @Test
    void consultarPendientes_delegaAlRepositorio() {
        paqueteService.consultarPendientes();
        verify(paqueteRepository).findByEntregadoFalse();
    }
}
