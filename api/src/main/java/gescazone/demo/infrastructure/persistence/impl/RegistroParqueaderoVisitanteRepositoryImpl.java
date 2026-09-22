package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
import gescazone.demo.infrastructure.persistence.entity.RegistroParqueaderoVisitanteEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ParqueaderoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RegistroParqueaderoVisitanteJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ResidenteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RegistroParqueaderoVisitanteRepositoryImpl implements RegistroParqueaderoVisitanteRepository {

    @Autowired
    private RegistroParqueaderoVisitanteJpaRepository jpaRepository;

    @Autowired
    private ResidenteJpaRepository residenteJpaRepository;

    @Autowired
    private ParqueaderoJpaRepository parqueaderoJpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    private RegistroParqueaderoVisitanteModel toModel(RegistroParqueaderoVisitanteEntity entity) {
        RegistroParqueaderoVisitanteModel model = new RegistroParqueaderoVisitanteModel();
        model.setId(entity.getId().toString());
        model.setIdResidente(entity.getResidente().getId().toString());
        model.setIdParqueadero(entity.getParqueadero().getId().toString());
        model.setIdApartamento(entity.getApartamento() != null ? entity.getApartamento().getId().toString() : null);
        model.setFechaHoraEntrada(entity.getFechaHoraEntrada());
        model.setFechaHoraSalida(entity.getFechaHoraSalida());
        model.setPlaca(entity.getPlaca());
        return model;
    }

    private RegistroParqueaderoVisitanteEntity toEntity(RegistroParqueaderoVisitanteModel model) {
        RegistroParqueaderoVisitanteEntity entity = new RegistroParqueaderoVisitanteEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setResidente(residenteJpaRepository.getReferenceById(UUID.fromString(model.getIdResidente())));
        entity.setParqueadero(parqueaderoJpaRepository.getReferenceById(UUID.fromString(model.getIdParqueadero())));
        if (model.getIdApartamento() != null) {
            entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(model.getIdApartamento())));
        }
        entity.setFechaHoraEntrada(model.getFechaHoraEntrada());
        entity.setFechaHoraSalida(model.getFechaHoraSalida());
        entity.setPlaca(model.getPlaca());
        return entity;
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByIdResidente(String idResidente) {
        return jpaRepository.findByResidente_Id(UUID.fromString(idResidente)).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByIdParqueadero(String idParqueadero) {
        return jpaRepository.findByParqueadero_Id(UUID.fromString(idParqueadero)).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByIdApartamento(String idApartamento) {
        return jpaRepository.findByApartamento_Id(UUID.fromString(idApartamento)).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByParqueaderoNumero(String numero) {
        return jpaRepository.findByParqueadero_Numero(numero).stream().map(this::toModel).toList();
    }

    @Override
    public boolean existsByIdResidenteAndIdParqueadero(String idResidente, String idParqueadero) {
        return jpaRepository.existsByResidente_IdAndParqueadero_Id(UUID.fromString(idResidente), UUID.fromString(idParqueadero));
    }

    @Override
    public Optional<RegistroParqueaderoVisitanteModel> findByIdResidenteAndIdParqueadero(String idResidente, String idParqueadero) {
        return jpaRepository.findByResidente_IdAndParqueadero_Id(UUID.fromString(idResidente), UUID.fromString(idParqueadero))
                .map(this::toModel);
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByFechaHoraSalidaIsNull() {
        return jpaRepository.findByFechaHoraSalidaIsNull().stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByIdResidenteAndFechaHoraSalidaIsNull(String idResidente) {
        return jpaRepository.findByResidente_IdAndFechaHoraSalidaIsNull(UUID.fromString(idResidente))
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findActivosByIdParqueadero(String idParqueadero) {
        return jpaRepository.findByParqueadero_IdAndFechaHoraSalidaIsNull(UUID.fromString(idParqueadero))
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findActivosByIdApartamento(String idApartamento) {
        return jpaRepository.findByApartamento_IdAndFechaHoraSalidaIsNull(UUID.fromString(idApartamento))
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findByPlacaContainingIgnoreCase(String placa) {
        return jpaRepository.findByPlacaContainingIgnoreCase(placa).stream().map(this::toModel).toList();
    }

    @Override
    public void deleteByIdResidente(String idResidente) {
        jpaRepository.deleteByResidente_Id(UUID.fromString(idResidente));
    }

    @Override
    public void deleteByIdParqueadero(String idParqueadero) {
        jpaRepository.deleteByParqueadero_Id(UUID.fromString(idParqueadero));
    }

    @Override
    public void deleteByIdApartamento(String idApartamento) {
        jpaRepository.deleteByApartamento_Id(UUID.fromString(idApartamento));
    }

    @Override
    public long countActivosByIdParqueadero(String idParqueadero) {
        return jpaRepository.countByParqueadero_IdAndFechaHoraSalidaIsNull(UUID.fromString(idParqueadero));
    }

    @Override
    public long countActivosByIdApartamento(String idApartamento) {
        return jpaRepository.countByApartamento_IdAndFechaHoraSalidaIsNull(UUID.fromString(idApartamento));
    }

    @Override
    public long countByIdResidente(String idResidente) {
        return jpaRepository.countByResidente_Id(UUID.fromString(idResidente));
    }

    @Override
    public List<RegistroParqueaderoVisitanteModel> findLatestRegistros() {
        return jpaRepository.findAllByOrderByFechaHoraEntradaDesc().stream().map(this::toModel).toList();
    }

    @Override
    public RegistroParqueaderoVisitanteModel save(RegistroParqueaderoVisitanteModel registro) {
        return toModel(jpaRepository.save(toEntity(registro)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<RegistroParqueaderoVisitanteModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }
}
