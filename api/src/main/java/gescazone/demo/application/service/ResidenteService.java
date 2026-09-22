package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.repository.ResidenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ResidenteService {

    @Autowired
    private ResidenteRepository residenteRepository;

    public String crear(ResidenteModel residente) {
    if (residente == null)
        throw new IllegalArgumentException("El residente no puede ser nulo");
    if (residente.getTipoDocumento() == null || residente.getTipoDocumento().getNombreTipoDocumento() == null
            || residente.getTipoDocumento().getNombreTipoDocumento().trim().isEmpty())
        throw new IllegalArgumentException("El tipo de documento es obligatorio");
    if (residente.getNumeroDocumento() == null)
        throw new IllegalArgumentException("El número de documento es obligatorio");
    if (residente.getNombre() == null || residente.getNombre().trim().isEmpty())
        throw new IllegalArgumentException("El nombre es obligatorio");
    if (residente.getApellido() == null || residente.getApellido().trim().isEmpty())
        throw new IllegalArgumentException("El apellido es obligatorio");
    if (residente.getCelular() == null)
        throw new IllegalArgumentException("El celular es obligatorio");
    if (residente.getTipoResidente() == null || residente.getTipoResidente().getNombreTipoResidente() == null
            || residente.getTipoResidente().getNombreTipoResidente().trim().isEmpty())
        throw new IllegalArgumentException("El tipo de residente es obligatorio");

    if (residenteRepository.existsByNumeroDocumento(residente.getNumeroDocumento()))
        throw new IllegalArgumentException("Ya existe un residente con el documento: " + residente.getNumeroDocumento());

    residente.setNombre(residente.getNombre().trim());
    residente.setApellido(residente.getApellido().trim());
    residente.getTipoDocumento().setNombreTipoDocumento(residente.getTipoDocumento().getNombreTipoDocumento().trim());
    residente.getTipoResidente().setNombreTipoResidente(residente.getTipoResidente().getNombreTipoResidente().trim());

    residenteRepository.save(residente);
    return "Residente registrado con éxito";
}
    public ResidenteModel consultar(Integer numeroDocumento) {
        if (numeroDocumento == null)
            throw new IllegalArgumentException("El número de documento es obligatorio para la consulta");
        return residenteRepository.findByNumeroDocumento(numeroDocumento).orElse(null);
    }

    public ResidenteModel consultarPorId(String idResidente) {
        if (idResidente == null)
            throw new IllegalArgumentException("El ID del residente es obligatorio para la consulta");
        return residenteRepository.findById(idResidente).orElse(null);
    }

    public List<ResidenteModel> consultarTodos() {
        return residenteRepository.findAll();
    }

    public String actualizar(ResidenteModel residente) {
    if (residente == null)
        throw new IllegalArgumentException("El residente no puede ser nulo");
    if (residente.getId() == null)
        throw new IllegalArgumentException("El ID del residente es obligatorio para actualizar");
    if (residenteRepository.findById(residente.getId()).isEmpty())
        throw new NotFoundException("No existe un residente con el ID: " + residente.getId());
    if (residente.getTipoDocumento() == null || residente.getTipoDocumento().getNombreTipoDocumento() == null
            || residente.getTipoDocumento().getNombreTipoDocumento().trim().isEmpty())
        throw new IllegalArgumentException("El tipo de documento es obligatorio");
    if (residente.getNumeroDocumento() == null)
        throw new IllegalArgumentException("El número de documento es obligatorio");
    if (residente.getNombre() == null || residente.getNombre().trim().isEmpty())
        throw new IllegalArgumentException("El nombre es obligatorio");
    if (residente.getApellido() == null || residente.getApellido().trim().isEmpty())
        throw new IllegalArgumentException("El apellido es obligatorio");
    if (residente.getCelular() == null)
        throw new IllegalArgumentException("El celular es obligatorio");
    if (residente.getTipoResidente() == null || residente.getTipoResidente().getNombreTipoResidente() == null
            || residente.getTipoResidente().getNombreTipoResidente().trim().isEmpty())
        throw new IllegalArgumentException("El tipo de residente es obligatorio");

    Optional<ResidenteModel> residenteExistente = residenteRepository.findByNumeroDocumento(residente.getNumeroDocumento());
    if (residenteExistente.isPresent() && !residenteExistente.get().getId().equals(residente.getId()))
        throw new IllegalArgumentException("Ya existe otro residente con el documento: " + residente.getNumeroDocumento());

    residente.setNombre(residente.getNombre().trim());
    residente.setApellido(residente.getApellido().trim());
    residente.getTipoDocumento().setNombreTipoDocumento(residente.getTipoDocumento().getNombreTipoDocumento().trim());
    residente.getTipoResidente().setNombreTipoResidente(residente.getTipoResidente().getNombreTipoResidente().trim());

    residenteRepository.save(residente);
    return "Residente actualizado con éxito";
}

    public String eliminar(Integer numeroDocumento) {
        if (numeroDocumento == null)
            throw new IllegalArgumentException("El número de documento es obligatorio para eliminar");
        if (!residenteRepository.existsByNumeroDocumento(numeroDocumento))
            throw new NotFoundException("No existe un residente con el documento: " + numeroDocumento);
        residenteRepository.deleteByNumeroDocumento(numeroDocumento);
        return "Residente eliminado con éxito";
    }

    public boolean residenteExistente(Integer numeroDocumento) {
        if (numeroDocumento == null) return false;
        return residenteRepository.existsByNumeroDocumento(numeroDocumento);
    }

    public List<ResidenteModel> consultarPorTipo(String nombreTipoResidente) {
        if (nombreTipoResidente == null)
            throw new IllegalArgumentException("El tipo de residente es obligatorio para la consulta");
        return residenteRepository.findByNombreTipoResidente(nombreTipoResidente);
    }

    public List<ResidenteModel> consultarPorTipoDeDocumento(String nombreTipoDocumento) {
        if (nombreTipoDocumento == null)
            throw new IllegalArgumentException("El tipo de documento es obligatorio para la consulta");
        return residenteRepository.findByNombreTipoDocumento(nombreTipoDocumento);
    }

    public List<ResidenteModel> buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty())
            throw new IllegalArgumentException("El nombre es obligatorio para la búsqueda");
        return residenteRepository.findByNombreContainingIgnoreCase(nombre.trim());
    }

    public List<ResidenteModel> buscarPorApellido(String apellido) {
        if (apellido == null || apellido.trim().isEmpty())
            throw new IllegalArgumentException("El apellido es obligatorio para la búsqueda");
        return residenteRepository.findByApellidoContainingIgnoreCase(apellido.trim());
    }

    public List<ResidenteModel> buscarPorNombreOApellido(String termino) {
        if (termino == null || termino.trim().isEmpty())
            throw new IllegalArgumentException("El término de búsqueda es obligatorio");
        return residenteRepository.findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCase(termino.trim(), termino.trim());
    }
}