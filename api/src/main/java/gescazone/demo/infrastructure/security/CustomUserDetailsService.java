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
    public UserDetails loadUserByUsername(String numeroDocumento) throws UsernameNotFoundException {

        // 1. Buscar usuario por número de documento
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + numeroDocumento));

        // 2. Validar que el usuario esté activo (si tu modelo tiene ese campo)
        //    Descomenta si UsuarioModel tiene un campo 'activo' o 'habilitado':
        // if (!usuario.isActivo()) {
        //     throw new DisabledException("Usuario deshabilitado: " + numeroDocumento);
        // }

        // 3. Construir el rol con prefijo ROLE_ (requerido por Spring Security)
        String roleName = RolNombre.toAuthority(usuario.getRol().getNombreRol());

        // 4. Retornar UserDetails con toda la información necesaria
        return User.builder()
                .username(usuario.getNumeroDocumento())
                .password(usuario.getContrasena())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(roleName)))
                // accountExpired, credentialsExpired, accountLocked todos en false = cuenta válida
                .accountExpired(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}