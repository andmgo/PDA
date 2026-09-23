package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ParqueaderoService {

    @Autowired
    private ParqueaderoRepository parqueaderoRepository;

    public String crear(ParqueaderoModel parqueadero) {
        if (parqueadero == null)
            throw new IllegalArgumentException("El parqueadero no puede ser nulo");

        if (parqueadero.getNumero() == null || parqueadero.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio");

        if (parqueadero.getEstado() == null)
            throw new IllegalArgumentException("El estado del parqueadero es obligatorio");

        if (parqueaderoRepository.existsByNumero(parqueadero.getNumero().trim()))
            throw new IllegalArgumentException("Ya existe un parqueadero con el número: " + parqueadero.getNumero());

        parqueadero.setNumero(parqueadero.getNumero().trim());

        if (parqueadero.getMedidas() != null)
            parqueadero.setMedidas(parqueadero.getMedidas().trim());

        parqueaderoRepository.save(parqueadero);
        return "Parqueadero creado con éxito";
    }

    public ParqueaderoModel consultar(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio para la consulta");

        return parqueaderoRepository.findByNumero(numero.trim()).orElse(null);
    }

    public List<ParqueaderoModel> consultarTodos() {
        return parqueaderoRepository.findAll();
    }

    public String actualizar(ParqueaderoModel parqueadero) {
        if (parqueadero == null)
            throw new IllegalArgumentException("El parqueadero no puede ser nulo");

        if (parqueadero.getId() == null)
            throw new IllegalArgumentException("El ID del parqueadero es obligatorio para actualizar");

        if (parqueaderoRepository.findById(parqueadero.getId()).isEmpty())
            throw new NotFoundException("No existe un parqueadero con el ID: " + parqueadero.getId());

        if (parqueadero.getNumero() == null || parqueadero.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio");

        if (parqueadero.getEstado() == null)
            throw new IllegalArgumentException("El estado del parqueadero es obligatorio");

        Optional<ParqueaderoModel> parqueaderoExistente = parqueaderoRepository.findByNumero(parqueadero.getNumero().trim());
        if (parqueaderoExistente.isPresent() && !parqueaderoExistente.get().getId().equals(parqueadero.getId()))
            throw new IllegalArgumentException("Ya existe otro parqueadero con el número: " + parqueadero.getNumero());

        parqueadero.setNumero(parqueadero.getNumero().trim());

        if (parqueadero.getMedidas() != null)
            parqueadero.setMedidas(parqueadero.getMedidas().trim());

        parqueaderoRepository.save(parqueadero);
        return "Parqueadero actualizado con éxito";
    }

    public String cambiarEstadoActivo(String numero, boolean activo) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio");

        ParqueaderoModel parqueadero = parqueaderoRepository.findByNumero(numero.trim())
                .orElseThrow(() -> new NotFoundException("No existe un parqueadero con el número: " + numero));

        parqueadero.setActivo(activo);
        parqueaderoRepository.save(parqueadero);
        return activo ? "Parqueadero activado con éxito" : "Parqueadero desactivado con éxito";
    }

    public boolean parqueaderoExistente(String numero) {
        if (numero == null || numero.trim().isEmpty())
            return false;
        return parqueaderoRepository.existsByNumero(numero.trim());
    }

    public String setEstado(String numero, String nombreEstado) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio");

        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del estado es obligatorio");

        ParqueaderoModel parqueadero = consultar(numero.trim());
        if (parqueadero == null)
            throw new NotFoundException("No existe un parqueadero con el número: " + numero);

        EstadoModel estado = new EstadoModel();
        estado.setNombreEstado(nombreEstado.trim());
        parqueadero.setEstado(estado);
        parqueaderoRepository.save(parqueadero);
        return "Estado del parqueadero actualizado con éxito";
    }

    public EstadoModel getEstado(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del parqueadero es obligatorio para consultar el estado");

        ParqueaderoModel parqueadero = consultar(numero.trim());
        if (parqueadero == null)
            throw new NotFoundException("No existe un parqueadero con el número: " + numero);

        return parqueadero.getEstado();
    }

    public List<ParqueaderoModel> consultarPorEstado(String nombreEstado) {
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del estado es obligatorio para la consulta");
        return parqueaderoRepository.findByNombreEstado(nombreEstado.trim());
    }

    public List<ParqueaderoModel> consultarPorMedida(String medida) {
        if (medida == null || medida.trim().isEmpty())
            throw new IllegalArgumentException("La medida es obligatoria para la consulta");
        return parqueaderoRepository.findByMedidas(medida.trim());
    }

    public ParqueaderoModel consultarPorId(String id) {
    return parqueaderoRepository.findById(id).orElse(null);
}
}