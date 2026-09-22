package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.SolicitudRegistroModel;
import gescazone.demo.domain.repository.SolicitudRegistroRepository;
import gescazone.demo.infrastructure.persistence.entity.SolicitudRegistroEntity;
import gescazone.demo.infrastructure.persistence.jpa.SolicitudRegistroJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SolicitudRegistroRepositoryImpl implements SolicitudRegistroRepository {

    @Autowired
    private SolicitudRegistroJpaRepository jpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    private SolicitudRegistroModel toModel(SolicitudRegistroEntity entity) {
        SolicitudRegistroModel model = new SolicitudRegistroModel();
        model.setId(entity.getId().toString());
        model.setNumeroDocumento(entity.getNumeroDocumento());
        model.setNombre(entity.getNombre());
        model.setApellido(entity.getApellido());
        model.setCorreo(entity.getCorreo());
        model.setContrasenaHash(entity.getContrasenaHash());
        model.setNombreTipoDocumento(entity.getNombreTipoDocumento());
        model.setEstado(entity.getEstado());
        model.setRevisadoPorIdUsuario(entity.getRevisadoPor() != null ? entity.getRevisadoPor().getId().toString() : null);
        model.setMotivoRechazo(entity.getMotivoRechazo());
        model.setFechaCreacion(entity.getFechaCreacion());
        model.setFechaRevision(entity.getFechaRevision());
        return model;
    }

    private SolicitudRegistroEntity toEntity(SolicitudRegistroModel model) {
        SolicitudRegistroEntity entity = new SolicitudRegistroEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setNumeroDocumento(model.getNumeroDocumento());
        entity.setNombre(model.getNombre());
        entity.setApellido(model.getApellido());
        entity.setCorreo(model.getCorreo());
        entity.setContrasenaHash(model.getContrasenaHash());
        entity.setNombreTipoDocumento(model.getNombreTipoDocumento());
        entity.setEstado(model.getEstado());
        if (model.getRevisadoPorIdUsuario() != null) {
            entity.setRevisadoPor(usuarioJpaRepository.getReferenceById(UUID.fromString(model.getRevisadoPorIdUsuario())));
        }
        entity.setMotivoRechazo(model.getMotivoRechazo());
        entity.setFechaCreacion(model.getFechaCreacion() != null ? model.getFechaCreacion() : LocalDateTime.now());
        entity.setFechaRevision(model.getFechaRevision());
        return entity;
    }

    @Override
    public SolicitudRegistroModel save(SolicitudRegistroModel solicitud) {
        return toModel(jpaRepository.save(toEntity(solicitud)));
    }

    @Override
    public Optional<SolicitudRegistroModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<SolicitudRegistroModel> findByEstado(String estado) {
        return jpaRepository.findByEstado(estado).stream().map(this::toModel).toList();
    }

    @Override
    public List<SolicitudRegistroModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
