package gescazone.demo.domain.repository;

import java.util.List;
import java.util.Map;

/**
 * Valores vigentes de cada catálogo (roles, tipos de documento, estados…),
 * leídos de la base de datos — única fuente de verdad. Claves: roles,
 * tiposDocumento, estados, estadosCuenta, tiposOcupacion, tiposResidente.
 */
public interface CatalogoRepository {
    Map<String, List<String>> todos();
}
