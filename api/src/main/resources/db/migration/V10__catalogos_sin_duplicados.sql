-- Fase 4 (revisión de la base de datos) — catálogos duplicados.
--
-- Los repositorios buscaban cada catálogo por nombre EXACTO y, si no lo
-- encontraban, lo creaban en silencio; además /api/catalogos/todos devolvía
-- listas escritas a mano que no coincidían con la base. Resultado real
-- encontrado en la base de desarrollo:
--   roles            'Administrador' (0 permisos, 1 usuario) junto a 'ADMINISTRADOR'
--   estados_cuenta   'Al dia' (los 8 apartamentos) junto a 'Al día' (0)
--   tipos_ocupacion  'Propietario' (los 8 apartamentos) — no es un tipo de ocupación
--   tipos_documento  'Cédula de Ciudadanía' dos veces, una con la codificación rota
-- Aquí se mueven las referencias al valor oficial y se borra el duplicado.
-- El código ya no crea catálogos (CatalogoResolver) y los índices únicos de
-- abajo impiden volver a tener dos filas que solo difieran en mayúsculas.

-- ── Roles: el oficial es el que está en MAYÚSCULAS ──
UPDATE usuarios u
SET rol_id = oficial.id
FROM roles dup
JOIN roles oficial ON upper(oficial.nombre_rol) = upper(dup.nombre_rol)
                  AND oficial.nombre_rol = upper(oficial.nombre_rol)
                  AND oficial.id <> dup.id
WHERE u.rol_id = dup.id;

DELETE FROM rol_permiso rp
USING roles dup, roles oficial
WHERE rp.rol_id = dup.id
  AND upper(oficial.nombre_rol) = upper(dup.nombre_rol)
  AND oficial.nombre_rol = upper(oficial.nombre_rol)
  AND oficial.id <> dup.id;

DELETE FROM roles dup
USING roles oficial
WHERE upper(oficial.nombre_rol) = upper(dup.nombre_rol)
  AND oficial.nombre_rol = upper(oficial.nombre_rol)
  AND oficial.id <> dup.id;

UPDATE roles SET nombre_rol = upper(nombre_rol) WHERE nombre_rol <> upper(nombre_rol);

-- ── Estados de cuenta: 'Al dia' → 'Al día' ──
UPDATE apartamentos
SET estado_cuenta_id = (SELECT id FROM estados_cuenta WHERE nombre_estado_cuenta = 'Al día')
WHERE estado_cuenta_id IN (SELECT id FROM estados_cuenta WHERE nombre_estado_cuenta = 'Al dia');
DELETE FROM estados_cuenta WHERE nombre_estado_cuenta = 'Al dia';

-- ── Tipos de ocupación: 'Propietario' (ocupado por su dueño) → 'Propio' ──
UPDATE apartamentos
SET tipo_ocupacion_id = (SELECT id FROM tipos_ocupacion WHERE nombre_tipo_ocupacion = 'Propio')
WHERE tipo_ocupacion_id IN (SELECT id FROM tipos_ocupacion WHERE nombre_tipo_ocupacion = 'Propietario');
DELETE FROM tipos_ocupacion WHERE nombre_tipo_ocupacion = 'Propietario';

-- ── Tipos de documento: cualquier variante de "Cédula de ..." → código oficial ──
-- (una de las filas tiene la codificación rota, por eso se busca por '%dula%')
UPDATE usuarios
SET tipo_documento_id = (SELECT id FROM tipos_documento WHERE nombre_tipo_documento = 'CC')
WHERE tipo_documento_id IN (SELECT id FROM tipos_documento
                            WHERE nombre_tipo_documento NOT IN ('CC', 'CE', 'Pasaporte', 'TI')
                              AND nombre_tipo_documento ILIKE '%dula%'
                              AND nombre_tipo_documento NOT ILIKE '%extranj%');
UPDATE residentes
SET tipo_documento_id = (SELECT id FROM tipos_documento WHERE nombre_tipo_documento = 'CC')
WHERE tipo_documento_id IN (SELECT id FROM tipos_documento
                            WHERE nombre_tipo_documento NOT IN ('CC', 'CE', 'Pasaporte', 'TI')
                              AND nombre_tipo_documento ILIKE '%dula%'
                              AND nombre_tipo_documento NOT ILIKE '%extranj%');
UPDATE usuarios
SET tipo_documento_id = (SELECT id FROM tipos_documento WHERE nombre_tipo_documento = 'CE')
WHERE tipo_documento_id IN (SELECT id FROM tipos_documento
                            WHERE nombre_tipo_documento NOT IN ('CC', 'CE', 'Pasaporte', 'TI')
                              AND nombre_tipo_documento ILIKE '%extranj%');
DELETE FROM tipos_documento td
WHERE td.nombre_tipo_documento NOT IN ('CC', 'CE', 'Pasaporte', 'TI')
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.tipo_documento_id = td.id)
  AND NOT EXISTS (SELECT 1 FROM residentes r WHERE r.tipo_documento_id = td.id);

-- solicitud_registro guarda el tipo como TEXTO (no FK): mismo arreglo
UPDATE solicitud_registro SET nombre_tipo_documento = 'CC'
WHERE nombre_tipo_documento NOT IN ('CC', 'CE', 'Pasaporte', 'TI')
  AND nombre_tipo_documento ILIKE '%dula%' AND nombre_tipo_documento NOT ILIKE '%extranj%';
UPDATE solicitud_registro SET nombre_tipo_documento = 'CE'
WHERE nombre_tipo_documento ILIKE '%extranj%';
UPDATE solicitud_registro SET nombre_tipo_documento = 'TI'
WHERE nombre_tipo_documento ILIKE 'tarjeta%';

-- ── Nunca más dos filas que solo difieran en mayúsculas ──
CREATE UNIQUE INDEX ux_roles_nombre            ON roles           (lower(nombre_rol));
CREATE UNIQUE INDEX ux_tipos_documento_nombre  ON tipos_documento (lower(nombre_tipo_documento));
CREATE UNIQUE INDEX ux_tipos_ocupacion_nombre  ON tipos_ocupacion (lower(nombre_tipo_ocupacion));
CREATE UNIQUE INDEX ux_estados_cuenta_nombre   ON estados_cuenta  (lower(nombre_estado_cuenta));
CREATE UNIQUE INDEX ux_estados_nombre          ON estados         (lower(nombre_estado));
CREATE UNIQUE INDEX ux_tipos_residente_nombre  ON tipos_residente (lower(nombre_tipo_residente));
