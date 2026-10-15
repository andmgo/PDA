-- Fase 2: base para visitas pre-autorizadas, "mis paquetes" y lector de
-- placas. Hasta aquí no existía ningún vínculo entre un Usuario
-- (propietario) y su apartamento, ni registro de vehículos de residentes.

-- Propietario ↔ apartamento: muchos a muchos (un propietario puede tener
-- varios apartamentos y un apartamento varios propietarios, p. ej. una
-- pareja). Es un vínculo, no un dato histórico: quitarlo es un DELETE.
CREATE TABLE usuario_apartamento (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id          UUID NOT NULL REFERENCES usuarios(id),
    apartamento_id      UUID NOT NULL REFERENCES apartamentos(id),
    fecha_asignacion    TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uq_usuario_apartamento UNIQUE (usuario_id, apartamento_id)
);
CREATE INDEX ix_usuario_apartamento_apartamento ON usuario_apartamento(apartamento_id);

-- Vehículos de los residentes de cada apartamento. La placa se guarda
-- normalizada (mayúsculas, sin espacios ni guiones: "ABC123") para que el
-- lector de placas pueda buscarla tal cual. Igual que el resto de
-- entidades, no se borra: se desactiva.
CREATE TABLE vehiculos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    placa               VARCHAR(10) NOT NULL,
    tipo                VARCHAR(10) NOT NULL CHECK (tipo IN ('CARRO', 'MOTO')),
    marca               VARCHAR(40),
    color               VARCHAR(30),
    apartamento_id      UUID NOT NULL REFERENCES apartamentos(id),
    parqueadero_id      UUID REFERENCES parqueaderos(id),
    activo              BOOLEAN NOT NULL DEFAULT true,
    fecha_registro      TIMESTAMP NOT NULL DEFAULT now()
);
-- Una placa solo puede estar activa una vez; desactivada puede volver a
-- registrarse (p. ej. el carro se vendió a otro residente del conjunto).
CREATE UNIQUE INDEX ux_vehiculos_placa_activa ON vehiculos(placa) WHERE activo;
CREATE INDEX ix_vehiculos_apartamento ON vehiculos(apartamento_id);
