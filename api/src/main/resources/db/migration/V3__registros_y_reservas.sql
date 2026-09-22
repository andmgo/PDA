-- Registros de acceso/visitantes y reservas. Llevan FK real a las entidades
-- principales: hoy en Mongo un idApartamento podía apuntar a nada, aquí
-- Postgres lo impide a nivel de base de datos (red de seguridad adicional,
-- no reemplaza las validaciones que ya existen en services/controllers).

CREATE TABLE registro_acceso_piscina (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apartamento_id  UUID NOT NULL REFERENCES apartamentos(id),
    residente_id    UUID NOT NULL REFERENCES residentes(id),
    fecha_hora      TIMESTAMP NOT NULL
);

-- fecha_hora_entrada/salida se preservan como VARCHAR: el dominio hoy los
-- maneja como String formateado ("yyyy-MM-dd HH:mm:ss"), no como fecha real.
-- No se corrige ese detalle en esta migración — es un cambio de
-- comportamiento aparte, no de infraestructura de persistencia.
CREATE TABLE registro_visitante (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    residente_id            UUID NOT NULL REFERENCES residentes(id),
    apartamento_id          UUID NOT NULL REFERENCES apartamentos(id),
    fecha_hora_entrada      VARCHAR(30),
    fecha_hora_salida       VARCHAR(30)
);

CREATE TABLE registro_parqueadero_visitante (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    residente_id        UUID NOT NULL REFERENCES residentes(id),
    parqueadero_id      UUID NOT NULL REFERENCES parqueaderos(id),
    apartamento_id      UUID REFERENCES apartamentos(id),
    placa               VARCHAR(20),
    fecha_hora_entrada  VARCHAR(30),
    fecha_hora_salida   VARCHAR(30)
);

CREATE TABLE reserva_salon_social (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id              UUID NOT NULL REFERENCES usuarios(id),
    salon_id                UUID NOT NULL REFERENCES salones_sociales(id),
    fecha_y_hora_reserva    TIMESTAMP NOT NULL
);
