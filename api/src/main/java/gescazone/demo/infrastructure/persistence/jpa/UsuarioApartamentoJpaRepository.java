package gescazone.demo.infrastructure.persistence.jpa;

import gescazone.demo.infrastructure.persistence.entity.UsuarioApartamentoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface UsuarioApartamentoJpaRepository extends JpaRepository<UsuarioApartamentoEntity, UUID> {

    boolean existsByUsuario_IdAndApartamento_Id(UUID idUsuario, UUID idApartamento);

    @Transactional
    long deleteByUsuario_IdAndApartamento_Id(UUID idUsuario, UUID idApartamento);

    @Query("select ua.apartamento.id from UsuarioApartamentoEntity ua where ua.usuario.id = :idUsuario")
    List<UUID> findApartamentoIdsByUsuarioId(UUID idUsuario);

    @Query("select ua.usuario.id from UsuarioApartamentoEntity ua where ua.apartamento.id = :idApartamento order by ua.fechaAsignacion")
    List<UUID> findUsuarioIdsByApartamentoId(UUID idApartamento);
}
