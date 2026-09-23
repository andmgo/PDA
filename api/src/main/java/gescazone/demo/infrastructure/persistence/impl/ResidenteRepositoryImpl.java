package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.TipoResidenteModel;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.infrastructure.persistence.entity.ResidenteEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoDocumentoEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoResidenteEntity;
import gescazone.demo.infrastructure.persistence.jpa.ResidenteJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoDocumentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoResidenteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ResidenteRepositoryImpl implements ResidenteRepository {

    @Autowired
    private ResidenteJpaRepository jpaRepository;

    @Autowired
    private TipoDocumentoJpaRepository tipoDocumentoJpaRepository;

    @Autowired
    private TipoResidenteJpaRepository tipoResidenteJpaRepository;

    private ResidenteModel toModel(ResidenteEntity entity) {
        ResidenteModel model = new ResidenteModel();
        model.setId(entity.getId().toString());
        model.setNumeroDocumento(entity.getNumeroDocumento());
        model.setNombre(entity.getNombre());
        model.setApellido(entity.getApellido());
        model.setCelular(entity.getCelular());
        model.setActivo(entity.isActivo());

        if (entity.getTipoDocumento() != null) {
            TipoDocumentoModel td = new TipoDocumentoModel();
            td.setNombreTipoDocumento(entity.getTipoDocumento().getNombreTipoDocumento());
            model.setTipoDocumento(td);
        }

        if (entity.getTipoResidente() != null) {
            TipoResidenteModel tr = new TipoResidenteModel();
            tr.setNombreTipoResidente(entity.getTipoResidente().getNombreTipoResidente());
            model.setTipoResidente(tr);
        }

        return model;
    }

    private ResidenteEntity toEntity(ResidenteModel model) {
        ResidenteEntity entity = new ResidenteEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setNumeroDocumento(model.getNumeroDocumento());
        entity.setNombre(model.getNombre());
        entity.setApellido(model.getApellido());
        entity.setCelular(model.getCelular());
        entity.setActivo(model.isActivo());

        if (model.getTipoDocumento() != null) {
            String nombre = model.getTipoDocumento().getNombreTipoDocumento();
            TipoDocumentoEntity td = tipoDocumentoJpaRepository.findByNombreTipoDocumento(nombre)
                    .orElseGet(() -> tipoDocumentoJpaRepository.save(new TipoDocumentoEntity(nombre)));
            entity.setTipoDocumento(td);
        }

        if (model.getTipoResidente() != null) {
            String nombre = model.getTipoResidente().getNombreTipoResidente();
            TipoResidenteEntity tr = tipoResidenteJpaRepository.findByNombreTipoResidente(nombre)
                    .orElseGet(() -> tipoResidenteJpaRepository.save(new TipoResidenteEntity(nombre)));
            entity.setTipoResidente(tr);
        }

        return entity;
    }

    @Override
    public Optional<ResidenteModel> findByNumeroDocumento(Integer numeroDocumento) {
        return jpaRepository.findByNumeroDocumento(numeroDocumento).map(this::toModel);
    }

    @Override
    public boolean existsByNumeroDocumento(Integer numeroDocumento) {
        return jpaRepository.existsByNumeroDocumento(numeroDocumento);
    }

    @Override
    public List<ResidenteModel> findByNombreTipoResidente(String nombreTipoResidente) {
        return jpaRepository.findByTipoResidente_NombreTipoResidente(nombreTipoResidente)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ResidenteModel> findByNombreTipoDocumento(String nombreTipoDocumento) {
        return jpaRepository.findByTipoDocumento_NombreTipoDocumento(nombreTipoDocumento)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ResidenteModel> findByNombreContainingIgnoreCase(String nombre) {
        return jpaRepository.findByNombreContainingIgnoreCase(nombre)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ResidenteModel> findByApellidoContainingIgnoreCase(String apellido) {
        return jpaRepository.findByApellidoContainingIgnoreCase(apellido)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ResidenteModel> findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(String nombre, String apellido) {
        return jpaRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(nombre, apellido)
                .stream().map(this::toModel).toList();
    }

    @Override
    public ResidenteModel save(ResidenteModel residente) {
        return toModel(jpaRepository.save(toEntity(residente)));
    }

    @Override
    public Optional<ResidenteModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<ResidenteModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
