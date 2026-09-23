-- Reemplaza "eliminar" por "activar/desactivar" en las 5 entidades
-- administradas desde Gestión de Datos. Motivo: un borrado físico perdía
-- el historial ligado por clave foránea (reservas, registros de acceso,
-- paquetes, etc. — de ahí los mensajes "no se puede eliminar porque tiene
-- registros relacionados" que ya existían) y no había forma de revertir
-- un error. "activo" reemplaza al borrado; el dato nunca se pierde.
ALTER TABLE apartamentos     ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE parqueaderos     ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE salones_sociales ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE residentes       ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
ALTER TABLE usuarios         ADD COLUMN activo BOOLEAN NOT NULL DEFAULT true;
