package gescazone.demo.application.service;

import gescazone.demo.application.exception.NotFoundException;
import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.VehiculoModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * Lector de placas de portería (Fase 8). La app lee la placa con la cámara
 * (o el portero la escribe) y en una sola consulta sabe qué hacer:
 * <ol>
 *   <li>VISITANTE_DENTRO: un visitante con esa placa sigue dentro → registrar su salida.</li>
 *   <li>RESIDENTE: vehículo registrado de un apartamento → dejar pasar.</li>
 *   <li>AUTORIZADA: un propietario la autorizó para hoy → registrar el ingreso en un toque.</li>
 *   <li>DESCONOCIDA: nadie la conoce → registrar el ingreso con la placa ya escrita.</li>
 * </ol>
 * El orden importa: si un visitante sigue dentro, lo que toca es su salida.
 */
@Service
public class LectorPlacaService {

    public enum Resultado { VISITANTE_DENTRO, RESIDENTE, AUTORIZADA, DESCONOCIDA }

    /** Solo viene lleno el campo que corresponde al resultado. */
    public record Lectura(String placa, Resultado resultado,
                          PropiedadService.ResultadoPlaca residente,
                          VisitaService.Visita visitaDentro,
                          VisitaAutorizadaService.Autorizacion autorizacion) {}

    /** Mismo rango que acepta portería para placas de visitantes. */
    private static final Pattern PLACA = Pattern.compile("^[A-Z0-9]{4,7}$");

    @Autowired private PropiedadService propiedadService;
    @Autowired private VisitaService visitaService;
    @Autowired private VisitaAutorizadaService visitaAutorizadaService;

    public Lectura consultar(String placaLeida) {
        String placa = VehiculoModel.normalizarPlaca(placaLeida);
        if (placa == null || !PLACA.matcher(placa).matches())
            throw new ValidationException("Placa inválida: escribe letras y números (ej. ABC123)");

        var dentro = visitaService.dentro().stream()
                .filter(v -> placa.equals(v.placa()))
                .findFirst();
        if (dentro.isPresent())
            return new Lectura(placa, Resultado.VISITANTE_DENTRO, null, dentro.get(), null);

        try {
            return new Lectura(placa, Resultado.RESIDENTE, propiedadService.buscarPorPlaca(placa), null, null);
        } catch (NotFoundException noEsDeResidente) {
            // sigue con las autorizaciones
        }

        return visitaAutorizadaService.esperadasHoy().stream()
                .filter(a -> placa.equals(a.placa()))
                .findFirst()
                .map(a -> new Lectura(placa, Resultado.AUTORIZADA, null, null, a))
                .orElseGet(() -> new Lectura(placa, Resultado.DESCONOCIDA, null, null, null));
    }
}
