package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.domain.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VisitaServiceTest {

    @Mock private ResidenteRepository residenteRepository;
    @Mock private ApartamentoRepository apartamentoRepository;
    @Mock private ParqueaderoRepository parqueaderoRepository;
    @Mock private RegistroVisitanteRepository registroVisitanteRepository;
    @Mock private RegistroParqueaderoVisitanteRepository registroParqueaderoRepository;
    @Mock private VehiculoRepository vehiculoRepository;

    @InjectMocks
    private VisitaService visitaService;

    private ApartamentoModel apto301;

    @BeforeEach
    void preparar() {
        apto301 = new ApartamentoModel();
        apto301.setId("apto-1");
        apto301.setNumero("301");
        apto301.setActivo(true);
        when(apartamentoRepository.findByNumero("301")).thenReturn(Optional.of(apto301));
        when(residenteRepository.save(any())).thenAnswer(inv -> {
            ResidenteModel r = inv.getArgument(0);
            r.setId("res-nuevo");
            return r;
        });
    }

    private ResidenteModel visitante(String id, boolean activo) {
        ResidenteModel r = new ResidenteModel();
        r.setId(id);
        r.setNumeroDocumento("1001");
        r.setNombre("Luis");
        r.setApellido("Gómez");
        r.setActivo(activo);
        return r;
    }

    private ParqueaderoModel parqueadero(String id, String numero, String estado) {
        ParqueaderoModel p = new ParqueaderoModel();
        p.setId(id);
        p.setNumero(numero);
        p.setActivo(true);
        p.setEstado(new EstadoModel(estado));
        return p;
    }

    private VisitaService.Ingreso aPie(String doc, String nombre, String apellido) {
        return new VisitaService.Ingreso(doc, nombre, apellido, null, "301", false, null, null);
    }

    @Test
    void ingreso_documentoNuevo_creaElVisitanteYRegistraEnUnSoloPaso() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.empty());

        String resultado = visitaService.registrarIngreso(aPie("1.001", "Luis", "Gómez"));

        assertThat(resultado).contains("Luis Gómez").contains("301");
        verify(residenteRepository).save(argThat(r ->
                "1001".equals(r.getNumeroDocumento()) && r.getTipoResidente().getNombreTipoResidente().equals("Visitante")));
        verify(registroVisitanteRepository).save(argThat(r ->
                r.getIdResidente().equals("res-nuevo") && r.getIdApartamento().equals("apto-1")));
    }

    @Test
    void ingreso_documentoNuevoSinNombre_pideNombre() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitaService.registrarIngreso(aPie("1001", "", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("primera visita");
        verify(registroVisitanteRepository, never()).save(any());
    }

    @Test
    void ingreso_visitanteQueYaVinoAntesYSalio_puedeVolverAEntrar() {
        // Antes esto fallaba: "Ya existe un registro para este residente en este apartamento"
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", true)));
        when(registroVisitanteRepository.findByIdResidenteAndFechaHoraSalidaIsNull("res-1")).thenReturn(List.of());
        when(registroParqueaderoRepository.findByIdResidenteAndFechaHoraSalidaIsNull("res-1")).thenReturn(List.of());

        visitaService.registrarIngreso(aPie("1001", null, null));

        verify(residenteRepository, never()).save(any());
        verify(registroVisitanteRepository).save(any());
    }

    @Test
    void ingreso_visitanteQueYaEstaAdentro_rechaza() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", true)));
        when(registroVisitanteRepository.findByIdResidenteAndFechaHoraSalidaIsNull("res-1"))
                .thenReturn(List.of(new RegistroVisitanteModel("res-1", "apto-2", "2026-10-15 08:00:00")));

        assertThatThrownBy(() -> visitaService.registrarIngreso(aPie("1001", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está dentro");
    }

    @Test
    void ingreso_visitanteBloqueado_rechaza() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", false)));

        assertThatThrownBy(() -> visitaService.registrarIngreso(aPie("1001", null, null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("bloqueado");
    }

    @Test
    void ingreso_apartamentoInexistente_noEncontrado() {
        when(apartamentoRepository.findByNumero("999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitaService.registrarIngreso(
                new VisitaService.Ingreso("1001", "Luis", "Gómez", null, "999", false, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void ingresoEnVehiculo_sinParqueadero_asignaElPrimeroLibreQueNoSeaDeUnResidente() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", true)));
        ParqueaderoModel p1 = parqueadero("p-1", "P-01", "Disponible");   // asignado a un residente
        ParqueaderoModel p2 = parqueadero("p-2", "P-02", "Mantenimiento");
        ParqueaderoModel p3 = parqueadero("p-3", "P-03", "Disponible");   // ← el que debe tocar
        when(parqueaderoRepository.findAll()).thenReturn(List.of(p1, p2, p3));
        when(parqueaderoRepository.findByNumero("P-03")).thenReturn(Optional.of(p3));
        when(vehiculoRepository.findIdsParqueaderoAsignados()).thenReturn(List.of("p-1"));
        when(registroParqueaderoRepository.findByFechaHoraSalidaIsNull()).thenReturn(List.of());

        String resultado = visitaService.registrarIngreso(
                new VisitaService.Ingreso("1001", null, null, null, "301", true, "abc-123", null));

        assertThat(resultado).contains("P-03").contains("ABC123");
        verify(registroParqueaderoRepository).save(argThat(r ->
                r.getIdParqueadero().equals("p-3") && r.getPlaca().equals("ABC123") && r.getIdApartamento().equals("apto-1")));
    }

    @Test
    void ingresoEnVehiculo_sinParqueaderosLibres_rechaza() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", true)));
        when(parqueaderoRepository.findAll()).thenReturn(List.of(parqueadero("p-1", "P-01", "Ocupado")));
        when(vehiculoRepository.findIdsParqueaderoAsignados()).thenReturn(List.of());
        when(registroParqueaderoRepository.findByFechaHoraSalidaIsNull()).thenReturn(List.of());

        assertThatThrownBy(() -> visitaService.registrarIngreso(
                new VisitaService.Ingreso("1001", null, null, null, "301", true, "ABC123", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No hay parqueaderos libres");
    }

    @Test
    void ingresoEnVehiculo_placaYaAdentro_rechaza() {
        when(residenteRepository.findByNumeroDocumento("1001")).thenReturn(Optional.of(visitante("res-1", true)));
        RegistroParqueaderoVisitanteModel adentro =
                new RegistroParqueaderoVisitanteModel("res-9", "p-1", "2026-10-15 08:00:00", "ABC123");
        when(registroParqueaderoRepository.findByFechaHoraSalidaIsNull()).thenReturn(List.of(adentro));

        assertThatThrownBy(() -> visitaService.registrarIngreso(
                new VisitaService.Ingreso("1001", null, null, null, "301", true, "ABC 123", null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ya está dentro");
    }

    @Test
    void ingreso_documentoLargoOConLetras_ahoraSeAcepta() {
        // Antes residentes.numero_documento era INTEGER: 11 dígitos o un pasaporte fallaban.
        when(residenteRepository.findByNumeroDocumento("AB12345678901")).thenReturn(Optional.empty());

        visitaService.registrarIngreso(aPie("ab-1234567.8901", "Luis", "Gómez"));

        verify(residenteRepository).save(argThat(r -> "AB12345678901".equals(r.getNumeroDocumento())));
    }

    @Test
    void ingreso_documentoMuyCorto_rechaza() {
        assertThatThrownBy(() -> visitaService.registrarIngreso(aPie("12", "Luis", "Gómez")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("entre 4 y 20");
    }

    @Test
    void salida_peaton_registraFechaDeSalida() {
        RegistroVisitanteModel registro = new RegistroVisitanteModel("res-1", "apto-1", "2026-10-15 08:00:00");
        when(registroVisitanteRepository.findById("r-1")).thenReturn(Optional.of(registro));

        visitaService.registrarSalida(VisitaService.Tipo.PEATON, "r-1");

        assertThat(registro.getFechaHoraSalida()).isNotNull();
        verify(registroVisitanteRepository).save(registro);
    }
}
