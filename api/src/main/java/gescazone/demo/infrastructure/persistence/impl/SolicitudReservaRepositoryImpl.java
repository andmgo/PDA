package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.SolicitudReservaModel;
import gescazone.demo.domain.repository.SolicitudReservaRepository;
import gescazone.demo.infrastructure.persistence.entity.SolicitudReservaEntity;
import gescazone.demo.infrastructure.persistence.jpa.SalonSocialJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.SolicitudReservaJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SolicitudReservaRepositoryImpl implements SolicitudReservaRepository {

    @Autowired
    private SolicitudReservaJpaRepository jpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private SalonSocialJpaRepository salonSocialJpaRepository;

    private SolicitudReservaModel toModel(SolicitudReservaEntity entity) {
        SolicitudReservaModel model = new SolicitudReservaModel();
        model.setId(entity.getId().toString());
        model.setIdUsuario(entity.getUsuario().getId().toString());
        model.setIdSalon(entity.getSalon().getId().toString());
        model.setFechaSolicitada(entity.getFechaSolicitada());
        model.setJustificacion(entity.getJustificacion());
        model.setEstado(entity.getEstado());
        model.setRevisadoPorIdUsuario(entity.getRevisadoPor() != null ? entity.getRevisadoPor().getId().toString() : null);
        model.setMotivoRechazo(entity.getMotivoRechazo());
        model.setFechaCreacion(entity.getFechaCreacion());
        model.setFechaRevision(entity.getFechaRevision());
        return model;
    }

    private SolicitudReservaEntity toEntity(SolicitudReservaModel model) {
        SolicitudReservaEntity entity = new SolicitudReservaEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setUsuario(usuarioJpaRepository.getReferenceById(UUID.fromString(model.getIdUsuario())));
        entity.setSalon(salonSocialJpaRepository.getReferenceById(UUID.fromString(model.getIdSalon())));
        entity.setFechaSolicitada(model.getFechaSolicitada());
        entity.setJustificacion(model.getJustificacion());
        entity.setEstado(model.getEstado());
        if (model.getRevisadoPorIdUsuario() != null) {
            entity.setRevisadoPor(usuarioJpaRepository.getReferenceById(UUID.fromString(model.getRevisadoPorIdUsuario())));
        }
        entity.setMotivoRechazo(model.getMotivoRechazo());
        entity.setFechaCreacion(model.getFechaCreacion() != null ? model.getFechaCreacion() : java.time.LocalDateTime.now());
        entity.setFechaRevision(model.getFechaRevision());
        return entity;
    }

    @Override
    public SolicitudReservaModel save(SolicitudReservaModel solicitud) {
        return toModel(jpaRepository.save(toEntity(solicitud)));
    }

    @Override
    public Optional<SolicitudReservaModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<SolicitudReservaModel> findByEstado(String estado) {
        return jpaRepository.findByEstado(estado).stream().map(this::toModel).toList();
    }

    @Override
    public List<SolicitudReservaModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
