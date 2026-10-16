-- Bloque J: Pagos y cartera real (antes la página era una maqueta).
--  · cargos: lo que debe cada apartamento (cuota de administración del mes,
--    reserva del salón social u otro concepto).
--  · pagos: abonos a un cargo, en línea (Wompi) o registrados a mano por la
--    administración (transferencia, consignación, efectivo) con su soporte.
--  El saldo de un cargo = valor − pagos APROBADOS. Montos en pesos (COP).

CREATE TABLE configuracion_cartera (
    id                    SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),   -- una sola fila
    cuota_administracion  NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (cuota_administracion >= 0),
    valor_reserva         NUMERIC(12, 2) NOT NULL DEFAULT 0 CHECK (valor_reserva >= 0),
    dia_vencimiento       SMALLINT NOT NULL DEFAULT 10 CHECK (dia_vencimiento BETWEEN 1 AND 28)
);
INSERT INTO configuracion_cartera (id) VALUES (1);

CREATE TABLE cargos (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apartamento_id     UUID NOT NULL REFERENCES apartamentos(id),
    concepto           VARCHAR(20) NOT NULL CHECK (concepto IN ('ADMINISTRACION', 'RESERVA', 'OTRO')),
    descripcion        VARCHAR(200) NOT NULL,
    periodo            VARCHAR(7),                                -- '2026-10' (solo administración)
    reserva_id         UUID REFERENCES reserva_salon_social(id) ON DELETE SET NULL,
    valor              NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    fecha_vencimiento  DATE NOT NULL,
    anulado            BOOLEAN NOT NULL DEFAULT false,
    fecha_creacion     TIMESTAMP NOT NULL DEFAULT now()
);
-- Una sola cuota de administración por apartamento y mes (generar dos veces no duplica)
CREATE UNIQUE INDEX ux_cargos_cuota_mes ON cargos (apartamento_id, periodo)
    WHERE concepto = 'ADMINISTRACION' AND NOT anulado;
CREATE UNIQUE INDEX ux_cargos_reserva ON cargos (reserva_id) WHERE reserva_id IS NOT NULL AND NOT anulado;
CREATE INDEX idx_cargos_apartamento ON cargos (apartamento_id);

CREATE TABLE pagos (
    id                         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cargo_id                   UUID NOT NULL REFERENCES cargos(id),
    valor                      NUMERIC(12, 2) NOT NULL CHECK (valor > 0),
    medio                      VARCHAR(20) NOT NULL CHECK (medio IN ('WOMPI', 'TRANSFERENCIA', 'CONSIGNACION', 'EFECTIVO')),
    estado                     VARCHAR(12) NOT NULL CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO')),
    referencia                 VARCHAR(60) NOT NULL UNIQUE,      -- la que se envía a Wompi / interna
    soporte                    VARCHAR(120),                     -- n.º de comprobante (pagos manuales)
    transaccion_externa        VARCHAR(80),                      -- id de la transacción en Wompi
    pagado_por_usuario_id      UUID REFERENCES usuarios(id),     -- quien pagó en línea
    registrado_por_usuario_id  UUID REFERENCES usuarios(id),     -- quien registró un pago manual
    fecha_creacion             TIMESTAMP NOT NULL DEFAULT now(),
    fecha_aprobacion           TIMESTAMP
);
CREATE INDEX idx_pagos_cargo ON pagos (cargo_id);

-- Administrar la cartera (generar cuotas, registrar pagos, ver todos los
-- apartamentos). PAGOS_Y_CARTERA sigue siendo "mi estado de cuenta".
INSERT INTO permisos (codigo, nombre, modulo) VALUES
    ('ADMIN_CARTERA', 'Administrar cartera', 'Finanzas');
INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r JOIN permisos p ON p.codigo = 'ADMIN_CARTERA'
WHERE r.nombre_rol = 'ADMINISTRADOR';
