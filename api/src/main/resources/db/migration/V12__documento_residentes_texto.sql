-- Fase 4 (revisión de la base de datos) — documento de residentes como texto.
--
-- residentes.numero_documento era INTEGER (máximo 2.147.483.647): no cabían
-- documentos de 10 dígitos que empiezan por 3 o más, ni pasaportes/cédulas
-- de extranjería con letras. usuarios.numero_documento ya era VARCHAR(20);
-- ahora ambos tienen el mismo tipo. Los valores se guardan normalizados
-- (mayúsculas, sin puntos ni espacios — ResidenteModel.normalizarDocumento).
ALTER TABLE residentes
    ALTER COLUMN numero_documento TYPE VARCHAR(20) USING numero_documento::text;
