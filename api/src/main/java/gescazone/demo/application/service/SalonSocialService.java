package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.EstadoModel;
import gescazone.demo.domain.repository.SalonSocialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SalonSocialService {

    @Autowired
    private SalonSocialRepository salonSocialRepository;

    public String crear(SalonSocialModel salonSocial) {
        if (salonSocial == null)
            throw new IllegalArgumentException("El salón social no puede ser nulo");
        if (salonSocial.getNumero() == null || salonSocial.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio");
        if (salonSocial.getEstado() == null)
            throw new IllegalArgumentException("El estado del salón social es obligatorio");
        if (salonSocialRepository.existsByNumero(salonSocial.getNumero().trim()))
            throw new IllegalArgumentException("Ya existe un salón social con el número: " + salonSocial.getNumero());

        salonSocial.setNumero(salonSocial.getNumero().trim());
        if (salonSocial.getMedidas() != null)
            salonSocial.setMedidas(salonSocial.getMedidas().trim());

        salonSocialRepository.save(salonSocial);
        return "Salón social creado con éxito";
    }

    public SalonSocialModel consultar(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio para la consulta");
        return salonSocialRepository.findByNumero(numero.trim()).orElse(null);
    }

    public List<SalonSocialModel> consultarTodos() {
        return salonSocialRepository.findAll();
    }

    public String actualizar(SalonSocialModel salonSocial) {
        if (salonSocial == null)
            throw new IllegalArgumentException("El salón social no puede ser nulo");
        if (salonSocial.getId() == null)
            throw new IllegalArgumentException("El ID del salón social es obligatorio para actualizar");
        if (salonSocialRepository.findById(salonSocial.getId()).isEmpty())
            throw new NotFoundException("No existe un salón social con el ID: " + salonSocial.getId());
        if (salonSocial.getNumero() == null || salonSocial.getNumero().trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio");
        if (salonSocial.getEstado() == null)
            throw new IllegalArgumentException("El estado del salón social es obligatorio");

        Optional<SalonSocialModel> salonExistente = salonSocialRepository.findByNumero(salonSocial.getNumero().trim());
        if (salonExistente.isPresent() && !salonExistente.get().getId().equals(salonSocial.getId()))
            throw new IllegalArgumentException("Ya existe otro salón social con el número: " + salonSocial.getNumero());

        salonSocial.setNumero(salonSocial.getNumero().trim());
        if (salonSocial.getMedidas() != null)
            salonSocial.setMedidas(salonSocial.getMedidas().trim());

        salonSocialRepository.save(salonSocial);
        return "Salón social actualizado con éxito";
    }

    public String eliminar(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio para eliminar");
        if (!salonSocialRepository.existsByNumero(numero.trim()))
            throw new NotFoundException("No existe un salón social con el número: " + numero);
        salonSocialRepository.deleteByNumero(numero.trim());
        return "Salón social eliminado con éxito";
    }

    public boolean salonSocialExistente(String numero) {
        if (numero == null || numero.trim().isEmpty()) return false;
        return salonSocialRepository.existsByNumero(numero.trim());
    }

    public String setEstado(String numero, String nombreEstado) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio");
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del estado es obligatorio");

        SalonSocialModel salon = consultar(numero.trim());
        if (salon == null)
            throw new NotFoundException("No existe un salón social con el número: " + numero);

        EstadoModel estado = new EstadoModel();
        estado.setNombreEstado(nombreEstado.trim());
        salon.setEstado(estado);
        salonSocialRepository.save(salon);
        return "Estado del salón social actualizado con éxito";
    }

    public EstadoModel getEstado(String numero) {
        if (numero == null || numero.trim().isEmpty())
            throw new IllegalArgumentException("El número del salón social es obligatorio para consultar el estado");
        SalonSocialModel salon = consultar(numero.trim());
        if (salon == null)
            throw new NotFoundException("No existe un salón social con el número: " + numero);
        return salon.getEstado();
    }

    public List<SalonSocialModel> consultarPorEstado(String nombreEstado) {
        if (nombreEstado == null || nombreEstado.trim().isEmpty())
            throw new IllegalArgumentException("El nombre del estado es obligatorio para la consulta");
        return salonSocialRepository.findByNombreEstado(nombreEstado.trim());
    }

    public List<SalonSocialModel> consultarPorMedida(String medida) {
        if (medida == null || medida.trim().isEmpty())
            throw new IllegalArgumentException("La medida es obligatoria para la consulta");
        return salonSocialRepository.findByMedidas(medida.trim());
    }
}