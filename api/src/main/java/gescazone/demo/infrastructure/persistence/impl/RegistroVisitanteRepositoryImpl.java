package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.infrastructure.persistence.entity.RegistroVisitanteEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RegistroVisitanteJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ResidenteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RegistroVisitanteRepositoryImpl implements RegistroVisitanteRepository {

    @Autowired
    private RegistroVisitanteJpaRepository jpaRepository;

    @Autowired
    private ResidenteJpaRepository residenteJpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    private RegistroVisitanteModel toModel(RegistroVisitanteEntity entity) {
        RegistroVisitanteModel model = new RegistroVisitanteModel();
        model.setId(entity.getId().toString());
        model.setIdResidente(entity.getResidente().getId().toString());
        model.setIdApartamento(entity.getApartamento().getId().toString());
        model.setFechaHoraEntrada(entity.getFechaHoraEntrada());
        model.setFechaHoraSalida(entity.getFechaHoraSalida());
        return model;
    }

    private RegistroVisitanteEntity toEntity(RegistroVisitanteModel model) {
        RegistroVisitanteEntity entity = new RegistroVisitanteEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setResidente(residenteJpaRepository.getReferenceById(UUID.fromString(model.getIdResidente())));
        entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(model.getIdApartamento())));
        entity.setFechaHoraEntrada(model.getFechaHoraEntrada());
        entity.setFechaHoraSalida(model.getFechaHoraSalida());
        return entity;
    }

    @Override
    public List<RegistroVisitanteModel> findByIdResidente(String idResidente) {
        return jpaRepository.findByResidente_Id(UUID.fromString(idResidente)).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByIdApartamento(String idApartamento) {
        return jpaRepository.findByApartamento_Id(UUID.fromString(idApartamento)).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByApartamentoNumero(String numero) {
        return jpaRepository.findByApartamento_Numero(numero).stream().map(this::toModel).toList();
    }

    @Override
    public boolean existsByIdResidenteAndIdApartamento(String idResidente, String idApartamento) {
        return jpaRepository.existsByResidente_IdAndApartamento_Id(UUID.fromString(idResidente), UUID.fromString(idApartamento));
    }

    @Override
    public Optional<RegistroVisitanteModel> findByIdResidenteAndIdApartamento(String idResidente, String idApartamento) {
        return jpaRepository.findByResidente_IdAndApartamento_Id(UUID.fromString(idResidente), UUID.fromString(idApartamento))
                .map(this::toModel);
    }

    @Override
    public List<RegistroVisitanteModel> findByFechaHoraSalidaIsNull() {
        return jpaRepository.findByFechaHoraSalidaIsNull().stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByIdResidenteAndFechaHoraSalidaIsNull(String idResidente) {
        return jpaRepository.findByResidente_IdAndFechaHoraSalidaIsNull(UUID.fromString(idResidente))
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findVisitantesActivosByIdApartamento(String idApartamento) {
        return jpaRepository.findByApartamento_IdAndFechaHoraSalidaIsNull(UUID.fromString(idApartamento))
                .stream().map(this::toModel).toList();
    }

    @Override
    public void deleteByIdResidente(String idResidente) {
        jpaRepository.deleteByResidente_Id(UUID.fromString(idResidente));
    }

    @Override
    public void deleteByIdApartamento(String idApartamento) {
        jpaRepository.deleteByApartamento_Id(UUID.fromString(idApartamento));
    }

    @Override
    public long countVisitantesActivosByIdApartamento(String idApartamento) {
        return jpaRepository.countByApartamento_IdAndFechaHoraSalidaIsNull(UUID.fromString(idApartamento));
    }

    @Override
    public long countByIdResidente(String idResidente) {
        return jpaRepository.countByResidente_Id(UUID.fromString(idResidente));
    }

    @Override
    public List<RegistroVisitanteModel> findLatestRegistros() {
        return jpaRepository.findAllByOrderByFechaHoraEntradaDesc().stream().map(this::toModel).toList();
    }

    @Override
    public RegistroVisitanteModel save(RegistroVisitanteModel registro) {
        return toModel(jpaRepository.save(toEntity(registro)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<RegistroVisitanteModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(UUID.fromString(id));
    }
}
