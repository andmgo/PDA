package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.PaqueteModel;
import gescazone.demo.domain.repository.PaqueteRepository;
import gescazone.demo.infrastructure.persistence.entity.PaqueteEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.PaqueteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PaqueteRepositoryImpl implements PaqueteRepository {

    @Autowired
    private PaqueteJpaRepository jpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    private PaqueteModel toModel(PaqueteEntity entity) {
        PaqueteModel model = new PaqueteModel();
        model.setId(entity.getId().toString());
        model.setIdApartamento(entity.getApartamento().getId().toString());
        model.setNombreReceptor(entity.getNombreReceptor());
        model.setCedulaReceptor(entity.getCedulaReceptor());
        model.setFechaHoraLlegada(entity.getFechaHoraLlegada());
        model.setEntregado(entity.isEntregado());
        model.setFechaHoraEntrega(entity.getFechaHoraEntrega());
        return model;
    }

    private PaqueteEntity toEntity(PaqueteModel model) {
        PaqueteEntity entity = new PaqueteEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(model.getIdApartamento())));
        entity.setNombreReceptor(model.getNombreReceptor());
        entity.setCedulaReceptor(model.getCedulaReceptor());
        entity.setFechaHoraLlegada(model.getFechaHoraLlegada());
        entity.setEntregado(model.isEntregado());
        entity.setFechaHoraEntrega(model.getFechaHoraEntrega());
        return entity;
    }

    @Override
    public PaqueteModel save(PaqueteModel paquete) {
        return toModel(jpaRepository.save(toEntity(paquete)));
    }

    @Override
    public Optional<PaqueteModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<PaqueteModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }

    @Override
    public List<PaqueteModel> findByEntregadoFalse() {
        return jpaRepository.findByEntregadoFalse().stream().map(this::toModel).toList();
    }

    @Override
    public List<PaqueteModel> findByApartamentoId(String idApartamento) {
        return jpaRepository.findByApartamento_Id(UUID.fromString(idApartamento)).stream().map(this::toModel).toList();
    }
}
