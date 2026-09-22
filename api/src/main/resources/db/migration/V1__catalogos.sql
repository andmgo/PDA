-- Tablas catálogo: hoy en Mongo eran solo un string suelto copiado en cada
-- documento. Aquí pasan a ser tablas reales con id propio, referenciadas por
-- FK — es lo que habilita roles (y demás catálogos) verdaderamente dinámicos.

CREATE TABLE roles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_rol  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipos_documento (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_tipo_documento   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipos_ocupacion (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_tipo_ocupacion   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estados_cuenta (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_estado_cuenta    VARCHAR(50) NOT NULL UNIQUE
);

-- Compartida por parqueaderos y salones sociales, igual que hoy EstadoModel.
CREATE TABLE estados (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_estado   VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipos_residente (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre_tipo_residente   VARCHAR(50) NOT NULL UNIQUE
);

-- Datos semilla de catálogos (independiente del perfil — son datos de
-- referencia del negocio, no datos de prueba como los 150 usuarios).
INSERT INTO roles (nombre_rol) VALUES
    ('ADMINISTRADOR'), ('PROPIETARIO'), ('FUNCIONARIO');

INSERT INTO tipos_documento (nombre_tipo_documento) VALUES
    ('CC'), ('CE'), ('Pasaporte'), ('TI');

INSERT INTO tipos_ocupacion (nombre_tipo_ocupacion) VALUES
    ('Propio'), ('Arrendado');

INSERT INTO estados_cuenta (nombre_estado_cuenta) VALUES
    ('Al día'), ('En mora');

INSERT INTO estados (nombre_estado) VALUES
    ('Disponible'), ('Ocupado'), ('Mantenimiento');

INSERT INTO tipos_residente (nombre_tipo_residente) VALUES
    ('Propietario'), ('Arrendatario'), ('Visitante');
