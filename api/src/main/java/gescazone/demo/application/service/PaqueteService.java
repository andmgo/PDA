package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.PaqueteModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.PaqueteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaqueteService {

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    public String registrar(String idApartamento, String nombreReceptor, String cedulaReceptor) {
        if (idApartamento == null || idApartamento.isBlank())
            throw new IllegalArgumentException("El apartamento es obligatorio");
        if (nombreReceptor == null || nombreReceptor.isBlank())
            throw new IllegalArgumentException("El nombre de quien recibe el paquete es obligatorio");
        if (cedulaReceptor == null || cedulaReceptor.isBlank())
            throw new IllegalArgumentException("La cédula de quien recibe el paquete es obligatoria");

        apartamentoRepository.findById(idApartamento)
                .orElseThrow(() -> new NotFoundException("No existe un apartamento con el ID: " + idApartamento));

        PaqueteModel paquete = new PaqueteModel(idApartamento, nombreReceptor.trim(), cedulaReceptor.trim());
        paqueteRepository.save(paquete);
        return "Paquete registrado exitosamente";
    }

    public String marcarEntregado(String idPaquete) {
        if (idPaquete == null)
            throw new IllegalArgumentException("El ID del paquete es obligatorio");
        PaqueteModel paquete = paqueteRepository.findById(idPaquete)
                .orElseThrow(() -> new NotFoundException("No existe un paquete con el ID: " + idPaquete));
        paquete.marcarEntregado();
        paqueteRepository.save(paquete);
        return "Paquete marcado como entregado";
    }

    public List<PaqueteModel> consultarTodos() {
        return paqueteRepository.findAll();
    }

    public List<PaqueteModel> consultarPendientes() {
        return paqueteRepository.findByEntregadoFalse();
    }

    public List<PaqueteModel> consultarPorApartamento(String idApartamento) {
        if (idApartamento == null || idApartamento.isBlank())
            throw new IllegalArgumentException("El ID del apartamento es obligatorio");
        return paqueteRepository.findByApartamentoId(idApartamento);
    }
}
