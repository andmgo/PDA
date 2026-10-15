package gescazone.demo.application.service;

import gescazone.demo.application.exception.ValidationException;
import gescazone.demo.domain.model.ApartamentoModel;
import gescazone.demo.domain.model.ParqueaderoModel;
import gescazone.demo.domain.model.ResidenteModel;
import gescazone.demo.domain.model.SalonSocialModel;
import gescazone.demo.domain.model.UsuarioModel;

/**
 * Regla única: nada desactivado se puede usar. Desde que "eliminar" pasó a
 * "activar/desactivar" (migración V8) solo el login filtraba por activo —
 * se podía reservar un salón desactivado, registrar paquetes o visitas a un
 * apartamento desactivado, etc. Todos los servicios que consumen estas
 * entidades pasan por aquí para tener el mismo criterio y el mismo mensaje.
 * Consultar (listar/historial) sigue permitido; lo que se bloquea es operar.
 */
final class RequiereActivo {

    private RequiereActivo() {}

    static ApartamentoModel apartamento(ApartamentoModel a) {
        if (a != null && !a.isActivo())
            throw new ValidationException("El apartamento " + a.getNumero() + " está desactivado. Actívalo en Gestión de Datos para usarlo.");
        return a;
    }

    static ParqueaderoModel parqueadero(ParqueaderoModel p) {
        if (p != null && !p.isActivo())
            throw new ValidationException("El parqueadero " + p.getNumero() + " está desactivado.");
        return p;
    }

    static SalonSocialModel salon(SalonSocialModel s) {
        if (s != null && !s.isActivo())
            throw new ValidationException("El salón " + s.getNumero() + " está desactivado y no se puede reservar.");
        return s;
    }

    static ResidenteModel residente(ResidenteModel r) {
        if (r != null && !r.isActivo())
            throw new ValidationException(r.getNombre() + " " + r.getApellido() + " está desactivado(a) y no tiene acceso.");
        return r;
    }

    static UsuarioModel usuario(UsuarioModel u) {
        if (u != null && !u.isActivo())
            throw new ValidationException("La cuenta de " + u.getNombre() + " " + u.getApellido() + " está desactivada.");
        return u;
    }
}
