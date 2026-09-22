package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.infrastructure.persistence.entity.EstadoEntity;
import gescazone.demo.infrastructure.persistence.entity.ParqueaderoEntity;
import gescazone.demo.infrastructure.persistence.jpa.EstadoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ParqueaderoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ParqueaderoRepositoryImpl implements ParqueaderoRepository {

    @Autowired
    private ParqueaderoJpaRepository jpaRepository;

    @Autowired
    private EstadoJpaRepository estadoJpaRepository;

    private ParqueaderoModel toModel(ParqueaderoEntity entity) {
        ParqueaderoModel model = new ParqueaderoModel();
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

    private ParqueaderoEntity toEntity(ParqueaderoModel model) {
        ParqueaderoEntity entity = new ParqueaderoEntity();
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
    public Optional<ParqueaderoModel> findByNumero(String numero) {
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
    public List<ParqueaderoModel> findByNombreEstado(String nombreEstado) {
        return jpaRepository.findByEstado_NombreEstado(nombreEstado)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ParqueaderoModel> findByMedidas(String medidas) {
        return jpaRepository.findByMedidas(medidas)
                .stream().map(this::toModel).toList();
    }

    @Override
    public ParqueaderoModel save(ParqueaderoModel parqueadero) {
        return toModel(jpaRepository.save(toEntity(parqueadero)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<ParqueaderoModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<ParqueaderoModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
