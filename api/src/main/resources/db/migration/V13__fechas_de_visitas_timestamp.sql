-- Fase 4 (revisión de la base de datos) — fechas de visitas como TIMESTAMP.
--
-- registro_visitante y registro_parqueadero_visitante guardaban la entrada y
-- la salida como VARCHAR(30) ("2026-10-15 11:40:47"), herencia de MongoDB.
-- Como texto, ordenar o filtrar "las visitas de hoy" dependía de que todas
-- las filas tuvieran exactamente el mismo formato, y la base no podía
-- validar que fuera una fecha. Ahora son TIMESTAMP; la entrada es obligatoria
-- (toda visita la tiene) y la salida no puede ser anterior a la entrada.
ALTER TABLE registro_visitante
    ALTER COLUMN fecha_hora_entrada TYPE TIMESTAMP
        USING to_timestamp(NULLIF(trim(fecha_hora_entrada), ''), 'YYYY-MM-DD HH24:MI:SS')::timestamp,
    ALTER COLUMN fecha_hora_salida TYPE TIMESTAMP
        USING to_timestamp(NULLIF(trim(fecha_hora_salida), ''), 'YYYY-MM-DD HH24:MI:SS')::timestamp,
    ALTER COLUMN fecha_hora_entrada SET NOT NULL,
    ADD CONSTRAINT ck_registro_visitante_salida CHECK (fecha_hora_salida IS NULL OR fecha_hora_salida >= fecha_hora_entrada);

ALTER TABLE registro_parqueadero_visitante
    ALTER COLUMN fecha_hora_entrada TYPE TIMESTAMP
        USING to_timestamp(NULLIF(trim(fecha_hora_entrada), ''), 'YYYY-MM-DD HH24:MI:SS')::timestamp,
    ALTER COLUMN fecha_hora_salida TYPE TIMESTAMP
        USING to_timestamp(NULLIF(trim(fecha_hora_salida), ''), 'YYYY-MM-DD HH24:MI:SS')::timestamp,
    ALTER COLUMN fecha_hora_entrada SET NOT NULL,
    ADD CONSTRAINT ck_visitante_vehiculo_salida CHECK (fecha_hora_salida IS NULL OR fecha_hora_salida >= fecha_hora_entrada);

CREATE INDEX ix_registro_visitante_entrada ON registro_visitante (fecha_hora_entrada DESC);
CREATE INDEX ix_visitante_vehiculo_entrada ON registro_parqueadero_visitante (fecha_hora_entrada DESC);
