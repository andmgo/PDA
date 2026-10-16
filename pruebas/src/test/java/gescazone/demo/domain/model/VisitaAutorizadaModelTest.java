package gescazone.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Visita autorizada por el propietario")
class VisitaAutorizadaModelTest {

    private static final LocalDate HOY = LocalDate.of(2026, 10, 16);

    private VisitaAutorizadaModel visita(LocalDate desde, LocalDate hasta) {
        VisitaAutorizadaModel v = new VisitaAutorizadaModel();
        v.setNombreVisitante(" Carla ");
        v.setApellidoVisitante(" Mejía ");
        v.setFechaDesde(desde);
        v.setFechaHasta(hasta);
        return v;
    }

    @Nested
    @DisplayName("al autorizarla")
    class Validacion {

        @Test
        @DisplayName("hoy mismo y por un día es válida; limpia espacios")
        void valida() {
            VisitaAutorizadaModel v = visita(HOY, HOY);
            v.setNota("   ");
            assertThatCode(() -> v.validarNueva(HOY)).doesNotThrowAnyException();
            assertThat(v.nombreCompleto()).isEqualTo("Carla Mejía");
            assertThat(v.getNota()).isNull();
        }

        @Test
        @DisplayName("exige nombre y apellido")
        void sinNombre() {
            VisitaAutorizadaModel v = visita(HOY, HOY);
            v.setApellidoVisitante(" ");
            assertThatThrownBy(() -> v.validarNueva(HOY)).hasMessageContaining("nombre y el apellido");
        }

        @Test
        @DisplayName("no puede empezar en el pasado")
        void pasada() {
            assertThatThrownBy(() -> visita(HOY.minusDays(1), HOY).validarNueva(HOY)).hasMessageContaining("pasada");
        }

        @Test
        @DisplayName("la fecha final no puede ser anterior a la inicial")
        void rangoInvertido() {
            assertThatThrownBy(() -> visita(HOY.plusDays(3), HOY.plusDays(1)).validarNueva(HOY))
                    .hasMessageContaining("anterior");
        }

        @ParameterizedTest(name = "{0} días → {1}")
        @CsvSource({"30, true", "31, false"})
        @DisplayName("cubre máximo 30 días")
        void maximoTreintaDias(int dias, boolean valida) {
            VisitaAutorizadaModel v = visita(HOY, HOY.plusDays(dias - 1));
            if (valida) assertThatCode(() -> v.validarNueva(HOY)).doesNotThrowAnyException();
            else assertThatThrownBy(() -> v.validarNueva(HOY)).hasMessageContaining("30 días");
        }

        @ParameterizedTest(name = "empieza en {0} días → {1}")
        @CsvSource({"90, true", "91, false"})
        @DisplayName("se autoriza con hasta 90 días de anticipación")
        void anticipacion(int dias, boolean valida) {
            VisitaAutorizadaModel v = visita(HOY.plusDays(dias), HOY.plusDays(dias));
            if (valida) assertThatCode(() -> v.validarNueva(HOY)).doesNotThrowAnyException();
            else assertThatThrownBy(() -> v.validarNueva(HOY)).hasMessageContaining("90 días");
        }
    }

    @Nested
    @DisplayName("vigencia")
    class Vigencia {

        @ParameterizedTest(name = "día {0} → vigente {1}")
        @CsvSource({"-1, false", "0, true", "2, true", "3, false"})
        @DisplayName("solo dentro del rango, incluidos los extremos")
        void enRango(int desplazamiento, boolean vigente) {
            VisitaAutorizadaModel v = visita(HOY, HOY.plusDays(2));
            assertThat(v.vigenteEn(HOY.plusDays(desplazamiento))).isEqualTo(vigente);
        }

        @Test
        @DisplayName("cancelada deja de estar vigente")
        void cancelada() {
            VisitaAutorizadaModel v = visita(HOY, HOY.plusDays(2));
            v.cancelar(HOY);
            assertThat(v.vigenteEn(HOY)).isFalse();
        }

        @Test
        @DisplayName("no se cancela dos veces ni después de vencida")
        void cancelacionesInvalidas() {
            VisitaAutorizadaModel v = visita(HOY, HOY);
            v.cancelar(HOY);
            assertThatThrownBy(() -> v.cancelar(HOY)).isInstanceOf(IllegalStateException.class).hasMessageContaining("ya estaba");

            VisitaAutorizadaModel vieja = visita(HOY.minusDays(5), HOY.minusDays(4));
            assertThat(vieja.vencida(HOY)).isTrue();
            assertThatThrownBy(() -> vieja.cancelar(HOY)).hasMessageContaining("venció");
        }
    }
}
