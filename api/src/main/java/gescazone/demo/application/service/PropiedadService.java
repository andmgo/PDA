package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.model.VehiculoModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.ParqueaderoRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import gescazone.demo.domain.repository.VehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Propiedad de cada apartamento: quiénes son sus propietarios (Usuarios) y
 * qué vehículos tiene registrados. Es la base de las visitas
 * pre-autorizadas, "mis paquetes" y el lector de placas de portería.
 */
@Service
public class PropiedadService {

    public record Propietario(String id, String numeroDocumento, String nombre, String apellido, String correo) {}

    public record Vehiculo(String id, String placa, String tipo, String marca, String color,
                           String numeroParqueadero, boolean activo) {}

    public record DetalleApartamento(String idApartamento, String numeroApartamento,
                                     List<Propietario> propietarios, List<Vehiculo> vehiculos) {}

    /** Resultado del lector de placas: el vehículo y de quién es. */
    public record ResultadoPlaca(Vehiculo vehiculo, String idApartamento, String numeroApartamento,
                                 List<Propietario> propietarios) {}

    @Autowired private UsuarioApartamentoRepository usuarioApartamentoRepository;
    @Autowired private VehiculoRepository vehiculoRepository;
    @Autowired private ApartamentoRepository apartamentoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private ParqueaderoRepository parqueaderoRepository;

    // ── Consultas ──────────────────────────────────────────────────────────

    public DetalleApartamento detalle(String idApartamento) {
        return armarDetalle(apartamentoExistente(idApartamento));
    }

    /** Apartamentos de la persona logueada (por su número de documento del JWT). */
    public List<DetalleApartamento> deUsuario(String numeroDocumento) {
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(numeroDocumento)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return usuarioApartamentoRepository.findApartamentoIdsByUsuario(usuario.getId()).stream()
                .map(apartamentoRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(this::armarDetalle)
                .toList();
    }

    public ResultadoPlaca buscarPorPlaca(String placa) {
        String normalizada = VehiculoModel.normalizarPlaca(placa);
        if (normalizada == null || normalizada.isBlank())
            throw new ValidationException("La placa es obligatoria");
        VehiculoModel vehiculo = vehiculoRepository.findActivoByPlaca(normalizada)
                .orElseThrow(() -> new NotFoundException("La placa " + normalizada + " no está registrada en el conjunto"));
        ApartamentoModel apartamento = apartamentoExistente(vehiculo.getIdApartamento());
        return new ResultadoPlaca(aVehiculo(vehiculo), apartamento.getId(), apartamento.getNumero(),
                propietariosDe(apartamento.getId()));
    }

    // ── Propietarios ───────────────────────────────────────────────────────

    /** @param identificador número de documento o correo del usuario a vincular */
    public String asignarPropietario(String idApartamento, String identificador) {
        ApartamentoModel apartamento = apartamentoActivo(idApartamento);
        if (identificador == null || identificador.isBlank())
            throw new ValidationException("Ingresa el número de documento o el correo del propietario");

        String valor = identificador.trim();
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(valor)
                .or(() -> usuarioRepository.findByCorreo(valor.toLowerCase(Locale.ROOT)))
                .or(() -> usuarioRepository.findByCorreo(valor))
                .orElseThrow(() -> new NotFoundException(
                        "No hay ningún usuario registrado con el documento o correo " + valor));
        if (!usuario.isActivo())
            throw new ValidationException("El usuario " + usuario.getNumeroDocumento() + " está desactivado");
        if (usuarioApartamentoRepository.existe(usuario.getId(), apartamento.getId()))
            throw new ValidationException(usuario.getNombre() + " " + usuario.getApellido()
                    + " ya es propietario del apartamento " + apartamento.getNumero());

        usuarioApartamentoRepository.asignar(usuario.getId(), apartamento.getId());
        return usuario.getNombre() + " " + usuario.getApellido() + " quedó como propietario del apartamento " + apartamento.getNumero();
    }

    @Transactional
    public String quitarPropietario(String idApartamento, String idUsuario) {
        ApartamentoModel apartamento = apartamentoExistente(idApartamento);
        if (idUsuario == null || !usuarioApartamentoRepository.existe(idUsuario, apartamento.getId()))
            throw new NotFoundException("Ese usuario no es propietario del apartamento " + apartamento.getNumero());
        usuarioApartamentoRepository.quitar(idUsuario, apartamento.getId());
        return "Propietario desvinculado del apartamento " + apartamento.getNumero();
    }

    // ── Vehículos ──────────────────────────────────────────────────────────

    public String registrarVehiculo(String idApartamento, String placa, String tipo,
                                    String marca, String color, String numeroParqueadero) {
        ApartamentoModel apartamento = apartamentoActivo(idApartamento);

        VehiculoModel.Tipo tipoVehiculo;
        try {
            tipoVehiculo = VehiculoModel.Tipo.valueOf(tipo == null ? "" : tipo.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("El tipo de vehículo debe ser CARRO o MOTO");
        }

        VehiculoModel vehiculo = new VehiculoModel(placa, tipoVehiculo, apartamento.getId());
        vehiculoRepository.findActivoByPlaca(vehiculo.getPlaca()).ifPresent(existente -> {
            String numero = apartamentoRepository.findById(existente.getIdApartamento())
                    .map(ApartamentoModel::getNumero).orElse("otro apartamento");
            throw new ValidationException("La placa " + vehiculo.getPlaca() + " ya está registrada en el apartamento " + numero);
        });

        vehiculo.setMarca(textoOpcional(marca));
        vehiculo.setColor(textoOpcional(color));
        if (numeroParqueadero != null && !numeroParqueadero.isBlank()) {
            ParqueaderoModel parqueadero = parqueaderoRepository.findByNumero(numeroParqueadero.trim().toUpperCase(Locale.ROOT))
                    .orElseThrow(() -> new NotFoundException("El parqueadero " + numeroParqueadero + " no existe"));
            vehiculo.setIdParqueadero(parqueadero.getId());
        }

        vehiculoRepository.save(vehiculo);
        return "Vehículo " + vehiculo.getPlaca() + " registrado en el apartamento " + apartamento.getNumero();
    }

    public String desactivarVehiculo(String idVehiculo) {
        if (idVehiculo == null || idVehiculo.isBlank())
            throw new ValidationException("El ID del vehículo es obligatorio");
        VehiculoModel vehiculo = vehiculoRepository.findById(idVehiculo)
                .orElseThrow(() -> new NotFoundException("No existe un vehículo con el ID: " + idVehiculo));
        vehiculo.desactivar();
        vehiculoRepository.save(vehiculo);
        return "Vehículo " + vehiculo.getPlaca() + " desactivado";
    }

    // ── Apoyo ──────────────────────────────────────────────────────────────

    private ApartamentoModel apartamentoExistente(String idApartamento) {
        if (idApartamento == null || idApartamento.isBlank())
            throw new ValidationException("El apartamento es obligatorio");
        return apartamentoRepository.findById(idApartamento)
                .orElseThrow(() -> new NotFoundException("No existe un apartamento con el ID: " + idApartamento));
    }

    private ApartamentoModel apartamentoActivo(String idApartamento) {
        ApartamentoModel apartamento = apartamentoExistente(idApartamento);
        if (!apartamento.isActivo())
            throw new ValidationException("El apartamento " + apartamento.getNumero() + " está desactivado");
        return apartamento;
    }

    private DetalleApartamento armarDetalle(ApartamentoModel apartamento) {
        List<Vehiculo> vehiculos = vehiculoRepository.findByApartamentoId(apartamento.getId()).stream()
                .map(this::aVehiculo).toList();
        return new DetalleApartamento(apartamento.getId(), apartamento.getNumero(),
                propietariosDe(apartamento.getId()), vehiculos);
    }

    private List<Propietario> propietariosDe(String idApartamento) {
        return usuarioApartamentoRepository.findUsuarioIdsByApartamento(idApartamento).stream()
                .map(usuarioRepository::findById)
                .flatMap(java.util.Optional::stream)
                .map(u -> new Propietario(u.getId(), u.getNumeroDocumento(), u.getNombre(), u.getApellido(), u.getCorreo()))
                .toList();
    }

    private Vehiculo aVehiculo(VehiculoModel v) {
        String numeroParqueadero = v.getIdParqueadero() == null ? null
                : parqueaderoRepository.findById(v.getIdParqueadero()).map(ParqueaderoModel::getNumero).orElse(null);
        return new Vehiculo(v.getId(), v.getPlaca(), v.getTipo().name(), v.getMarca(), v.getColor(),
                numeroParqueadero, v.isActivo());
    }

    private String textoOpcional(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
