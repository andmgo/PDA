package gescazone.demo.domain.repository;

import gescazone.demo.domain.model.VehiculoModel;

import java.util.List;
import java.util.Optional;

public interface VehiculoRepository {
    VehiculoModel save(VehiculoModel vehiculo);
    Optional<VehiculoModel> findById(String id);
    /** Solo vehículos activos — una placa puede existir desactivada varias veces. */
    Optional<VehiculoModel> findActivoByPlaca(String placaNormalizada);
    List<VehiculoModel> findByApartamentoId(String idApartamento);
    /** Parqueaderos asignados a algún vehículo activo de residente (no se prestan a visitantes). */
    List<String> findIdsParqueaderoAsignados();
}
