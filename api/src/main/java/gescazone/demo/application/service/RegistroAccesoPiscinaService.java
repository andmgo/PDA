package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.RegistroAccesoPiscinaModel;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.RegistroAccesoPiscinaRepository;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ResidenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistroAccesoPiscinaService {

    @Autowired
    private RegistroAccesoPiscinaRepository registroRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @Autowired
    private ResidenteRepository residenteRepository;

    public String registrarIngreso(String numeroApartamento, Integer numeroDocumento) {
        ApartamentoModel apartamento = apartamentoRepository.findByNumero(numeroApartamento)
                .orElseThrow(() -> new NotFoundException("No existe el apartamento: " + numeroApartamento));

        ResidenteModel residente = residenteRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new NotFoundException("No existe el residente con documento: " + numeroDocumento));

        RegistroAccesoPiscinaModel registro = new RegistroAccesoPiscinaModel(
                apartamento.getId(), residente.getId());
        registroRepository.save(registro);

        return "Acceso registrado exitosamente para el apartamento " + numeroApartamento;
    }

    public List<RegistroAccesoPiscinaModel> consultarTodos() {
        return registroRepository.findAllOrderByFechaHoraDesc();
    }

    public List<RegistroAccesoPiscinaModel> consultarRegistrosHoy() {
        return registroRepository.findRegistrosHoy();
    }

    public List<RegistroAccesoPiscinaModel> consultarPorApartamento(String numeroApartamento) {
        return registroRepository.findByApartamentoNumero(numeroApartamento);
    }

    public List<RegistroAccesoPiscinaModel> consultarPorResidente(Integer numeroDocumento) {
        return registroRepository.findByResidenteNumeroDocumento(numeroDocumento);
    }

    public boolean verificarAcceso(String numeroApartamento) {
        if (!apartamentoRepository.existsByNumero(numeroApartamento))
            throw new NotFoundException("No existe el apartamento: " + numeroApartamento);
        return true;
    }

    public String eliminar(String idRegistro) {
        if (!registroRepository.existsById(idRegistro))
            throw new NotFoundException("No existe el registro con ID: " + idRegistro);
        registroRepository.deleteById(idRegistro);
        return "Registro eliminado exitosamente";
    }

    public String modificar(String id, String idResidente, String idApartamento) {
    RegistroAccesoPiscinaModel registro = registroRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("No existe el registro con ID: " + id));

    // Validar que existan
    residenteRepository.findById(idResidente)
            .orElseThrow(() -> new NotFoundException("Residente no encontrado"));

    apartamentoRepository.findById(idApartamento)
            .orElseThrow(() -> new NotFoundException("Apartamento no encontrado"));

    registro.setIdResidente(idResidente);
    registro.setIdApartamento(idApartamento);

    registroRepository.save(registro);

    return "Registro modificado exitosamente";
}
}