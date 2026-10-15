package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.VisitaAutorizadaModel;
import gescazone.demo.domain.repository.VisitaAutorizadaRepository;
import gescazone.demo.infrastructure.persistence.entity.VisitaAutorizadaEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.VisitaAutorizadaJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class VisitaAutorizadaRepositoryImpl implements VisitaAutorizadaRepository {

    @Autowired private VisitaAutorizadaJpaRepository jpaRepository;
    @Autowired private ApartamentoJpaRepository apartamentoJpaRepository;
    @Autowired private UsuarioJpaRepository usuarioJpaRepository;

    private VisitaAutorizadaModel toModel(VisitaAutorizadaEntity e) {
        VisitaAutorizadaModel m = new VisitaAutorizadaModel();
        m.setId(e.getId().toString());
        m.setIdApartamento(e.getApartamento().getId().toString());
        m.setIdAutorizadaPor(e.getAutorizadaPor().getId().toString());
        m.setNombreVisitante(e.getNombreVisitante());
        m.setApellidoVisitante(e.getApellidoVisitante());
        m.setNumeroDocumento(e.getNumeroDocumento());
        m.setPlaca(e.getPlaca());
        m.setFechaDesde(e.getFechaDesde());
        m.setFechaHasta(e.getFechaHasta());
        m.setNota(e.getNota());
        m.setCancelada(e.isCancelada());
        m.setFechaCreacion(e.getFechaCreacion());
        return m;
    }

    private VisitaAutorizadaEntity toEntity(VisitaAutorizadaModel m) {
        VisitaAutorizadaEntity e = new VisitaAutorizadaEntity();
        if (m.getId() != null) e.setId(UUID.fromString(m.getId()));
        e.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(m.getIdApartamento())));
        e.setAutorizadaPor(usuarioJpaRepository.getReferenceById(UUID.fromString(m.getIdAutorizadaPor())));
        e.setNombreVisitante(m.getNombreVisitante());
        e.setApellidoVisitante(m.getApellidoVisitante());
        e.setNumeroDocumento(m.getNumeroDocumento());
        e.setPlaca(m.getPlaca());
        e.setFechaDesde(m.getFechaDesde());
        e.setFechaHasta(m.getFechaHasta());
        e.setNota(m.getNota());
        e.setCancelada(m.isCancelada());
        e.setFechaCreacion(m.getFechaCreacion());
        return e;
    }

    @Override
    public VisitaAutorizadaModel save(VisitaAutorizadaModel visita) {
        return toModel(jpaRepository.save(toEntity(visita)));
    }

    @Override
    public Optional<VisitaAutorizadaModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<VisitaAutorizadaModel> findByAutorizadaPor(String idUsuario) {
        return jpaRepository.findByAutorizadaPor_IdOrderByFechaDesdeDescFechaCreacionDesc(UUID.fromString(idUsuario))
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<VisitaAutorizadaModel> findVigentesEn(LocalDate fecha) {
        return jpaRepository.findVigentesEn(fecha).stream().map(this::toModel).toList();
    }

    @Override
    public long contarIngresos(String idVisitaAutorizada) {
        return jpaRepository.contarIngresos(UUID.fromString(idVisitaAutorizada));
    }
}
