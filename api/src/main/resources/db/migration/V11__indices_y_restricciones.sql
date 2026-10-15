-- Fase 4 (revisión de la base de datos) — índices y restricciones.

-- ── Índices en llaves foráneas ──
-- PostgreSQL NO indexa automáticamente las columnas FK (solo la PK y los
-- UNIQUE). Estas 24 columnas se usan en joins y búsquedas ("paquetes del
-- apartamento X", "reservas del salón Y", "visitas de este visitante"…) y
-- sin índice cada consulta recorre la tabla completa a medida que crece.
CREATE INDEX ix_apartamentos_estado_cuenta        ON apartamentos (estado_cuenta_id);
CREATE INDEX ix_apartamentos_tipo_ocupacion       ON apartamentos (tipo_ocupacion_id);
CREATE INDEX ix_parqueaderos_estado               ON parqueaderos (estado_id);
CREATE INDEX ix_salones_sociales_estado           ON salones_sociales (estado_id);
CREATE INDEX ix_residentes_tipo_documento         ON residentes (tipo_documento_id);
CREATE INDEX ix_residentes_tipo_residente         ON residentes (tipo_residente_id);
CREATE INDEX ix_usuarios_rol                      ON usuarios (rol_id);
CREATE INDEX ix_usuarios_tipo_documento           ON usuarios (tipo_documento_id);
CREATE INDEX ix_acceso_piscina_apartamento        ON registro_acceso_piscina (apartamento_id);
CREATE INDEX ix_acceso_piscina_residente          ON registro_acceso_piscina (residente_id);
CREATE INDEX ix_registro_visitante_apartamento    ON registro_visitante (apartamento_id);
CREATE INDEX ix_registro_visitante_residente      ON registro_visitante (residente_id);
CREATE INDEX ix_visitante_vehiculo_apartamento    ON registro_parqueadero_visitante (apartamento_id);
CREATE INDEX ix_visitante_vehiculo_parqueadero    ON registro_parqueadero_visitante (parqueadero_id);
CREATE INDEX ix_visitante_vehiculo_residente      ON registro_parqueadero_visitante (residente_id);
CREATE INDEX ix_reserva_salon_usuario             ON reserva_salon_social (usuario_id);
CREATE INDEX ix_rol_permiso_permiso               ON rol_permiso (permiso_id);
CREATE INDEX ix_solicitud_reserva_usuario         ON solicitud_reserva_excepcional (usuario_id);
CREATE INDEX ix_solicitud_reserva_salon           ON solicitud_reserva_excepcional (salon_id);
CREATE INDEX ix_solicitud_reserva_revisor         ON solicitud_reserva_excepcional (revisado_por_usuario_id);
CREATE INDEX ix_solicitud_registro_revisor        ON solicitud_registro (revisado_por_usuario_id);
CREATE INDEX ix_paquetes_apartamento              ON paquetes (apartamento_id);
CREATE INDEX ix_vehiculos_parqueadero             ON vehiculos (parqueadero_id);
-- (reserva_salon_social.salon_id queda cubierto por el UNIQUE de abajo)

-- ── Búsquedas frecuentes que no son FK ──
-- "Quién está dentro ahora": visitas sin salida registrada.
CREATE INDEX ix_registro_visitante_adentro ON registro_visitante (residente_id) WHERE fecha_hora_salida IS NULL;
CREATE INDEX ix_visitante_vehiculo_adentro ON registro_parqueadero_visitante (residente_id) WHERE fecha_hora_salida IS NULL;
-- Paquetes pendientes de entrega.
CREATE INDEX ix_paquetes_pendientes ON paquetes (apartamento_id) WHERE NOT entregado;

-- ── Reglas de negocio que hasta ahora solo validaba el código ──
-- Un salón no puede tener dos reservas a la misma fecha y hora (el servicio
-- ya lo revisaba, pero dos solicitudes simultáneas podían colarse las dos).
ALTER TABLE reserva_salon_social
    ADD CONSTRAINT uq_reserva_salon_fecha UNIQUE (salon_id, fecha_y_hora_reserva);

-- El correo identifica a la persona en el login: "Ana@x.com" y "ana@x.com"
-- deben ser la misma cuenta.
CREATE UNIQUE INDEX ux_usuarios_correo_sin_mayusculas ON usuarios (lower(correo));

-- Estados de las colas de aprobación: solo los tres que maneja el código.
ALTER TABLE solicitud_reserva_excepcional
    ADD CONSTRAINT ck_solicitud_reserva_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));
ALTER TABLE solicitud_registro
    ADD CONSTRAINT ck_solicitud_registro_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));
