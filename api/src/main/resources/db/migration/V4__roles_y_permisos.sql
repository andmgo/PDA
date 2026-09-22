-- RBAC dinámico: catálogo fijo de permisos (las funcionalidades que el código
-- ya sabe hacer) + matriz rol_permiso editable desde la pantalla admin
-- "Roles y permisos". Sembrado para reproducir EXACTAMENTE las reglas que
-- hoy tenía SecurityConfig hardcodeadas — el comportamiento no cambia al
-- desplegar esta migración, solo se vuelve editable después.

CREATE TABLE permisos (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    codigo  VARCHAR(60) NOT NULL UNIQUE,
    nombre  VARCHAR(120) NOT NULL,
    modulo  VARCHAR(60)
);

CREATE TABLE rol_permiso (
    rol_id      UUID NOT NULL REFERENCES roles(id),
    permiso_id  UUID NOT NULL REFERENCES permisos(id),
    puede_ver     BOOLEAN NOT NULL DEFAULT false,
    puede_editar  BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (rol_id, permiso_id)
);

INSERT INTO permisos (codigo, nombre, modulo) VALUES
    ('GESTION_DATOS', 'Gestión de datos', 'Administración'),
    ('PAGOS_Y_CARTERA', 'Pagos y cartera', 'Finanzas'),
    ('CONTROL_ACCESOS', 'Control de accesos', 'Seguridad'),
    ('SALONES', 'Salones sociales', 'Zonas comunes'),
    ('USUARIOS', 'Usuarios', 'Administración'),
    ('APARTAMENTOS_PARQUEADEROS_RESIDENTES', 'Apartamentos, parqueaderos y residentes', 'Administración'),
    ('RESERVAS', 'Reservas', 'Zonas comunes');

-- ADMINISTRADOR: ver + editar en los 7 permisos.
INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r CROSS JOIN permisos p
WHERE r.nombre_rol = 'ADMINISTRADOR';

-- PROPIETARIO: pagos y reservas (ver+editar), salones y usuarios (solo ver).
INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r JOIN permisos p ON p.codigo IN ('PAGOS_Y_CARTERA', 'RESERVAS')
WHERE r.nombre_rol = 'PROPIETARIO';

INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, false
FROM roles r JOIN permisos p ON p.codigo IN ('SALONES', 'USUARIOS')
WHERE r.nombre_rol = 'PROPIETARIO';

-- FUNCIONARIO: accesos y reservas (ver+editar), salones y usuarios (solo ver).
INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, true
FROM roles r JOIN permisos p ON p.codigo IN ('CONTROL_ACCESOS', 'RESERVAS')
WHERE r.nombre_rol = 'FUNCIONARIO';

INSERT INTO rol_permiso (rol_id, permiso_id, puede_ver, puede_editar)
SELECT r.id, p.id, true, false
FROM roles r JOIN permisos p ON p.codigo IN ('SALONES', 'USUARIOS')
WHERE r.nombre_rol = 'FUNCIONARIO';
