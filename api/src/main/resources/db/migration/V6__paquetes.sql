-- Módulo de paquetes de recepción: los funcionarios registran cada paquete
-- que llega (nombre de quien recibe, apartamento, cédula). Solo visible para
-- Administrador y Funcionario — Propietario no tiene ni siquiera permiso de
-- ver (ni fila en rol_permiso para este permiso).

CREATE TABLE paquetes (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    apartamento_id          UUID NOT NULL REFERENCES apartamentos(id),
    nombre_receptor         VARCHAR(150) NOT NULL,
    cedula_receptor         VARCHAR(20) NOT NULL,
    fecha_hora_llegada      TIMESTAMP NOT NULL DEFAULT now(),
    entregado               BOOLEAN NOT NULL DEFAULT false,
    fecha_hora_entrega      TIMESTAMP
);

INSERT INTO permisos (codigo, nombre, modulo) VALUES
    ('PAQUETES', 'Paquetes de recepción', 'Seguridad');

INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r JOIN permisos p ON p.codigo = 'PAQUETES'
WHERE r.nombre_rol IN ('ADMINISTRADOR', 'FUNCIONARIO');
