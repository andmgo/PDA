package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.ReservaSalonSocialModel;
import gescazone.demo.domain.repository.ReservaSalonSocialRepository;
import gescazone.demo.infrastructure.persistence.entity.ReservaSalonSocialEntity;
import gescazone.demo.infrastructure.persistence.jpa.ReservaSalonSocialJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.SalonSocialJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.UsuarioJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ReservaSalonSocialRepositoryImpl implements ReservaSalonSocialRepository {

    @Autowired
    private ReservaSalonSocialJpaRepository jpaRepository;

    @Autowired
    private UsuarioJpaRepository usuarioJpaRepository;

    @Autowired
    private SalonSocialJpaRepository salonSocialJpaRepository;

    private ReservaSalonSocialModel toModel(ReservaSalonSocialEntity entity) {
        ReservaSalonSocialModel model = new ReservaSalonSocialModel();
        model.setId(entity.getId().toString());
        model.setIdUsuario(entity.getUsuario().getId().toString());
        model.setIdSalon(entity.getSalon().getId().toString());
        model.setFechaYHoraReserva(entity.getFechaYHoraReserva());
        return model;
    }

    private ReservaSalonSocialEntity toEntity(ReservaSalonSocialModel model) {
        ReservaSalonSocialEntity entity = new ReservaSalonSocialEntity();
        if (model.getId() != null) entity.setId(UUID.fromString(model.getId()));
        entity.setUsuario(usuarioJpaRepository.getReferenceById(UUID.fromString(model.getIdUsuario())));
        entity.setSalon(salonSocialJpaRepository.getReferenceById(UUID.fromString(model.getIdSalon())));
        entity.setFechaYHoraReserva(model.getFechaYHoraReserva());
        return entity;
    }

    @Override
    public List<ReservaSalonSocialModel> findByIdSalon(String idSalon) {
        return jpaRepository.findBySalon_Id(UUID.fromString(idSalon)).stream().map(this::toModel).toList();
    }

    @Override
    public List<ReservaSalonSocialModel> findBySalonNumero(String numero) {
        return jpaRepository.findBySalon_Numero(numero).stream().map(this::toModel).toList();
    }

    @Override
    public List<ReservaSalonSocialModel> findByIdUsuario(String idUsuario) {
        return jpaRepository.findByUsuario_Id(UUID.fromString(idUsuario)).stream().map(this::toModel).toList();
    }

    @Override
    public List<ReservaSalonSocialModel> findByUsuarioNumeroDocumento(String documento) {
        return jpaRepository.findByUsuario_NumeroDocumento(documento).stream().map(this::toModel).toList();
    }

    @Override
    public boolean existsByIdSalonAndFechaYHoraReserva(String idSalon, LocalDateTime fechaYHora) {
        return jpaRepository.existsBySalon_IdAndFechaYHoraReserva(UUID.fromString(idSalon), fechaYHora);
    }

    @Override
    public List<ReservaSalonSocialModel> findByFechaYHoraReservaBetween(LocalDateTime inicio, LocalDateTime fin) {
        return jpaRepository.findByFechaYHoraReservaBetween(inicio, fin)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ReservaSalonSocialModel> findReservasFuturasPorSalon(String idSalon, LocalDateTime ahora) {
        return jpaRepository.findBySalon_IdAndFechaYHoraReservaAfter(UUID.fromString(idSalon), ahora)
                .stream().map(this::toModel).toList();
    }

    @Override
    public List<ReservaSalonSocialModel> findReservasFuturasPorUsuario(String idUsuario, LocalDateTime ahora) {
        return jpaRepository.findByUsuario_IdAndFechaYHoraReservaAfter(UUID.fromString(idUsuario), ahora)
                .stream().map(this::toModel).toList();
    }

    @Override
    public boolean existsReservaEnMismoDia(String idSalon, LocalDateTime fecha) {
        LocalDateTime inicioDia = fecha.toLocalDate().atStartOfDay();
        LocalDateTime finDia = fecha.toLocalDate().atTime(23, 59, 59);
        return jpaRepository.existsBySalon_IdAndFechaYHoraReservaBetween(UUID.fromString(idSalon), inicioDia, finDia);
    }

    @Override
    public ReservaSalonSocialModel save(ReservaSalonSocialModel reserva) {
        return toModel(jpaRepository.save(toEntity(reserva)));
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(UUID.fromString(id));
    }

    @Override
    public Optional<ReservaSalonSocialModel> findById(String id) {
        return jpaRepository.findById(UUID.fromString(id)).map(this::toModel);
    }

    @Override
    public List<ReservaSalonSocialModel> findAll() {
        return jpaRepository.findAll().stream().map(this::toModel).toList();
    }
}
