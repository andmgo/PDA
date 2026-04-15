package gescazone.demo.application.service;

import gescazone.demo.domain.model.RegistroVisitanteModel;
import gescazone.demo.domain.repository.RegistroVisitanteRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.domain.repository.ApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegistroVisitanteService {

    @Autowired
    private RegistroVisitanteRepository registroVisitanteRepository;
    @Autowired
    private ResidenteRepository residenteRepository;
    @Autowired
    private ApartamentoRepository apartamentoRepository;

    public String crear(RegistroVisitanteModel registro, String residenteId, String apartamentoId) {
        if (registro == null)
            throw new IllegalArgumentException("El registro no puede ser nulo");
        if (residenteId == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio");
        if (apartamentoId == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");

        residenteRepository.findById(residenteId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el residente con ID: " + residenteId));
        apartamentoRepository.findById(apartamentoId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el apartamento con ID: " + apartamentoId));

        if (registroVisitanteRepository.existsByIdResidenteAndIdApartamento(residenteId, apartamentoId))
            throw new IllegalArgumentException("Ya existe un registro para este residente en este apartamento");

        if (registro.getFechaHoraEntrada() == null || registro.getFechaHoraEntrada().trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de entrada es obligatoria");

        registro.setIdResidente(residenteId);
        registro.setIdApartamento(apartamentoId);
        registro.setFechaHoraEntrada(registro.getFechaHoraEntrada().trim());

        registroVisitanteRepository.save(registro);
        return "Registro de visitante creado con éxito";
    }

    public RegistroVisitanteModel consultar(String residenteId, String apartamentoId) {
        if (residenteId == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio para la consulta");
        if (apartamentoId == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio para la consulta");
        return registroVisitanteRepository.findByIdResidenteAndIdApartamento(residenteId, apartamentoId).orElse(null);
    }

    public List<RegistroVisitanteModel> consultarTodos() {
        return registroVisitanteRepository.findLatestRegistros();
    }

    public List<RegistroVisitanteModel> consultarPorIdResidente(String idResidente) {
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio para la consulta");
        return registroVisitanteRepository.findByIdResidente(idResidente);
    }

    public List<RegistroVisitanteModel> consultarPorIdApartamento(String idApartamento) {
        if (idApartamento == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio para la consulta");
        return registroVisitanteRepository.findByIdApartamento(idApartamento);
    }

    public List<RegistroVisitanteModel> consultarVisitantesActivos() {
        return registroVisitanteRepository.findByFechaHoraSalidaIsNull();
    }

    public List<RegistroVisitanteModel> consultarVisitantesActivosPorApartamento(String idApartamento) {
        if (idApartamento == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");
        return registroVisitanteRepository.findVisitantesActivosByIdApartamento(idApartamento);
    }

    public String registrarSalida(String id, String fechaHoraSalida) {
        if (id == null)
            throw new IllegalArgumentException("El ID es obligatorio");

        if (fechaHoraSalida == null || fechaHoraSalida.trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de salida es obligatoria");

        RegistroVisitanteModel registro = registroVisitanteRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro"));

        registro.registrarSalida(fechaHoraSalida);
        registroVisitanteRepository.save(registro);

        return "Salida registrada con éxito";
    }
    
    public String actualizar(String id, RegistroVisitanteModel registro) {
        if (id == null)
            throw new IllegalArgumentException("El ID es obligatorio");

        RegistroVisitanteModel existente = registroVisitanteRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro"));

        if (registro.getFechaHoraEntrada() != null)
            existente.setFechaHoraEntrada(registro.getFechaHoraEntrada().trim());

        if (registro.getFechaHoraSalida() != null)
            existente.setFechaHoraSalida(registro.getFechaHoraSalida().trim());

        registroVisitanteRepository.save(existente);

        return "Registro de visitante actualizado con éxito";
    }

    public String eliminar(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID es obligatorio");

        if (!registroVisitanteRepository.existsById(id))
            throw new IllegalArgumentException("No se encontró el registro");

        registroVisitanteRepository.deleteById(id);

        return "Registro eliminado con éxito";
    }

    public long contarVisitantesActivosEnApartamento(String idApartamento) {
        if (idApartamento == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");
        return registroVisitanteRepository.countVisitantesActivosByIdApartamento(idApartamento);
    }

    public List<RegistroVisitanteModel> consultarPorNumeroApartamento(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del apartamento es obligatorio");

        var apartamento = apartamentoRepository.findByNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el apartamento con número: " + numero));

        return registroVisitanteRepository.findByIdApartamento(apartamento.getId());
    }

    public Optional<RegistroVisitanteModel> consultarPorId(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID es obligatorio");

        return registroVisitanteRepository.findById(id);
    }
}