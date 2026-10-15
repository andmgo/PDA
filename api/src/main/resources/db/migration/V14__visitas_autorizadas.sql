-- Fase 6 — visitas autorizadas por el propietario.
--
-- Antes portería llamaba al propietario cada vez que llegaba alguien para
-- preguntar si lo dejaba entrar. Ahora el propietario registra de antemano
-- quién viene y en qué fechas; portería ve la lista de "esperados hoy" y
-- registra el ingreso con un toque.

CREATE TABLE visitas_autorizadas (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apartamento_id              UUID NOT NULL REFERENCES apartamentos(id),
    autorizada_por_usuario_id   UUID NOT NULL REFERENCES usuarios(id),
    nombre_visitante            VARCHAR(100) NOT NULL,
    apellido_visitante          VARCHAR(100) NOT NULL,
    numero_documento            VARCHAR(20),          -- opcional: el propietario no siempre lo sabe
    placa                       VARCHAR(10),          -- opcional: si viene en vehículo
    fecha_desde                 DATE NOT NULL,
    fecha_hasta                 DATE NOT NULL,
    nota                        VARCHAR(300),
    cancelada                   BOOLEAN NOT NULL DEFAULT false,
    fecha_creacion              TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT ck_visita_autorizada_rango CHECK (fecha_hasta >= fecha_desde)
);
-- "Esperados hoy": autorizaciones vigentes en una fecha.
CREATE INDEX ix_visitas_autorizadas_vigentes ON visitas_autorizadas (fecha_desde, fecha_hasta) WHERE NOT cancelada;
CREATE INDEX ix_visitas_autorizadas_apartamento ON visitas_autorizadas (apartamento_id);
CREATE INDEX ix_visitas_autorizadas_autor ON visitas_autorizadas (autorizada_por_usuario_id);
CREATE INDEX ix_visitas_autorizadas_documento ON visitas_autorizadas (numero_documento) WHERE numero_documento IS NOT NULL;

-- Auditoría de cada ingreso: con qué autorización entró (si tenía) y qué
-- funcionario lo registró. Recomendación pendiente de la revisión de la BD
-- (docs/BASE_DE_DATOS.md). Nulos en los registros anteriores a esta fase.
ALTER TABLE registro_visitante
    ADD COLUMN visita_autorizada_id      UUID REFERENCES visitas_autorizadas(id),
    ADD COLUMN registrado_por_usuario_id UUID REFERENCES usuarios(id);
ALTER TABLE registro_parqueadero_visitante
    ADD COLUMN visita_autorizada_id      UUID REFERENCES visitas_autorizadas(id),
    ADD COLUMN registrado_por_usuario_id UUID REFERENCES usuarios(id);
CREATE INDEX ix_registro_visitante_autorizacion ON registro_visitante (visita_autorizada_id);
CREATE INDEX ix_registro_visitante_registrador  ON registro_visitante (registrado_por_usuario_id);
CREATE INDEX ix_visitante_vehiculo_autorizacion ON registro_parqueadero_visitante (visita_autorizada_id);
CREATE INDEX ix_visitante_vehiculo_registrador  ON registro_parqueadero_visitante (registrado_por_usuario_id);

-- Permiso para autorizar visitas: propietarios y administración.
-- Portería solo LEE los esperados (con su permiso CONTROL_ACCESOS).
INSERT INTO permisos (codigo, nombre, modulo) VALUES
    ('VISITAS', 'Visitas autorizadas', 'Seguridad');

INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r JOIN permisos p ON p.codigo = 'VISITAS'
WHERE r.nombre_rol IN ('ADMINISTRADOR', 'PROPIETARIO');
