package gescazone.demo.infrastructure.security;

import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsername_usuarioExistente_construyeUserDetailsConAuthorityDelRol() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNumeroDocumento("123456");
        usuario.setContrasena("hash-bcrypt");
        usuario.setRol(new RolModel("administrador"));
        when(usuarioRepository.findByNumeroDocumento("123456")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("123456");

        assertThat(userDetails.getUsername()).isEqualTo("123456");
        assertThat(userDetails.getPassword()).isEqualTo("hash-bcrypt");
        assertThat(userDetails.getAuthorities())
                .extracting(Object::toString)
                .containsExactly("ROLE_ADMINISTRADOR");
        assertThat(userDetails.isAccountNonExpired()).isTrue();
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_usuarioDesactivado_construyeUserDetailsDisabled() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setNumeroDocumento("123456");
        usuario.setContrasena("hash-bcrypt");
        usuario.setRol(new RolModel("administrador"));
        usuario.setActivo(false);
        when(usuarioRepository.findByNumeroDocumento("123456")).thenReturn(Optional.of(usuario));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("123456");

        assertThat(userDetails.isEnabled()).isFalse();
    }

    @Test
    void loadUserByUsername_usuarioInexistente_lanzaUsernameNotFoundException() {
        when(usuarioRepository.findByNumeroDocumento("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername("999"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("999");
    }
}
