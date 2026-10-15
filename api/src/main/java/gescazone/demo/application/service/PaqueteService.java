package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.port.NotificadorCorreo;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.PaqueteModel;
import gescazone.demo.domain.model.UsuarioModel;
import gescazone.demo.domain.repository.ApartamentoRepository;
import gescazone.demo.domain.repository.PaqueteRepository;
import gescazone.demo.domain.repository.UsuarioApartamentoRepository;
import gescazone.demo.domain.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PaqueteService {

    /** Paquete como lo ven portería y el propietario (con nombres legibles, no IDs). */
    public record Paquete(String id, String idApartamento, String numeroApartamento, String nombreReceptor,
                          String cedulaReceptor, String descripcion, LocalDateTime fechaHoraLlegada,
                          boolean entregado, LocalDateTime fechaHoraEntrega, String entregadoA,
                          String recibidoPor, String entregadoPor) {}

    @Autowired
    private PaqueteRepository paqueteRepository;

    @Autowired
    private ApartamentoRepository apartamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioApartamentoRepository usuarioApartamentoRepository;

    @Autowired
    private NotificadorCorreo notificadorCorreo;

    public String registrar(String idApartamento, String nombreReceptor, String cedulaReceptor) {
        return registrar(idApartamento, nombreReceptor, cedulaReceptor, null, null);
    }

    /**
     * @param descripcion          "Caja de Amazon", "Sobre"… (opcional)
     * @param documentoRegistrador funcionario que lo recibe en portería (sale del JWT)
     */
    public String registrar(String idApartamento, String nombreReceptor, String cedulaReceptor,
                            String descripcion, String documentoRegistrador) {
        if (idApartamento == null || idApartamento.isBlank())
            throw new IllegalArgumentException("El apartamento es obligatorio");
        if (nombreReceptor == null || nombreReceptor.isBlank())
            throw new IllegalArgumentException("El nombre de quien recibe el paquete es obligatorio");
        if (cedulaReceptor == null || cedulaReceptor.isBlank())
            throw new IllegalArgumentException("La cédula de quien recibe el paquete es obligatoria");

        ApartamentoModel apartamento = RequiereActivo.apartamento(apartamentoRepository.findById(idApartamento)
                .orElseThrow(() -> new NotFoundException("No existe un apartamento con el ID: " + idApartamento)));

        PaqueteModel paquete = new PaqueteModel(idApartamento, nombreReceptor.trim(), cedulaReceptor.trim());
        paquete.setDescripcion(descripcion == null || descripcion.isBlank() ? null : descripcion.trim());
        paquete.setIdRegistradoPor(idUsuario(documentoRegistrador));
        paqueteRepository.save(paquete);

        // Aviso por correo a los propietarios del apartamento (cortesía, no bloquea).
        for (String idPropietario : usuarioApartamentoRepository.findUsuarioIdsByApartamento(idApartamento)) {
            usuarioRepository.findById(idPropietario).filter(UsuarioModel::isActivo).ifPresent(u ->
                    notificadorCorreo.avisarPaqueteLlegado(u.getCorreo(), u.getNombre(), apartamento.getNumero(),
                            paquete.getDescripcion()));
        }
        return "Paquete registrado exitosamente";
    }

    /** Portería escribe el número del apartamento ("301"), no su ID interno. */
    public String registrarPorNumeroApartamento(String numeroApartamento, String nombreReceptor, String cedulaReceptor) {
        return registrarPorNumeroApartamento(numeroApartamento, nombreReceptor, cedulaReceptor, null, null);
    }

    public String registrarPorNumeroApartamento(String numeroApartamento, String nombreReceptor, String cedulaReceptor,
                                                String descripcion, String documentoRegistrador) {
        if (numeroApartamento == null || numeroApartamento.isBlank())
            throw new IllegalArgumentException("El apartamento es obligatorio");
        String idApartamento = apartamentoRepository.findByNumero(numeroApartamento.trim().toUpperCase())
                .orElseThrow(() -> new NotFoundException("No existe el apartamento " + numeroApartamento.trim()))
                .getId();
        return registrar(idApartamento, nombreReceptor, cedulaReceptor, descripcion, documentoRegistrador);
    }

    public String marcarEntregado(String idPaquete) {
        return marcarEntregado(idPaquete, null, null);
    }

    /**
     * @param entregadoA           quién lo retira (vacío = el destinatario)
     * @param documentoFuncionario funcionario que lo entrega (sale del JWT)
     */
    public String marcarEntregado(String idPaquete, String entregadoA, String documentoFuncionario) {
        if (idPaquete == null)
            throw new IllegalArgumentException("El ID del paquete es obligatorio");
        PaqueteModel paquete = paqueteRepository.findById(idPaquete)
                .orElseThrow(() -> new NotFoundException("No existe un paquete con el ID: " + idPaquete));
        paquete.marcarEntregado(entregadoA, idUsuario(documentoFuncionario));
        paqueteRepository.save(paquete);
        return "Paquete entregado a " + paquete.getEntregadoA();
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

    /** "Mis paquetes" (Fase 7): los de los apartamentos vinculados a la cuenta, máximo 100. */
    public List<Paquete> mios(String documentoUsuario) {
        UsuarioModel usuario = usuarioRepository.findByNumeroDocumento(documentoUsuario)
                .orElseThrow(() -> new NotFoundException("Usuario autenticado no encontrado"));
        List<String> apartamentos = usuarioApartamentoRepository.findApartamentoIdsByUsuario(usuario.getId());
        return vista(paqueteRepository.findByApartamentoIds(apartamentos)).stream().limit(100).toList();
    }

    /**
     * Pendientes primero (los más antiguos arriba: son los que llevan más
     * tiempo esperando), luego entregados del más reciente al más antiguo.
     */
    public List<Paquete> vista(List<PaqueteModel> paquetes) {
        Map<String, String> apartamentos = new HashMap<>();
        Map<String, String> usuarios = new HashMap<>();
        Comparator<PaqueteModel> orden = Comparator.comparing(PaqueteModel::isEntregado)
                .thenComparing((a, b) -> a.isEntregado()
                        ? Objects.compare(b.getFechaHoraEntrega(), a.getFechaHoraEntrega(), Comparator.nullsLast(Comparator.naturalOrder()))
                        : a.getFechaHoraLlegada().compareTo(b.getFechaHoraLlegada()));
        return paquetes.stream().sorted(orden).map(p -> new Paquete(
                p.getId(), p.getIdApartamento(),
                apartamentos.computeIfAbsent(p.getIdApartamento(), this::numeroApartamento),
                p.getNombreReceptor(), p.getCedulaReceptor(), p.getDescripcion(), p.getFechaHoraLlegada(),
                p.isEntregado(), p.getFechaHoraEntrega(), p.getEntregadoA(),
                nombreUsuario(p.getIdRegistradoPor(), usuarios), nombreUsuario(p.getIdEntregadoPor(), usuarios)
        )).toList();
    }

    /** Número legible del apartamento de un paquete (para mostrarlo en pantalla). */
    public String numeroApartamento(String idApartamento) {
        return apartamentoRepository.findById(idApartamento).map(ApartamentoModel::getNumero).orElse("—");
    }

    private String nombreUsuario(String idUsuario, Map<String, String> cache) {
        if (idUsuario == null) return null;
        return cache.computeIfAbsent(idUsuario, id -> usuarioRepository.findById(id)
                .map(u -> u.getNombre() + " " + u.getApellido()).orElse(null));
    }

    private String idUsuario(String documento) {
        if (documento == null || documento.isBlank()) return null;
        return usuarioRepository.findByNumeroDocumento(documento).map(UsuarioModel::getId).orElse(null);
    }
}
