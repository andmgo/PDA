package gescazone.demo.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNoException;

class VehiculoModelTest {

    @ParameterizedTest
    @CsvSource({
            "abc-123, ABC123",
            "' ABC 123 ', ABC123",
            "xyz12d, XYZ12D",
            "Abc.12, ABC12"
    })
    void normalizarPlaca_quitaSeparadoresYPasaAMayusculas(String entrada, String esperado) {
        assertThat(VehiculoModel.normalizarPlaca(entrada)).isEqualTo(esperado);
    }

    @ParameterizedTest
    @CsvSource({"ABC123, CARRO", "ABC12D, MOTO", "ABC12, MOTO"})
    void validarFormatoPlaca_formatosColombianosValidos(String placa, VehiculoModel.Tipo tipo) {
        assertThatNoException().isThrownBy(() -> VehiculoModel.validarFormatoPlaca(placa, tipo));
    }

    @ParameterizedTest
    @CsvSource({"ABC12D, CARRO", "AB1234, CARRO", "ABC123, MOTO", "ABCD12, MOTO"})
    void validarFormatoPlaca_formatoQueNoCorrespondeAlTipo_rechaza(String placa, VehiculoModel.Tipo tipo) {
        assertThatThrownBy(() -> VehiculoModel.validarFormatoPlaca(placa, tipo))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nuevoVehiculo_quedaActivo_yDesactivarDosVecesFalla() {
        VehiculoModel vehiculo = new VehiculoModel("abc-123", VehiculoModel.Tipo.CARRO, "apto-1");
        assertThat(vehiculo.isActivo()).isTrue();
        assertThat(vehiculo.getPlaca()).isEqualTo("ABC123");

        vehiculo.desactivar();
        assertThat(vehiculo.isActivo()).isFalse();
        assertThatThrownBy(vehiculo::desactivar).isInstanceOf(IllegalStateException.class);
    }
}
