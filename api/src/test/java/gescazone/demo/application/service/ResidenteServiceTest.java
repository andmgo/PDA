package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.TipoDocumentoModel;
import gescazone.demo.domain.model.TipoResidenteModel;
import gescazone.demo.domain.repository.ResidenteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResidenteServiceTest {

    @Mock
    private ResidenteRepository residenteRepository;

    @InjectMocks
    private ResidenteService residenteService;

    private ResidenteModel residenteValido() {
        ResidenteModel r = new ResidenteModel();
        r.setNumeroDocumento(123456);
        r.setNombre(" Juan ");
        r.setApellido(" Pérez ");
        r.setCelular(3001234567L);
        r.setTipoDocumento(new TipoDocumentoModel(" CC "));
        r.setTipoResidente(new TipoResidenteModel(" Propietario "));
        return r;
    }

    @Test
    void crear_sinCelular_lanzaExcepcion() {
        ResidenteModel r = residenteValido();
        r.setCelular(null);
        assertThatThrownBy(() -> residenteService.crear(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("celular");
    }

    @Test
    void crear_sinTipoResidente_lanzaExcepcion() {
        ResidenteModel r = residenteValido();
        r.setTipoResidente(null);
        assertThatThrownBy(() -> residenteService.crear(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tipo de residente");
    }

    @Test
    void crear_documentoDuplicado_lanzaExcepcion() {
        ResidenteModel r = residenteValido();
        when(residenteRepository.existsByNumeroDocumento(123456)).thenReturn(true);
        assertThatThrownBy(() -> residenteService.crear(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe");
        verify(residenteRepository, never()).save(any());
    }

    @Test
    void crear_valido_recortaCamposYGuarda() {
        ResidenteModel r = residenteValido();
        when(residenteRepository.existsByNumeroDocumento(123456)).thenReturn(false);

        String resultado = residenteService.crear(r);

        assertThat(resultado).isEqualTo("Residente registrado con éxito");
        assertThat(r.getNombre()).isEqualTo("Juan");
        assertThat(r.getApellido()).isEqualTo("Pérez");
        assertThat(r.getTipoDocumento().getNombreTipoDocumento()).isEqualTo("CC");
        verify(residenteRepository).save(r);
    }

    @Test
    void actualizar_documentoYaUsadoPorOtroResidente_lanzaExcepcion() {
        ResidenteModel r = residenteValido();
        r.setId("id-1");
        when(residenteRepository.findById("id-1")).thenReturn(Optional.of(r));

        ResidenteModel otro = residenteValido();
        otro.setId("id-2");
        when(residenteRepository.findByNumeroDocumento(123456)).thenReturn(Optional.of(otro));

        assertThatThrownBy(() -> residenteService.actualizar(r))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe otro");
    }

    @Test
    void eliminar_noExistente_lanzaExcepcion() {
        when(residenteRepository.existsByNumeroDocumento(123456)).thenReturn(false);
        assertThatThrownBy(() -> residenteService.eliminar(123456))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void buscarPorNombreOApellido_delegaAlRepositorioConMismoTermino() {
        when(residenteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase("juan", "juan"))
                .thenReturn(java.util.List.of(residenteValido()));

        var resultado = residenteService.buscarPorNombreOApellido("juan");

        assertThat(resultado).hasSize(1);
    }
}
