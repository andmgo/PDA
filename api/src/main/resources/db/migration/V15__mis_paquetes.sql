-- Fase 7: "Mis paquetes". El propietario ve los paquetes de SUS apartamentos
-- y portería deja trazabilidad de quién recibió y quién entregó cada uno.

ALTER TABLE paquetes
    ADD COLUMN descripcion                VARCHAR(150),          -- "Caja de Amazon", "Sobre"...
    ADD COLUMN registrado_por_usuario_id  UUID REFERENCES usuarios(id),
    ADD COLUMN entregado_por_usuario_id   UUID REFERENCES usuarios(id),
    ADD COLUMN entregado_a                VARCHAR(150);          -- quién lo retiró de portería

CREATE INDEX IF NOT EXISTS idx_paquetes_apartamento_entregado ON paquetes (apartamento_id, entregado);

-- Permiso propio: ver los paquetes de los apartamentos vinculados a la cuenta.
-- (PAQUETES sigue siendo el módulo de recepción de portería.)
INSERT INTO permisos (codigo, nombre, modulo) VALUES
    ('MIS_PAQUETES', 'Mis paquetes', 'Seguridad');

INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, false
FROM roles r JOIN permisos p ON p.codigo = 'MIS_PAQUETES'
WHERE r.nombre_rol IN ('ADMINISTRADOR', 'PROPIETARIO');
