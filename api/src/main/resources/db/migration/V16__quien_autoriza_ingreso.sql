-- Control de accesos: el vigilante registra quién dio el paso al visitante
-- (el residente que autorizó por citófono o en persona). Con visita
-- autorizada previamente se llena con el nombre de quien la creó.
ALTER TABLE registro_visitante
    ADD COLUMN autorizado_por VARCHAR(150);
ALTER TABLE registro_parqueadero_visitante
    ADD COLUMN autorizado_por VARCHAR(150);
