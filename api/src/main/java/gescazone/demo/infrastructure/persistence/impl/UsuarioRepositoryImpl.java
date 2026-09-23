package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.RolModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoDocumentoEntity;
import gescazone.demo.infrastructure.persistence.entity.UsuarioEntity;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoDocumentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UsuarioRepositoryImpl implements UsuarioRepository {

    @Autowired
    private UsuarioJpaRepository jpaRepository;

    @Autowired
    private RolJpaRepository rolJpaRepository;

    @Autowired
    private TipoDocumentoJpaRepository tipoDocumentoJpaRepository;

    private UsuarioModel toModel(UsuarioEntity entity) {
        UsuarioModel model = new UsuarioModel();
        model.setId(entity.getId().toString());
        model.setNumeroDocumento(entity.getNumeroDocumento());
        model.setNombre(entity.getNombre());
        model.setApellido(entity.getApellido());
        model.setCorreo(entity.getCorreo());
        model.setContrasena(entity.getContrasena());
        model.setActivo(entity.isActivo());

        if (entity.getRol() != null) {
            RolModel rol = new RolModel();
            rol.setNombreRol(entity.getRol().getNombreRol());
            model.setRol(rol);
        }

        if (entity.getTipoDocumento() != null) {
            TipoDocumentoModel td = new TipoDocumentoModel();
            td.setNombreTipoDocumento(entity.getTipoDocumento().getNombreTipoDocumento());
            model.setTipoDocumento(td);
        }

        return model;
    }

    private UsuarioEntity toEntity(UsuarioModel model) {
        UsuarioEntity entity = new UsuarioEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setNumeroDocumento(model.getNumeroDocumento());
        entity.setNombre(model.getNombre());
        entity.setApellido(model.getApellido());
        entity.setCorreo(model.getCorreo());
        entity.setContrasena(model.getContrasena());
        entity.setActivo(model.isActivo());

        if (model.getRol() != null) {
            String nombre = model.getRol().getNombreRol();
            RolEntity rol = rolJpaRepository.findByNombreRol(nombre)
                    .orElseGet(() -> rolJpaRepository.save(new RolEntity(nombre)));
            entity.setRol(rol);
        }

        if (model.getTipoDocumento() != null) {
            String nombre = model.getTipoDocumento().getNombreTipoDocumento();
            TipoDocumentoEntity td = tipoDocumentoJpaRepository.findByNombreTipoDocumento(nombre)
                    .orElseGet(() -> tipoDocumentoJpaRepository.save(new TipoDocumentoEntity(nombre)));
            entity.setTipoDocumento(td);
        }

        return entity;
    }

    @Override
    public Optional<UsuarioModel> findByNumeroDocumento(String numeroDocumento) {
        return jpaRepository.findByNumeroDocumento(numeroDocumento).map(this::toModel);
    }

    @Override
    public boolean existsByNumeroDocumento(String numeroDocumento) {
        return jpaRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public List<UsuarioModel> findByNombreRol(String nombreRol) {
        return jpaRepository.findByRol_NombreRol(nombreRol)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<UsuarioModel> findByNombreTipoDocumento(String nombreTipoDocumento) {
        return jpaRepository.findByTipoDocumento_NombreTipoDocumento(nombreTipoDocumento)
                .stream().map(this::toModel).toList();
    }

    @Override
    public Optional<UsuarioModel> findByCorreo(String correo) {
        return jpaRepository.findByCorreo(correo).map(this::toModel);
    }

    @Override
    public boolean existsByCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public UsuarioModel save(UsuarioModel usuario) {
        return toModel(jpaRepository.save(toEntity(usuario)));
    }

    @Override
    public Optional<UsuarioModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<UsuarioModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
