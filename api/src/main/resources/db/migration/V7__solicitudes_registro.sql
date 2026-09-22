CREATE TABLE solicitud_registro (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    numero_documento VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL,
    contrasena_hash VARCHAR(200) NOT NULL,
    nombre_tipo_documento VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    revisado_por_usuario_id UUID NULL REFERENCES usuarios(id),
    motivo_rechazo VARCHAR(500) NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT now(),
    fecha_revision TIMESTAMP NULL
);
