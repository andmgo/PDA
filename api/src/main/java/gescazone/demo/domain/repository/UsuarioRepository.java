package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.UsuarioModel;
import java.util.List;
import java.util.Optional;

public interface UsuarioRepository {
    Optional<UsuarioModel> findByNumeroDocumento(String numeroDocumento);
    boolean existsByNumeroDocumento(String numeroDocumento);
    List<UsuarioModel> findByNombreRol(String nombreRol);
    List<UsuarioModel> findByNombreTipoDocumento(String nombreTipoDocumento);
    Optional<UsuarioModel> findByCorreo(String correo);
    boolean existsByCorreo(String correo);
    UsuarioModel save(UsuarioModel usuario);
    Optional<UsuarioModel> findById(String id);
    List<UsuarioModel> findAll();
}