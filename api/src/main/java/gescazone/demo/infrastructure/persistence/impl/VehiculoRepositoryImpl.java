package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.repository.VehiculoRepository;
import gescazone.demo.infrastructure.persistence.entity.VehiculoEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ParqueaderoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.VehiculoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VehiculoRepositoryImpl implements VehiculoRepository {

    @Autowired
    private VehiculoJpaRepository jpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    @Autowired
    private ParqueaderoJpaRepository parqueaderoJpaRepository;

    private VehiculoModel toModel(VehiculoEntity entity) {
        VehiculoModel model = new VehiculoModel();
        model.setId(entity.getId().toString());
        model.setPlaca(entity.getPlaca());
        model.setTipo(VehiculoModel.Tipo.valueOf(entity.getTipo()));
        model.setMarca(entity.getMarca());
        model.setColor(entity.getColor());
        model.setIdApartamento(entity.getApartamento().getId().toString());
        model.setIdParqueadero(entity.getParqueadero() != null ? entity.getParqueadero().getId().toString() : null);
        model.setActivo(entity.isActivo());
        model.setFechaRegistro(entity.getFechaRegistro());
        return model;
    }

    private VehiculoEntity toEntity(VehiculoModel model) {
        VehiculoEntity entity = new VehiculoEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setPlaca(model.getPlaca());
        entity.setTipo(model.getTipo().name());
        entity.setMarca(model.getMarca());
        entity.setColor(model.getColor());
        entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(model.getIdApartamento())));
        entity.setParqueadero(model.getIdParqueadero() != null
                ? parqueaderoJpaRepository.getReferenceById(UUID.fromString(model.getIdParqueadero()))
                : null);
        entity.setActivo(model.isActivo());
        entity.setFechaRegistro(model.getFechaRegistro());
        return entity;
    }

    @Override
    public VehiculoModel save(VehiculoModel vehiculo) {
        return toModel(jpaRepository.save(toEntity(vehiculo)));
    }

    @Override
    public Optional<VehiculoModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public Optional<VehiculoModel> findActivoByPlaca(String placaNormalizada) {
        return jpaRepository.findByPlacaAndActivoTrue(placaNormalizada).map(this::toModel);
    }

    @Override
    public List<VehiculoModel> findByApartamentoId(String idApartamento) {
        return jpaRepository.findByApartamento_IdOrderByActivoDescFechaRegistroDesc(UUID.fromString(idApartamento))
                .stream().map(this::toModel).toList();
    }
}
