package gescazone.demo.infrastructure.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GeneradorNumerosTest {

    @Test
    void generar_sinPrefijo_generaConsecutivosMismoAncho() {
        List<String> resultado = GeneradorNumeros.generar("101", 5);
        assertThat(resultado).containsExactly("101", "102", "103", "104", "105");
    }

    @Test
    void generar_conPrefijoDeLetras_preservaElPrefijo() {
        List<String> resultado = GeneradorNumeros.generar("P-01", 3);
        assertThat(resultado).containsExactly("P-01", "P-02", "P-03");
    }

    @Test
    void generar_saltoDeAncho_creceNaturalmente() {
        List<String> resultado = GeneradorNumeros.generar("P-98", 3);
        assertThat(resultado).containsExactly("P-98", "P-99", "P-100");
    }

    @Test
    void generar_cantidadCero_lanzaExcepcion() {
        assertThatThrownBy(() -> GeneradorNumeros.generar("101", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 y 200");
    }

    @Test
    void generar_cantidadExcesiva_lanzaExcepcion() {
        assertThatThrownBy(() -> GeneradorNumeros.generar("101", 201))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 1 y 200");
    }

    @Test
    void generar_numeroInicialSinDigitos_lanzaExcepcion() {
        assertThatThrownBy(() -> GeneradorNumeros.generar("Torre-A", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("terminar en dígitos");
    }

    @Test
    void generar_numeroInicialVacio_lanzaExcepcion() {
        assertThatThrownBy(() -> GeneradorNumeros.generar("  ", 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatorio");
    }
}
