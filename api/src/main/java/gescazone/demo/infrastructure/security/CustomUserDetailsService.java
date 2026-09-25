package gescazone.demo.infrastructure.security;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import gescazone.demo.domain.model.RolNombre;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String identificador) throws UsernameNotFoundException {

        // 1. Buscar por número de documento y, si no coincide con ninguno,
        //    probar por correo — así el login acepta cualquiera de los dos
        //    sin que el llamador (AuthenticationManager) tenga que saber cuál es.
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(identificador)
                .or(() -> usuarioRepository.findByCorreo(identificador))
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + identificador));

        // 2. Construir el rol con prefijo ROLE_ (requerido por Spring Security)
        String roleName = RolNombre.toAuthority(usuario.getRol().getNombreRol());

        // 3. Retornar UserDetails con toda la información necesaria. disabled
        //    en true hace que DaoAuthenticationProvider (usado por /api/auth/login
        //    vía AuthenticationManager) rechace el login con DisabledException
        //    automáticamente — ver AuthController. login-google NO pasa por el
        //    AuthenticationManager, así que ese endpoint repite el chequeo a mano.
        return User.builder()
                .username(usuario.getNumeroDocumento())
                .password(usuario.getContrasena())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(!usuario.isActivo())
                .build();
    }
}