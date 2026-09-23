package gescazone.demo.application.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.infrastructure.mail.MailService;

@Service
public class UsuarioService {

    private static final String ALFABETO_CONTRASENA = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int LARGO_CONTRASENA_GENERADA = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MailService mailService;

    public List<UsuarioModel> consultarTodos() {
        return usuarioRepository.findAll();
    }

    public UsuarioModel consultar(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El número de documento no puede estar vacío");
        return usuarioRepository.findByNumeroDocumento(numeroDocumento.trim()).orElse(null);
    }

    public UsuarioModel consultarPorCorreo(String correo) {
        if (correo == null || correo.trim().isEmpty())
            throw new IllegalArgumentException("El correo no puede estar vacío");
        return usuarioRepository.findByCorreo(correo.trim()).orElse(null);
    }

    @Transactional
    public String crear(UsuarioModel usuario) {
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        if (usuario.getContrasena().length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
        validarDatosBasicos(usuario);

        usuario.setContrasena(passwordEncoder.encode(usuario.getContrasena()));
        guardarValidado(usuario);
        return "Usuario creado exitosamente";
    }

    /**
     * Crea un usuario cuya contraseña ya viene hasheada (p.ej. desde una SolicitudRegistro
     * aprobada) — no vuelve a hashearla para evitar un doble hash.
     */
    @Transactional
    public String crearAprobado(UsuarioModel usuario) {
        if (usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty())
            throw new IllegalArgumentException("La contraseña es obligatoria");
        validarDatosBasicos(usuario);

        guardarValidado(usuario);
        return "Usuario creado exitosamente";
    }

    private void validarDatosBasicos(UsuarioModel usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        if (usuario.getNumeroDocumento() == null || usuario.getNumeroDocumento().trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty())
            throw new IllegalArgumentException("El apellido es obligatorio");
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty())
            throw new IllegalArgumentException("El correo es obligatorio");
        if (usuario.getTipoDocumento() == null || usuario.getTipoDocumento().getNombreTipoDocumento() == null
                || usuario.getTipoDocumento().getNombreTipoDocumento().trim().isEmpty())
            throw new IllegalArgumentException("Debe seleccionar un tipo de documento");
        if (usuario.getRol() == null || usuario.getRol().getNombreRol() == null
                || usuario.getRol().getNombreRol().trim().isEmpty())
            throw new IllegalArgumentException("Debe seleccionar un rol");
        if (usuarioRepository.existsByNumeroDocumento(usuario.getNumeroDocumento().trim()))
            throw new IllegalArgumentException("Ya existe un usuario con el documento: " + usuario.getNumeroDocumento());
        if (usuarioRepository.existsByCorreo(usuario.getCorreo().trim()))
            throw new IllegalArgumentException("Ya existe un usuario con el correo: " + usuario.getCorreo());
    }

    private void guardarValidado(UsuarioModel usuario) {
        usuario.setNumeroDocumento(usuario.getNumeroDocumento().trim());
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setApellido(usuario.getApellido().trim());
        usuario.setCorreo(usuario.getCorreo().trim());
        usuario.setTipoDocumento(new TipoDocumentoModel(usuario.getTipoDocumento().getNombreTipoDocumento().trim()));
        usuario.setRol(new RolModel(usuario.getRol().getNombreRol().trim()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public String actualizar(UsuarioModel usuario) {
        if (usuario == null)
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        if (usuario.getNumeroDocumento() == null || usuario.getNumeroDocumento().trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");

        UsuarioModel existente = usuarioRepository.findByNumeroDocumento(usuario.getNumeroDocumento().trim())
            .orElseThrow(() -> new NotFoundException("No existe un usuario con el documento: " + usuario.getNumeroDocumento()));

        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (usuario.getApellido() == null || usuario.getApellido().trim().isEmpty())
            throw new IllegalArgumentException("El apellido es obligatorio");
        if (usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty())
            throw new IllegalArgumentException("El correo es obligatorio");
        if (!existente.getCorreo().equals(usuario.getCorreo().trim()))
            if (usuarioRepository.existsByCorreo(usuario.getCorreo().trim()))
                throw new IllegalArgumentException("Ya existe un usuario con el correo: " + usuario.getCorreo());

        existente.setNombre(usuario.getNombre().trim());
        existente.setApellido(usuario.getApellido().trim());
        existente.setCorreo(usuario.getCorreo().trim());

        if (usuario.getTipoDocumento() != null && usuario.getTipoDocumento().getNombreTipoDocumento() != null
                && !usuario.getTipoDocumento().getNombreTipoDocumento().trim().isEmpty())
            existente.setTipoDocumento(new TipoDocumentoModel(usuario.getTipoDocumento().getNombreTipoDocumento().trim()));

        if (usuario.getRol() != null && usuario.getRol().getNombreRol() != null
                && !usuario.getRol().getNombreRol().trim().isEmpty())
            existente.setRol(new RolModel(usuario.getRol().getNombreRol().trim()));

        usuarioRepository.save(existente);
        return "Usuario actualizado exitosamente";
    }

    @Transactional
    public String cambiarEstadoActivo(String numeroDocumento, boolean activo) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");

        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento.trim())
                .orElseThrow(() -> new NotFoundException("No existe un usuario con el documento: " + numeroDocumento));

        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        return activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
    }

    @Transactional
    public String cambiarContrasena(String numeroDocumento, String contrasenaActual, String contrasenaNueva) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");

        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento.trim())
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(contrasenaActual, usuario.getContrasena()))
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        if (contrasenaNueva == null || contrasenaNueva.trim().isEmpty())
            throw new IllegalArgumentException("La nueva contraseña no puede estar vacía");
        if (contrasenaNueva.length() < 6)
            throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");

        usuario.setContrasena(passwordEncoder.encode(contrasenaNueva));
        usuarioRepository.save(usuario);
        return "Contraseña actualizada exitosamente";
    }

    /**
     * Reseteo de contraseña por un administrador (a diferencia de
     * cambiarContrasena, no pide la contraseña actual — el permiso
     * USUARIOS-editar en SecurityConfig es la única barrera). Genera una
     * contraseña aleatoria, la guarda ya hasheada y se la envía por correo
     * a la persona; si el correo falla, no se guarda el cambio (@Transactional
     * revierte) para no dejar a alguien con una contraseña que nunca va a
     * conocer.
     */
    @Transactional
    public String resetearContrasena(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El número de documento es obligatorio");

        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento.trim())
            .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        String contrasenaNueva = generarContrasenaAleatoria();
        mailService.enviarContrasenaReseteada(usuario.getCorreo(), usuario.getNombre(), contrasenaNueva);

        usuario.setContrasena(passwordEncoder.encode(contrasenaNueva));
        usuarioRepository.save(usuario);
        return "Se generó una contraseña nueva y se envió a " + usuario.getCorreo();
    }

    private String generarContrasenaAleatoria() {
        StringBuilder sb = new StringBuilder(LARGO_CONTRASENA_GENERADA);
        for (int i = 0; i < LARGO_CONTRASENA_GENERADA; i++) {
            sb.append(ALFABETO_CONTRASENA.charAt(RANDOM.nextInt(ALFABETO_CONTRASENA.length())));
        }
        return sb.toString();
    }

    public List<UsuarioModel> consultarPorTipoDocumento(String nombreTipoDocumento) {
        if (nombreTipoDocumento == null || nombreTipoDocumento.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del tipo de documento es obligatorio");
        return usuarioRepository.findByNombreTipoDocumento(nombreTipoDocumento.trim());
    }

    public List<UsuarioModel> consultarPorCargo(String cargo) {
        if (cargo == null || cargo.trim().isEmpty())
            throw new IllegalArgumentException("El cargo es obligatorio");
        return usuarioRepository.findByNombreRol(cargo.trim());
    }

    public boolean usuarioExiste(String numeroDocumento) {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) return false;
        return usuarioRepository.existsByNumeroDocumento(numeroDocumento.trim());
    }
}