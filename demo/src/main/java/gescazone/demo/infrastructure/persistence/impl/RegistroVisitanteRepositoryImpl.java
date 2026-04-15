package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.infrastructure.persistence.document.RegistroVisitanteDocument;
import gescazone.demo.infrastructure.persistence.mongo.RegistroVisitanteMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RegistroVisitanteRepositoryImpl implements RegistroVisitanteRepository {

    @Autowired
    private RegistroVisitanteMongoRepository mongoRepository;

    private RegistroVisitanteModel toModel(RegistroVisitanteDocument doc) {
        RegistroVisitanteModel model = new RegistroVisitanteModel();
        model.setId(doc.getId());
        model.setIdResidente(doc.getIdResidente());
        model.setIdApartamento(doc.getIdApartamento());
        model.setFechaHoraEntrada(doc.getFechaHoraEntrada());
        model.setFechaHoraSalida(doc.getFechaHoraSalida());
        return model;
    }

    private RegistroVisitanteDocument toDocument(RegistroVisitanteModel model) {
        RegistroVisitanteDocument doc = new RegistroVisitanteDocument();
        doc.setId(model.getId());
        doc.setIdResidente(model.getIdResidente());
        doc.setIdApartamento(model.getIdApartamento());
        doc.setFechaHoraEntrada(model.getFechaHoraEntrada());
        doc.setFechaHoraSalida(model.getFechaHoraSalida());
        return doc;
    }

    @Override
    public List<RegistroVisitanteModel> findByIdResidente(String idResidente) {
        return mongoRepository.findByIdResidente(idResidente).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByIdApartamento(String idApartamento) {
        return mongoRepository.findByIdApartamento(idApartamento).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByApartamentoNumero(String numero) {
        return mongoRepository.findAll().stream()
                .map(this::toModel).toList();
    }

    @Override
    public boolean existsByIdResidenteAndIdApartamento(String idResidente, String idApartamento) {
        return mongoRepository.existsByIdResidenteAndIdApartamento(idResidente, idApartamento);
    }

    @Override
    public Optional<RegistroVisitanteModel> findByIdResidenteAndIdApartamento(String idResidente, String idApartamento) {
        return mongoRepository.findByIdResidenteAndIdApartamento(idResidente, idApartamento).map(this::toModel);
    }

    @Override
    public List<RegistroVisitanteModel> findByFechaHoraSalidaIsNull() {
        return mongoRepository.findByFechaHoraSalidaIsNull().stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findByIdResidenteAndFechaHoraSalidaIsNull(String idResidente) {
        return mongoRepository.findByIdResidenteAndFechaHoraSalidaIsNull(idResidente).stream().map(this::toModel).toList();
    }

    @Override
    public List<RegistroVisitanteModel> findVisitantesActivosByIdApartamento(String idApartamento) {
        return mongoRepository.findByIdApartamentoAndFechaHoraSalidaIsNull(idApartamento).stream().map(this::toModel).toList();
    }

    @Override
    public void deleteByIdResidente(String idResidente) {
        mongoRepository.findByIdResidente(idResidente)
                .forEach(doc -> mongoRepository.deleteById(doc.getId()));
    }

    @Override
    public void deleteByIdApartamento(String idApartamento) {
        mongoRepository.findByIdApartamento(idApartamento)
                .forEach(doc -> mongoRepository.deleteById(doc.getId()));
    }

    @Override
    public long countVisitantesActivosByIdApartamento(String idApartamento) {
        return mongoRepository.countByIdApartamentoAndFechaHoraSalidaIsNull(idApartamento);
    }

    @Override
    public long countByIdResidente(String idResidente) {
        return mongoRepository.countByIdResidente(idResidente);
    }

    @Override
    public List<RegistroVisitanteModel> findLatestRegistros() {
        return mongoRepository.findLatestRegistros().stream().map(this::toModel).toList();
    }

    @Override
    public RegistroVisitanteModel save(RegistroVisitanteModel registro) {
        return toModel(mongoRepository.save(toDocument(registro)));
    }

    @Override
    public void deleteById(String id) {
        mongoRepository.deleteById(id);
    }

    @Override
    public Optional<RegistroVisitanteModel> findById(String id) {
        return mongoRepository.findById(id).map(this::toModel);
    }

    @Override
    public boolean existsById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'existsById'");
    }
}