package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.RegistroAccesoPiscinaModel;
import gescazone.demo.domain.repository.RegistroAccesoPiscinaRepository;
import gescazone.demo.infrastructure.persistence.entity.RegistroAccesoPiscinaEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RegistroAccesoPiscinaJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ResidenteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class RegistroAccesoPiscinaRepositoryImpl implements RegistroAccesoPiscinaRepository {

    @Autowired
    private RegistroAccesoPiscinaJpaRepository jpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    @Autowired
    private ResidenteJpaRepository residenteJpaRepository;

    private RegistroAccesoPiscinaModel toModel(RegistroAccesoPiscinaEntity entity) {
        RegistroAccesoPiscinaModel model = new RegistroAccesoPiscinaModel();
        model.setId(entity.getId().toString());
        model.setIdApartamento(entity.getApartamento().getId().toString());
        model.setIdResidente(entity.getResidente().getId().toString());
        model.setFechaHora(entity.getFechaHora());
        return model;
    }

    private RegistroAccesoPiscinaEntity toEntity(RegistroAccesoPiscinaModel model) {
        RegistroAccesoPiscinaEntity entity = new RegistroAccesoPiscinaEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(model.getIdApartamento())));
        entity.setResidente(residenteJpaRepository.getReferenceById(UUID.fromString(model.getIdResidente())));
        entity.setFechaHora(model.getFechaHora());
        return entity;
    }

    @Override
    public List<RegistroAccesoPiscinaModel> findByApartamentoNumero(String numero) {
        return jpaRepository.findByApartamento_Numero(numero)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroAccesoPiscinaModel> findByResidenteNumeroDocumento(Integer numeroDocumento) {
        return jpaRepository.findByResidente_NumeroDocumento(numeroDocumento)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroAccesoPiscinaModel> findRegistrosHoy() {
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia = LocalDate.now().atTime(23, 59, 59);
        return jpaRepository.findByFechaHoraBetween(inicioDia, finDia)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroAccesoPiscinaModel> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin) {
        return jpaRepository.findByFechaHoraBetween(inicio, fin)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroAccesoPiscinaModel> findAllOrderByFechaHoraDesc() {
        return jpaRepository.findAllByOrderByFechaHoraDesc()
                .stream().map(this::toModel).toList();
    }

    @Override
    public RegistroAccesoPiscinaModel save(RegistroAccesoPiscinaModel registro) {
        return toModel(jpaRepository.save(toEntity(registro)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<RegistroAccesoPiscinaModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public boolean existsById(String id) {
        return jpaRepository.existsById(UUID.fromString(id));
    }
}
