package gescazone.demo.application.service;

import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.domain.repository.RegistroParqueaderoVisitanteRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.ApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RegistroParqueaderoVisitanteService {

    @Autowired
    private RegistroParqueaderoVisitanteRepository registroRepository;
    @Autowired
    private ParqueaderoRepository parqueaderoRepository;
    @Autowired
    private ResidenteRepository residenteRepository;
    @Autowired
    private ApartamentoRepository apartamentoRepository;

    public String crear(RegistroParqueaderoVisitanteModel registro, String idResidente,
                        String idParqueadero, String idApartamento) {
        if (registro == null)
            throw new IllegalArgumentException("El registro no puede ser nulo");
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio");
        if (idParqueadero == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio");

        residenteRepository.findById(idResidente)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el residente con ID: " + idResidente));
        parqueaderoRepository.findById(idParqueadero)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el parqueadero con ID: " + idParqueadero));

        if (idApartamento != null)
            apartamentoRepository.findById(idApartamento)
                    .orElseThrow(() -> new IllegalArgumentException("No se encontró el apartamento con ID: " + idApartamento));

        if (registroRepository.existsByIdResidenteAndIdParqueadero(idResidente, idParqueadero))
            throw new IllegalArgumentException("Ya existe un registro para este residente en este parqueadero");

        if (registro.getFechaHoraEntrada() == null || registro.getFechaHoraEntrada().trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de entrada es obligatoria");

        registro.setIdResidente(idResidente);
        registro.setIdParqueadero(idParqueadero);
        registro.setIdApartamento(idApartamento);
        registro.setFechaHoraEntrada(registro.getFechaHoraEntrada().trim());

        if (registro.getPlaca() != null)
            registro.setPlaca(registro.getPlaca().trim().toUpperCase());

        registroRepository.save(registro);
        return "Registro de parqueadero creado con éxito";
    }

    public RegistroParqueaderoVisitanteModel consultar(String idResidente, String idParqueadero) {
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio para la consulta");
        if (idParqueadero == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio para la consulta");
        return registroRepository.findByIdResidenteAndIdParqueadero(idResidente, idParqueadero).orElse(null);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarTodos() {
        return registroRepository.findLatestRegistros();
    }

    public List<RegistroParqueaderoVisitanteModel> consultarPorResidente(String idResidente) {
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio");
        return registroRepository.findByIdResidente(idResidente);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarPorParqueadero(String idParqueadero) {
        if (idParqueadero == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio");
        return registroRepository.findByIdParqueadero(idParqueadero);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarPorApartamento(String idApartamento) {
        if (idApartamento == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");
        return registroRepository.findByIdApartamento(idApartamento);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarActivos() {
        return registroRepository.findByFechaHoraSalidaIsNull();
    }

    public List<RegistroParqueaderoVisitanteModel> consultarActivosPorParqueadero(String idParqueadero) {
        if (idParqueadero == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio");
        return registroRepository.findActivosByIdParqueadero(idParqueadero);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarActivosPorApartamento(String idApartamento) {
        if (idApartamento == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");
        return registroRepository.findActivosByIdApartamento(idApartamento);
    }

    public String registrarSalida(String id, String fechaHoraSalida) {
        if (id == null)
            throw new IllegalArgumentException("El ID del registro es obligatorio");

        if (fechaHoraSalida == null || fechaHoraSalida.trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de salida es obligatoria");

        RegistroParqueaderoVisitanteModel registro = registroRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro"));

        registro.registrarSalida(fechaHoraSalida);
        registroRepository.save(registro);

        return "Salida registrada con éxito";
    }

    public String actualizar(String id, RegistroParqueaderoVisitanteModel registro) {
        if (id == null)
            throw new IllegalArgumentException("El ID del registro es obligatorio");

        if (registro == null)
            throw new IllegalArgumentException("El registro no puede ser nulo");

        RegistroParqueaderoVisitanteModel existente = registroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro"));

        if (registro.getFechaHoraEntrada() == null || registro.getFechaHoraEntrada().trim().isEmpty())
            throw new IllegalArgumentException("La fecha y hora de entrada es obligatoria");

        existente.setIdResidente(registro.getIdResidente());
        existente.setIdParqueadero(registro.getIdParqueadero());
        existente.setIdApartamento(registro.getIdApartamento());
        existente.setFechaHoraEntrada(registro.getFechaHoraEntrada().trim());

        if (registro.getFechaHoraSalida() != null)
            existente.setFechaHoraSalida(registro.getFechaHoraSalida().trim());

        if (registro.getPlaca() != null)
            existente.setPlaca(registro.getPlaca().trim().toUpperCase());

        registroRepository.save(existente);

        return "Registro actualizado con éxito";
    }

    public String eliminar(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID del registro es obligatorio");

        RegistroParqueaderoVisitanteModel registro = registroRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el registro"));

        registroRepository.deleteById(registro.getId());

        return "Registro eliminado con éxito";
    }

    public List<RegistroParqueaderoVisitanteModel> buscarPorPlaca(String placa) {
        if (placa == null || placa.trim().isEmpty())
            throw new IllegalArgumentException("La placa es obligatoria");
        return registroRepository.findByPlacaContainingIgnoreCase(placa.trim());
    }

    public long contarActivosPorParqueadero(String idParqueadero) {
        if (idParqueadero == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio");
        return registroRepository.countActivosByIdParqueadero(idParqueadero);
    }

    public long contarVisitasResidente(String idResidente) {
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio");
        return registroRepository.countByIdResidente(idResidente);
    }

    public List<RegistroParqueaderoVisitanteModel> consultarPorNumeroParqueadero(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio");

        var parqueadero = parqueaderoRepository.findByNumero(numero)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el parqueadero con número: " + numero));

        return registroRepository.findByIdParqueadero(parqueadero.getId());
    }

    public Optional<RegistroParqueaderoVisitanteModel> consultarPorId(String id) {
        if (id == null)
            throw new IllegalArgumentException("El ID es obligatorio");

        return registroRepository.findById(id);
    }
}