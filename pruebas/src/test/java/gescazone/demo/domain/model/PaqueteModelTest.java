package gescazone.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Paquete de recepción")
class PaqueteModelTest {

    private PaqueteModel paquete() {
        return new PaqueteModel("apto-401", "Elena Vásquez", "300000001");
    }

    @Test
    @DisplayName("llega pendiente, con fecha de llegada")
    void llegaPendiente() {
        PaqueteModel p = paquete();
        assertThat(p.isEntregado()).isFalse();
        assertThat(p.getFechaHoraLlegada()).isNotNull();
        assertThat(p.getFechaHoraEntrega()).isNull();
    }

    @ParameterizedTest(name = "quien retira = \"{0}\" → el destinatario")
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("si no se dice quién lo retira, se entrega al destinatario")
    void entregaAlDestinatario(String quien) {
        PaqueteModel p = paquete();
        p.marcarEntregado(quien, "func-1");
        assertThat(p.isEntregado()).isTrue();
        assertThat(p.getEntregadoA()).isEqualTo("Elena Vásquez");
        assertThat(p.getIdEntregadoPor()).isEqualTo("func-1");
        assertThat(p.getFechaHoraEntrega()).isNotNull();
    }

    @Test
    @DisplayName("registra a otra persona que lo retira (sin espacios sobrantes)")
    void entregaAOtraPersona() {
        PaqueteModel p = paquete();
        p.marcarEntregado("  María (empleada) ", null);
        assertThat(p.getEntregadoA()).isEqualTo("María (empleada)");
    }

    @Test
    @DisplayName("no se entrega dos veces")
    void dobleEntrega() {
        PaqueteModel p = paquete();
        p.marcarEntregado();
        assertThatThrownBy(p::marcarEntregado)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ya fue marcado como entregado");
    }
}
