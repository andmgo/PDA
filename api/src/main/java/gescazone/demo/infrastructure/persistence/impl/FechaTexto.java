package gescazone.demo.infrastructure.persistence.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Las visitas manejan sus fechas en el dominio como texto "yyyy-MM-dd HH:mm:ss"
 * (herencia de la época de MongoDB). En la base ahora son TIMESTAMP reales
 * (migración V13) — ordenar, filtrar por día o comparar ya no depende de que
 * el texto tenga el formato exacto. La conversión vive solo aquí, en la capa
 * de persistencia, para no tocar dominio, servicios ni el contrato del API.
 */
final class FechaTexto {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private FechaTexto() {}

    static LocalDateTime aFecha(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String limpio = texto.trim().replace('T', ' ');
        if (limpio.length() == 16) limpio += ":00";            // "yyyy-MM-dd HH:mm"
        if (limpio.length() > 19) limpio = limpio.substring(0, 19); // descarta fracciones
        return LocalDateTime.parse(limpio, FORMATO);
    }

    static String aTexto(LocalDateTime fecha) {
        return fecha == null ? null : fecha.format(FORMATO);
    }
}
