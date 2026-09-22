-- Entidades principales. PK UUID para preservar el mismo contrato de "id"
-- como String opaco que ya usaba todo el dominio/controllers con el
-- ObjectId de Mongo — cero cambios por encima de infrastructure/.

CREATE TABLE apartamentos (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero              VARCHAR(20) NOT NULL UNIQUE,
    medidas             VARCHAR(100),
    telefono            BIGINT,
    tipo_ocupacion_id   UUID REFERENCES tipos_ocupacion(id),
    estado_cuenta_id    UUID REFERENCES estados_cuenta(id)
);

CREATE TABLE parqueaderos (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero      VARCHAR(20) NOT NULL UNIQUE,
    medidas     VARCHAR(100),
    telefono    BIGINT,
    estado_id   UUID REFERENCES estados(id)
);

CREATE TABLE salones_sociales (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero      VARCHAR(20) NOT NULL UNIQUE,
    medidas     VARCHAR(100),
    telefono    BIGINT,
    estado_id   UUID REFERENCES estados(id)
);

CREATE TABLE residentes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_documento    INTEGER NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    celular             BIGINT,
    tipo_documento_id   UUID REFERENCES tipos_documento(id),
    tipo_residente_id   UUID REFERENCES tipos_residente(id)
);

CREATE TABLE usuarios (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_documento    VARCHAR(20) NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    correo              VARCHAR(150) NOT NULL UNIQUE,
    contrasena          VARCHAR(255) NOT NULL,
    rol_id              UUID REFERENCES roles(id),
    tipo_documento_id   UUID REFERENCES tipos_documento(id)
);
