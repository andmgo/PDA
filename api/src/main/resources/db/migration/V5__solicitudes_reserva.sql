-- Cola de solicitudes de reserva excepcional (reservas fuera del año actual)
-- que un administrador aprueba o rechaza dentro de la misma app — sin correo
-- real, ver ReservaSalonSocialService.guardarReservaAprobada().

CREATE TABLE solicitud_reserva_excepcional (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                  UUID NOT NULL REFERENCES usuarios(id),
    salon_id                    UUID NOT NULL REFERENCES salones_sociales(id),
    fecha_solicitada            TIMESTAMP NOT NULL,
    justificacion               VARCHAR(500),
    estado                      VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    revisado_por_usuario_id     UUID REFERENCES usuarios(id),
    motivo_rechazo              VARCHAR(500),
    fecha_creacion               TIMESTAMP NOT NULL DEFAULT now(),
    fecha_revision               TIMESTAMP
);
