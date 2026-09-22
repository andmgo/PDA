package gescazone.demo.infrastructure.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Genera números consecutivos a partir de un número inicial, preservando el
 * prefijo no numérico y el ancho (ceros a la izquierda) — usado por la alta
 * masiva de apartamentos/parqueaderos/salones sociales.
 */
public class GeneradorNumeros {

    private static final Pattern PATRON = Pattern.compile("^(.*?)(\\d+)$");

    private GeneradorNumeros() {}

    public static List<String> generar(String numeroInicial, int cantidad) {
        if (numeroInicial == null || numeroInicial.trim().isEmpty())
            throw new IllegalArgumentException("El número inicial es obligatorio");
        if (cantidad < 1 || cantidad > 200)
            throw new IllegalArgumentException("La cantidad debe estar entre 1 y 200");

        Matcher m = PATRON.matcher(numeroInicial.trim());
        if (!m.matches())
            throw new IllegalArgumentException("El número inicial debe terminar en dígitos, ej: 101 o P-01");

        String prefijo = m.group(1);
        String digitos = m.group(2);
        int ancho = digitos.length();
        long inicio = Long.parseLong(digitos);

        List<String> resultado = new ArrayList<>();
        for (long i = 0; i < cantidad; i++) {
            resultado.add(prefijo + String.format("%0" + ancho + "d", inicio + i));
        }
        return resultado;
    }
}
