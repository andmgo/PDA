package gescazone.demo.domain.repository;

import java.util.List;

/** Vínculo propietario (Usuario) ↔ apartamento, muchos a muchos. */
public interface UsuarioApartamentoRepository {
    void asignar(String idUsuario, String idApartamento);
    void quitar(String idUsuario, String idApartamento);
    boolean existe(String idUsuario, String idApartamento);
    List<String> findApartamentoIdsByUsuario(String idUsuario);
    List<String> findUsuarioIdsByApartamento(String idApartamento);
}
