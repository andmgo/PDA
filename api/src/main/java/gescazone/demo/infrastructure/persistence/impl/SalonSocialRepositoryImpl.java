package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.repository.SalonSocialRepository;
import gescazone.demo.infrastructure.persistence.entity.EstadoEntity;
import gescazone.demo.infrastructure.persistence.entity.SalonSocialEntity;
import gescazone.demo.infrastructure.persistence.jpa.EstadoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.SalonSocialJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SalonSocialRepositoryImpl implements SalonSocialRepository {

    @Autowired
    private SalonSocialJpaRepository jpaRepository;

    @Autowired
    private EstadoJpaRepository estadoJpaRepository;

    private SalonSocialModel toModel(SalonSocialEntity entity) {
        SalonSocialModel model = new SalonSocialModel();
        model.setId(entity.getId().toString());
        model.setNumero(entity.getNumero());
        model.setMedidas(entity.getMedidas());
        model.setTelefono(entity.getTelefono());

        if (entity.getEstado() != null) {
            EstadoModel estado = new EstadoModel();
            estado.setNombreEstado(entity.getEstado().getNombreEstado());
            model.setEstado(estado);
        }

        return model;
    }

    private SalonSocialEntity toEntity(SalonSocialModel model) {
        SalonSocialEntity entity = new SalonSocialEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setNumero(model.getNumero());
        entity.setMedidas(model.getMedidas());
        entity.setTelefono(model.getTelefono());

        if (model.getEstado() != null) {
            String nombre = model.getEstado().getNombreEstado();
            EstadoEntity estado = estadoJpaRepository.findByNombreEstado(nombre)
                    .orElseGet(() -> estadoJpaRepository.save(new EstadoEntity(nombre)));
            entity.setEstado(estado);
        }

        return entity;
    }

    @Override
    public Optional<SalonSocialModel> findByNumero(String numero) {
        return jpaRepository.findByNumero(numero).map(this::toModel);
    }

    @Override
    public boolean existsByNumero(String numero) {
        return jpaRepository.existsByNumero(numero);
    }

    @Override
    public void deleteByNumero(String numero) {
        jpaRepository.deleteByNumero(numero);
    }

    @Override
    public List<SalonSocialModel> findByNombreEstado(String nombreEstado) {
        return jpaRepository.findByEstado_NombreEstado(nombreEstado)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<SalonSocialModel> findByMedidas(String medidas) {
        return jpaRepository.findByMedidas(medidas)
                .stream().map(this::toModel).toList();
    }

    @Override
    public SalonSocialModel save(SalonSocialModel salon) {
        return toModel(jpaRepository.save(toEntity(salon)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<SalonSocialModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<SalonSocialModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
