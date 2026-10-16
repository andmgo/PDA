package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.CargoModel;
import gescazone.demo.domain.model.ConfiguracionCarteraModel;
import gescazone.demo.domain.model.PagoModel;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Cargos, pagos y configuración de la cartera. */
public interface CarteraRepository {

    ConfiguracionCarteraModel configuracion();
    ConfiguracionCarteraModel guardarConfiguracion(ConfiguracionCarteraModel configuracion);

    CargoModel guardarCargo(CargoModel cargo);
    Optional<CargoModel> cargo(String id);
    Optional<CargoModel> cargoDeReserva(String idReserva);
    /** No anulados, de los apartamentos indicados (o de todos si la lista es null). */
    List<CargoModel> cargosVigentes(List<String> idsApartamento);
    boolean existeCuota(String idApartamento, String periodo);

    PagoModel guardarPago(PagoModel pago);
    Optional<PagoModel> pagoPorReferencia(String referencia);
    List<PagoModel> pagosDeCargos(List<String> idsCargo);
    /** Suma de pagos APROBADOS por cargo. */
    Map<String, BigDecimal> pagadoPorCargo(List<String> idsCargo);
}
