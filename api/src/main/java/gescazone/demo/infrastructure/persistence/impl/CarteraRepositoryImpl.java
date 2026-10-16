package gescazone.demo.infrastructure.persistence.impl;

import gescazone.demo.domain.model.CargoModel;
import gescazone.demo.domain.model.ConfiguracionCarteraModel;
import gescazone.demo.domain.model.PagoModel;
import gescazone.demo.domain.repository.CarteraRepository;
import gescazone.demo.infrastructure.persistence.entity.CargoEntity;
import gescazone.demo.infrastructure.persistence.entity.ConfiguracionCarteraEntity;
import gescazone.demo.infrastructure.persistence.entity.PagoEntity;
import gescazone.demo.infrastructure.persistence.jpa.CargoJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.ConfiguracionCarteraJpaRepository;
import gescazone.demo.infrastructure.persistence.jpa.PagoJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CarteraRepositoryImpl implements CarteraRepository {

    @Autowired private CargoJpaRepository cargoJpa;
    @Autowired private PagoJpaRepository pagoJpa;
    @Autowired private ConfiguracionCarteraJpaRepository configuracionJpa;

    private static String texto(UUID id) { return id == null ? null : id.toString(); }
    private static UUID uuid(String id) { return id == null ? null : UUID.fromString(id); }
    private static List<UUID> uuids(List<String> ids) { return ids.stream().map(UUID::fromString).toList(); }

    // ── Configuración ──

    @Override
    public ConfiguracionCarteraModel configuracion() {
        ConfiguracionCarteraEntity e = configuracionJpa.findById((short) 1).orElseGet(ConfiguracionCarteraEntity::new);
        ConfiguracionCarteraModel m = new ConfiguracionCarteraModel();
        m.setCuotaAdministracion(e.getCuotaAdministracion() == null ? BigDecimal.ZERO : e.getCuotaAdministracion());
        m.setValorReserva(e.getValorReserva() == null ? BigDecimal.ZERO : e.getValorReserva());
        m.setDiaVencimiento(e.getDiaVencimiento() == 0 ? 10 : e.getDiaVencimiento());
        return m;
    }

    @Override
    public ConfiguracionCarteraModel guardarConfiguracion(ConfiguracionCarteraModel m) {
        ConfiguracionCarteraEntity e = new ConfiguracionCarteraEntity();
        e.setId((short) 1);
        e.setCuotaAdministracion(m.getCuotaAdministracion());
        e.setValorReserva(m.getValorReserva());
        e.setDiaVencimiento((short) m.getDiaVencimiento());
        configuracionJpa.save(e);
        return configuracion();
    }

    // ── Cargos ──

    private CargoModel aModelo(CargoEntity e) {
        CargoModel m = new CargoModel();
        m.setId(texto(e.getId()));
        m.setIdApartamento(texto(e.getApartamentoId()));
        m.setConcepto(CargoModel.Concepto.valueOf(e.getConcepto()));
        m.setDescripcion(e.getDescripcion());
        m.setPeriodo(e.getPeriodo());
        m.setIdReserva(texto(e.getReservaId()));
        m.setValor(e.getValor());
        m.setFechaVencimiento(e.getFechaVencimiento());
        m.setAnulado(e.isAnulado());
        m.setFechaCreacion(e.getFechaCreacion());
        return m;
    }

    @Override
    public CargoModel guardarCargo(CargoModel m) {
        CargoEntity e = new CargoEntity();
        e.setId(uuid(m.getId()));
        e.setApartamentoId(uuid(m.getIdApartamento()));
        e.setConcepto(m.getConcepto().name());
        e.setDescripcion(m.getDescripcion());
        e.setPeriodo(m.getPeriodo());
        e.setReservaId(uuid(m.getIdReserva()));
        e.setValor(m.getValor());
        e.setFechaVencimiento(m.getFechaVencimiento());
        e.setAnulado(m.isAnulado());
        e.setFechaCreacion(m.getFechaCreacion());
        return aModelo(cargoJpa.save(e));
    }

    @Override
    public Optional<CargoModel> cargo(String id) {
        return cargoJpa.findById(UUID.fromString(id)).map(this::aModelo);
    }

    @Override
    public Optional<CargoModel> cargoDeReserva(String idReserva) {
        return cargoJpa.findFirstByReservaIdAndAnuladoFalse(UUID.fromString(idReserva)).map(this::aModelo);
    }

    @Override
    public List<CargoModel> cargosVigentes(List<String> idsApartamento) {
        List<CargoEntity> lista = idsApartamento == null
                ? cargoJpa.findByAnuladoFalse()
                : idsApartamento.isEmpty() ? List.of() : cargoJpa.findByAnuladoFalseAndApartamentoIdIn(uuids(idsApartamento));
        return lista.stream().map(this::aModelo).toList();
    }

    @Override
    public boolean existeCuota(String idApartamento, String periodo) {
        return cargoJpa.existsByApartamentoIdAndPeriodoAndConceptoAndAnuladoFalse(
                UUID.fromString(idApartamento), periodo, CargoModel.Concepto.ADMINISTRACION.name());
    }

    // ── Pagos ──

    private PagoModel aModelo(PagoEntity e) {
        PagoModel m = new PagoModel();
        m.setId(texto(e.getId()));
        m.setIdCargo(texto(e.getCargoId()));
        m.setValor(e.getValor());
        m.setMedio(PagoModel.Medio.valueOf(e.getMedio()));
        m.setEstado(PagoModel.Estado.valueOf(e.getEstado()));
        m.setReferencia(e.getReferencia());
        m.setSoporte(e.getSoporte());
        m.setTransaccionExterna(e.getTransaccionExterna());
        m.setIdPagadoPor(texto(e.getPagadoPorUsuarioId()));
        m.setIdRegistradoPor(texto(e.getRegistradoPorUsuarioId()));
        m.setFechaCreacion(e.getFechaCreacion());
        m.setFechaAprobacion(e.getFechaAprobacion());
        return m;
    }

    @Override
    public PagoModel guardarPago(PagoModel m) {
        PagoEntity e = new PagoEntity();
        e.setId(uuid(m.getId()));
        e.setCargoId(uuid(m.getIdCargo()));
        e.setValor(m.getValor());
        e.setMedio(m.getMedio().name());
        e.setEstado(m.getEstado().name());
        e.setReferencia(m.getReferencia());
        e.setSoporte(m.getSoporte());
        e.setTransaccionExterna(m.getTransaccionExterna());
        e.setPagadoPorUsuarioId(uuid(m.getIdPagadoPor()));
        e.setRegistradoPorUsuarioId(uuid(m.getIdRegistradoPor()));
        e.setFechaCreacion(m.getFechaCreacion());
        e.setFechaAprobacion(m.getFechaAprobacion());
        return aModelo(pagoJpa.save(e));
    }

    @Override
    public Optional<PagoModel> pagoPorReferencia(String referencia) {
        return pagoJpa.findByReferencia(referencia).map(this::aModelo);
    }

    @Override
    public List<PagoModel> pagosDeCargos(List<String> idsCargo) {
        if (idsCargo.isEmpty()) return List.of();
        return pagoJpa.findByCargoIdIn(uuids(idsCargo)).stream().map(this::aModelo).toList();
    }

    @Override
    public Map<String, BigDecimal> pagadoPorCargo(List<String> idsCargo) {
        Map<String, BigDecimal> suma = new HashMap<>();
        for (PagoModel p : pagosDeCargos(idsCargo)) {
            if (p.getEstado() == PagoModel.Estado.APROBADO)
                suma.merge(p.getIdCargo(), p.getValor(), BigDecimal::add);
        }
        return suma;
    }
}
