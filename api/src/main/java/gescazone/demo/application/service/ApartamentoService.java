package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApartamentoService {

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    public String crear(ApartamentoModel apartamento) {
        if (apartamento == null)
            throw new IllegalArgumentException("El apartamento no puede ser nulo");

        if (apartamento.getNumero() == null || apartamento.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del apartamento es obligatorio");

        if (apartamento.getTipoOcupacion() == null)
            throw new IllegalArgumentException("El tipo de ocupación del apartamento es obligatorio");

        if (apartamento.getEstadoCuenta() == null)
            throw new IllegalArgumentException("El estado de cuenta del apartamento es obligatorio");

        if (apartamentoRepository.existsByNumero(apartamento.getNumero().trim()))
            throw new IllegalArgumentException("Ya existe un apartamento con el número: " + apartamento.getNumero());

        apartamento.setNumero(apartamento.getNumero().trim());

        if (apartamento.getMedidas() != null)
            apartamento.setMedidas(apartamento.getMedidas().trim());

        apartamentoRepository.save(apartamento);
        return "Apartamento creado con éxito";
    }

    public ApartamentoModel consultar(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del apartamento es obligatorio para la consulta");

        return apartamentoRepository.findByNumero(numero.trim()).orElse(null);
    }

    public List<ApartamentoModel> consultarTodos() {
        return apartamentoRepository.findAll();
    }

    public String actualizar(ApartamentoModel apartamento) {
        if (apartamento == null)
            throw new IllegalArgumentException("El apartamento no puede ser nulo");

        if (apartamento.getId() == null)
            throw new IllegalArgumentException("El ID del apartamento es obligatorio para actualizar");

        if (apartamentoRepository.findById(apartamento.getId()).isEmpty())
            throw new NotFoundException("No existe un apartamento con el ID: " + apartamento.getId());

        if (apartamento.getNumero() == null || apartamento.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del apartamento es obligatorio");

        if (apartamento.getTipoOcupacion() == null)
            throw new IllegalArgumentException("El tipo de ocupación del apartamento es obligatorio");

        if (apartamento.getEstadoCuenta() == null)
            throw new IllegalArgumentException("El estado de cuenta del apartamento es obligatorio");

        Optional<ApartamentoModel> apartamentoExistente = apartamentoRepository.findByNumero(apartamento.getNumero().trim());
        if (apartamentoExistente.isPresent() && !apartamentoExistente.get().getId().equals(apartamento.getId()))
            throw new IllegalArgumentException("Ya existe otro apartamento con el número: " + apartamento.getNumero());

        apartamento.setNumero(apartamento.getNumero().trim());

        if (apartamento.getMedidas() != null)
            apartamento.setMedidas(apartamento.getMedidas().trim());

        apartamentoRepository.save(apartamento);
        return "Apartamento actualizado con éxito";
    }

    public String cambiarEstado(String numero, boolean activo) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del apartamento es obligatorio");

        ApartamentoModel apartamento = apartamentoRepository.findByNumero(numero.trim())
                .orElseThrow(() -> new NotFoundException("No existe un apartamento con el número: " + numero));

        apartamento.setActivo(activo);
        apartamentoRepository.save(apartamento);
        return activo ? "Apartamento activado con éxito" : "Apartamento desactivado con éxito";
    }

    public boolean apartamentoExistente(String numeroApto) {
        if (numeroApto == null || numeroApto.trim().isEmpty())
            return false;
        return apartamentoRepository.existsByNumero(numeroApto.trim());
    }

    public List<ApartamentoModel> consultarPorTipoOcupacion(String nombreTipoOcupacion) {
        if (nombreTipoOcupacion == null)
            throw new IllegalArgumentException("El tipo de ocupación es obligatorio para la consulta");
        return apartamentoRepository.findByNombreTipoOcupacion(nombreTipoOcupacion);
    }

    public List<ApartamentoModel> consultarPorEstadoCuenta(String nombreEstadoCuenta) {
        if (nombreEstadoCuenta == null)
            throw new IllegalArgumentException("El estado de cuenta es obligatorio para la consulta");
        return apartamentoRepository.findByNombreEstadoCuenta(nombreEstadoCuenta);
    }
}