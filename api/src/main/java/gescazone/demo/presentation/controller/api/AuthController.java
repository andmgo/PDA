package gescazone.demo.presentation.controller.api;

import gescazone.demo.application.service.UsuarioService;
import gescazone.demo.domain.model.RolPermisoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.RolPermisoRepository;
import gescazone.demo.infrastructure.security.CustomUserDetailsService;
import gescazone.demo.infrastructure.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Único punto de entrada de autenticación para gescazone-web (y, a futuro,
 * Flutter). Antes el login vivía enteramente dentro de Spring Security
 * (form login + sesión); ahora la Api es stateless y emite un JWT propio —
 * JwtTokenProvider.generateToken() por fin se usa (antes era código muerto,
 * hallazgo de la auditoría al preparar este bloque).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RolPermisoRepository rolPermisoRepository;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Value("${internal.api-key}")
    private String internalApiKey;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos) {
        String numeroDocumento = datos.get("numeroDocumento");
        String contrasena = datos.get("contrasena");

        if (numeroDocumento == null || numeroDocumento.isBlank()
                || contrasena == null || contrasena.isBlank()) {
            return ResponseEntity.badRequest().body("Documento y contraseña son obligatorios");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(numeroDocumento, contrasena));
            return respuestaLogin(authentication, numeroDocumento);
        } catch (DisabledException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Esta cuenta está desactivada. Contacta a un administrador.");
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
    }

    /**
     * Emite un JWT sin pedir contraseña, para el flujo "iniciar sesión con
     * Google" — gescazone-web ya verificó el correo con Google (OIDC) antes
     * de llamar aquí. Sin contraseña que validar, este endpoint solo puede
     * ser confiable si lo llama gescazone-web (nunca un navegador directo) —
     * por eso exige el header X-Internal-Key, que solo web conoce.
     */
    @PostMapping("/login-google")
    public ResponseEntity<?> loginGoogle(
            @RequestBody Map<String, String> datos,
            @RequestHeader(value = "X-Internal-Key", required = false) String claveInterna) {

        if (claveInterna == null || !claveInterna.equals(internalApiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No autorizado");
        }

        String correo = datos.get("correo");
        if (correo == null || correo.isBlank()) {
            return ResponseEntity.badRequest().body("El correo es obligatorio");
        }

        UsuarioModel usuario = usuarioService.consultarPorCorreo(correo);
        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe un usuario con ese correo");
        }

        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(usuario.getNumeroDocumento());
            if (!userDetails.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Esta cuenta está desactivada. Contacta a un administrador.");
            }
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            return respuestaLogin(authentication, usuario.getNumeroDocumento());
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No existe un usuario con ese correo");
        }
    }

    private ResponseEntity<?> respuestaLogin(Authentication authentication, String numeroDocumento) {
        String token = jwtTokenProvider.generateToken(authentication);

        UsuarioModel usuario = usuarioService.consultar(numeroDocumento);
        String nombreRol = usuario.getRol().getNombreRol();

        List<Map<String, Object>> permisos = rolPermisoRepository.matrizCompleta().stream()
                .filter(c -> c.getNombreRol().equalsIgnoreCase(nombreRol))
                .map(this::celdaAMapa)
                .toList();

        return ResponseEntity.ok(Map.of(
                "token", token,
                "numeroDocumento", usuario.getNumeroDocumento(),
                "nombre", usuario.getNombre(),
                "apellido", usuario.getApellido(),
                "correo", usuario.getCorreo(),
                "nombreRol", nombreRol,
                "permisos", permisos
        ));
    }

    private Map<String, Object> celdaAMapa(RolPermisoModel celda) {
        return Map.of(
                "codigo", celda.getCodigoPermiso(),
                "puedeVer", celda.isPuedeVer(),
                "puedeEditar", celda.isPuedeEditar()
        );
    }
}
