package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.TipoOcupacionModel;
import gescazone.demo.domain.model.EstadoCuentaModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.infrastructure.persistence.entity.ApartamentoEntity;
import gescazone.demo.infrastructure.persistence.entity.EstadoCuentaEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoOcupacionEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.EstadoCuentaJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoOcupacionJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ApartamentoRepositoryImpl implements ApartamentoRepository {

    @Autowired
    private ApartamentoJpaRepository jpaRepository;

    @Autowired
    private TipoOcupacionJpaRepository tipoOcupacionJpaRepository;

    @Autowired
    private EstadoCuentaJpaRepository estadoCuentaJpaRepository;

    private ApartamentoModel toModel(ApartamentoEntity entity) {
        ApartamentoModel model = new ApartamentoModel();
        model.setId(entity.getId().toString());
        model.setNumero(entity.getNumero());
        model.setMedidas(entity.getMedidas());
        model.setTelefono(entity.getTelefono());
        model.setActivo(entity.isActivo());

        if (entity.getTipoOcupacion() != null) {
            TipoOcupacionModel to = new TipoOcupacionModel();
            to.setNombreTipoOcupacion(entity.getTipoOcupacion().getNombreTipoOcupacion());
            model.setTipoOcupacion(to);
        }

        if (entity.getEstadoCuenta() != null) {
            EstadoCuentaModel ec = new EstadoCuentaModel();
            ec.setNombreEstadoCuenta(entity.getEstadoCuenta().getNombreEstadoCuenta());
            model.setEstadoCuenta(ec);
        }

        return model;
    }

    private ApartamentoEntity toEntity(ApartamentoModel model) {
        ApartamentoEntity entity = new ApartamentoEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setNumero(model.getNumero());
        entity.setMedidas(model.getMedidas());
        entity.setTelefono(model.getTelefono());
        entity.setActivo(model.isActivo());

        if (model.getTipoOcupacion() != null) {
            String nombre = model.getTipoOcupacion().getNombreTipoOcupacion();
            TipoOcupacionEntity to = tipoOcupacionJpaRepository.findByNombreTipoOcupacion(nombre)
                    .orElseGet(() -> tipoOcupacionJpaRepository.save(new TipoOcupacionEntity(nombre)));
            entity.setTipoOcupacion(to);
        }

        if (model.getEstadoCuenta() != null) {
            String nombre = model.getEstadoCuenta().getNombreEstadoCuenta();
            EstadoCuentaEntity ec = estadoCuentaJpaRepository.findByNombreEstadoCuenta(nombre)
                    .orElseGet(() -> estadoCuentaJpaRepository.save(new EstadoCuentaEntity(nombre)));
            entity.setEstadoCuenta(ec);
        }

        return entity;
    }

    @Override
    public Optional<ApartamentoModel> findByNumero(String numero) {
        return jpaRepository.findByNumero(numero).map(this::toModel);
    }

    @Override
    public boolean existsByNumero(String numero) {
        return jpaRepository.existsByNumero(numero);
    }

    @Override
    public List<ApartamentoModel> findByNombreTipoOcupacion(String nombreTipoOcupacion) {
        return jpaRepository.findByTipoOcupacion_NombreTipoOcupacion(nombreTipoOcupacion)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ApartamentoModel> findByNombreEstadoCuenta(String nombreEstadoCuenta) {
        return jpaRepository.findByEstadoCuenta_NombreEstadoCuenta(nombreEstadoCuenta)
                .stream().map(this::toModel).toList();
    }

    @Override
    public ApartamentoModel save(ApartamentoModel apartamento) {
        return toModel(jpaRepository.save(toEntity(apartamento)));
    }

    @Override
    public Optional<ApartamentoModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<ApartamentoModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
