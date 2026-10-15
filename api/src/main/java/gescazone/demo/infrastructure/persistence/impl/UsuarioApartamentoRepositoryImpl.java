package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.infrastructure.persistence.entity.UsuarioApartamentoEntity;
import gescazone.demo.infrastructure.persistence.jpa.ApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioApartamentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public class UsuarioApartamentoRepositoryImpl implements UsuarioApartamentoRepository {

    @Autowired
    private UsuarioApartamentoJpaRepository jpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private ApartamentoJpaRepository apartamentoJpaRepository;

    @Override
    public void asignar(String idUsuario, String idApartamento) {
        UsuarioApartamentoEntity entity = new UsuarioApartamentoEntity();
        entity.setUsuario(usuarioJpaRepository.getReferenceById(UUID.fromString(idUsuario)));
        entity.setApartamento(apartamentoJpaRepository.getReferenceById(UUID.fromString(idApartamento)));
        entity.setFechaAsignacion(LocalDateTime.now());
        jpaRepository.save(entity);
    }

    @Override
    public void quitar(String idUsuario, String idApartamento) {
        jpaRepository.deleteByUsuario_IdAndApartamento_Id(UUID.fromString(idUsuario), UUID.fromString(idApartamento));
    }

    @Override
    public boolean existe(String idUsuario, String idApartamento) {
        return jpaRepository.existsByUsuario_IdAndApartamento_Id(UUID.fromString(idUsuario), UUID.fromString(idApartamento));
    }

    @Override
    public List<String> findApartamentoIdsByUsuario(String idUsuario) {
        return jpaRepository.findApartamentoIdsByUsuarioId(UUID.fromString(idUsuario))
                .stream().map(UUID::toString).toList();
    }

    @Override
    public List<String> findUsuarioIdsByApartamento(String idApartamento) {
        return jpaRepository.findUsuarioIdsByApartamentoId(UUID.fromString(idApartamento))
                .stream().map(UUID::toString).toList();
    }
}
