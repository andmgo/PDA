package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.repository.CatalogoRepository;
import gescazone.demo.infrastructure.persistence.entity.EstadoCuentaEntity;
import gescazone.demo.infrastructure.persistence.entity.EstadoEntity;
import gescazone.demo.infrastructure.persistence.entity.RolEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoDocumentoEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoOcupacionEntity;
import gescazone.demo.infrastructure.persistence.entity.TipoResidenteEntity;
import gescazone.demo.infrastructure.persistence.jpa.EstadoCuentaJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.EstadoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.RolJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoDocumentoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoOcupacionJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.TipoResidenteJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Traduce el nombre de un catálogo (rol, tipo de documento, estado, …) a su
 * fila real. Reemplaza el patrón anterior "findByNombre(...).orElseGet(crear)"
 * que tenían 7 repositorios: cualquier variante de escritura ("Administrador"
 * vs "ADMINISTRADOR", "Al dia" vs "Al día", "Cédula de Ciudadanía" con la
 * codificación rota) creaba EN SILENCIO una fila nueva del catálogo — así se
 * llegó a tener un rol "Administrador" sin ningún permiso, con un usuario
 * dentro. Ahora la comparación ignora mayúsculas, tildes y espacios, y si el
 * valor no existe se rechaza con un 400 que lista las opciones válidas.
 */
@Component
public class CatalogoResolver implements CatalogoRepository {

    @Override
    public Map<String, List<String>> todos() {
        Map<String, List<String>> catalogos = new LinkedHashMap<>();
        catalogos.put("roles", ordenados(rolJpaRepository.findAll(), RolEntity::getNombreRol));
        catalogos.put("tiposDocumento", ordenados(tipoDocumentoJpaRepository.findAll(), TipoDocumentoEntity::getNombreTipoDocumento));
        catalogos.put("estados", ordenados(estadoJpaRepository.findAll(), EstadoEntity::getNombreEstado));
        catalogos.put("estadosCuenta", ordenados(estadoCuentaJpaRepository.findAll(), EstadoCuentaEntity::getNombreEstadoCuenta));
        catalogos.put("tiposOcupacion", ordenados(tipoOcupacionJpaRepository.findAll(), TipoOcupacionEntity::getNombreTipoOcupacion));
        catalogos.put("tiposResidente", ordenados(tipoResidenteJpaRepository.findAll(), TipoResidenteEntity::getNombreTipoResidente));
        return catalogos;
    }

    private static <E> List<String> ordenados(List<E> filas, Function<E, String> nombreDe) {
        return filas.stream().map(nombreDe).sorted().toList();
    }

    /** Nombres largos que llegan desde formularios (registro con Google, etc.). */
    private static final Map<String, String> ALIAS_TIPO_DOCUMENTO = Map.of(
            "cedula de ciudadania", "CC",
            "cedula", "CC",
            "cedula de extranjeria", "CE",
            "tarjeta de identidad", "TI",
            "pasaporte", "Pasaporte");

    @Autowired private RolJpaRepository rolJpaRepository;
    @Autowired private TipoDocumentoJpaRepository tipoDocumentoJpaRepository;
    @Autowired private EstadoJpaRepository estadoJpaRepository;
    @Autowired private EstadoCuentaJpaRepository estadoCuentaJpaRepository;
    @Autowired private TipoOcupacionJpaRepository tipoOcupacionJpaRepository;
    @Autowired private TipoResidenteJpaRepository tipoResidenteJpaRepository;

    /** "  Al Día " → "al dia". Pública para que otros chequeos de duplicados usen el mismo criterio. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto.trim(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return sinTildes.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    public RolEntity rol(String nombre) {
        return buscar(rolJpaRepository.findAll(), RolEntity::getNombreRol, nombre, "rol");
    }

    public TipoDocumentoEntity tipoDocumento(String nombre) {
        String alias = ALIAS_TIPO_DOCUMENTO.get(normalizar(nombre));
        return buscar(tipoDocumentoJpaRepository.findAll(), TipoDocumentoEntity::getNombreTipoDocumento,
                alias != null ? alias : nombre, "tipo de documento");
    }

    public EstadoEntity estado(String nombre) {
        return buscar(estadoJpaRepository.findAll(), EstadoEntity::getNombreEstado, nombre, "estado");
    }

    public EstadoCuentaEntity estadoCuenta(String nombre) {
        return buscar(estadoCuentaJpaRepository.findAll(), EstadoCuentaEntity::getNombreEstadoCuenta, nombre, "estado de cuenta");
    }

    public TipoOcupacionEntity tipoOcupacion(String nombre) {
        return buscar(tipoOcupacionJpaRepository.findAll(), TipoOcupacionEntity::getNombreTipoOcupacion, nombre, "tipo de ocupación");
    }

    public TipoResidenteEntity tipoResidente(String nombre) {
        return buscar(tipoResidenteJpaRepository.findAll(), TipoResidenteEntity::getNombreTipoResidente, nombre, "tipo de residente");
    }

    private static <E> E buscar(List<E> filas, Function<E, String> nombreDe, String buscado, String etiqueta) {
        String clave = normalizar(buscado);
        return filas.stream()
                .filter(f -> normalizar(nombreDe.apply(f)).equals(clave))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "\"" + (buscado == null ? "" : buscado.trim()) + "\" no es un " + etiqueta + " válido. Opciones: "
                                + filas.stream().map(nombreDe).sorted().collect(Collectors.joining(", "))));
    }
}
