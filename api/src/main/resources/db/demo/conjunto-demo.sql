-- Conjunto de demostración (solo perfil "dev", ver DataInitializer): se
-- ejecuta una vez, cuando no hay apartamentos. Las fechas son relativas a
-- hoy para que la demostración no envejezca.

-- Apartamentos: torres de 4 pisos, 4 por piso
INSERT INTO apartamentos (numero, tipo_ocupacion_id, estado_cuenta_id, activo)
SELECT p || lpad(a::text, 2, '0'),
       (SELECT id FROM tipos_ocupacion WHERE nombre_tipo_ocupacion = CASE WHEN a = 4 THEN 'Arrendado' ELSE 'Propio' END),
       (SELECT id FROM estados_cuenta WHERE nombre_estado_cuenta = 'Al día'),
       true
FROM generate_series(1, 5) p, generate_series(1, 4) a
ON CONFLICT (numero) DO NOTHING;

-- Propietarios: 300000001 (Elena) en el 401, Carlos en el 102, y los demás en orden
INSERT INTO usuario_apartamento (usuario_id, apartamento_id)
SELECT u.id, ap.id
FROM (VALUES ('300000001', '401'), ('123456781', '102'), ('300000002', '402'), ('300000003', '403'),
             ('300000004', '404'), ('300000005', '101'), ('300000006', '103'), ('300000007', '104'),
             ('300000008', '201'), ('300000009', '202'), ('300000010', '203'), ('300000011', '204'),
             ('300000012', '301'), ('300000013', '302'), ('300000014', '303'), ('300000015', '304'),
             ('300000016', '501'), ('300000017', '502'), ('300000018', '503'), ('300000019', '504'))
     AS m(doc, apto)
JOIN usuarios u ON u.numero_documento = m.doc
JOIN apartamentos ap ON ap.numero = m.apto
ON CONFLICT DO NOTHING;

-- Salones sociales y parqueaderos
INSERT INTO salones_sociales (numero, estado_id, activo)
SELECT s, (SELECT id FROM estados WHERE nombre_estado = 'Disponible'), true
FROM unnest(ARRAY['S-10', 'S-11', 'S-12']) s
WHERE NOT EXISTS (SELECT 1 FROM salones_sociales WHERE numero = s);

INSERT INTO parqueaderos (numero, estado_id, activo)
SELECT 'P-' || lpad(n::text, 2, '0'), (SELECT id FROM estados WHERE nombre_estado = 'Disponible'), true
FROM generate_series(1, 12) n
ON CONFLICT (numero) DO NOTHING;

-- Vehículos registrados (el lector de placas los reconoce)
INSERT INTO vehiculos (placa, tipo, marca, color, apartamento_id)
SELECT v.placa, v.tipo, v.marca, v.color, ap.id
FROM (VALUES ('ABC123', 'CARRO', 'Mazda', 'Gris', '401'), ('XYZ12D', 'MOTO', NULL, 'Negra', '402'),
             ('KLM456', 'CARRO', 'Renault', 'Blanco', '101'), ('JHT789', 'CARRO', 'Chevrolet', 'Azul', '203'))
     AS v(placa, tipo, marca, color, apto)
JOIN apartamentos ap ON ap.numero = v.apto
WHERE NOT EXISTS (SELECT 1 FROM vehiculos WHERE placa = v.placa);

-- Cartera: cuota y valor de reserva
UPDATE configuracion_cartera SET cuota_administracion = 350000, valor_reserva = 80000, dia_vencimiento = 10 WHERE id = 1;

-- Cuotas de administración del mes pasado y del actual
INSERT INTO cargos (apartamento_id, concepto, descripcion, periodo, valor, fecha_vencimiento)
SELECT ap.id, 'ADMINISTRACION',
       'Administración ' || (ARRAY['enero','febrero','marzo','abril','mayo','junio','julio','agosto',
                                  'septiembre','octubre','noviembre','diciembre'])[extract(month FROM m)::int]
           || ' ' || extract(year FROM m)::int,
       to_char(m, 'YYYY-MM'), 350000, (m + interval '9 days')::date
FROM apartamentos ap,
     (SELECT date_trunc('month', current_date) - interval '1 month' AS m
      UNION ALL SELECT date_trunc('month', current_date)) meses
WHERE ap.activo;

-- Pagos: el mes pasado lo pagaron todos menos 203, 304 y 502 (quedan en mora con
-- dos cuotas); el mes actual, todos menos esos tres y 401 (Elena), 102 y 103.
INSERT INTO pagos (cargo_id, valor, medio, estado, referencia, soporte, registrado_por_usuario_id, fecha_creacion, fecha_aprobacion)
SELECT c.id, c.valor,
       CASE WHEN row_number() OVER (ORDER BY c.periodo, ap.numero) % 3 = 0 THEN 'EFECTIVO' ELSE 'TRANSFERENCIA' END,
       'APROBADO',
       'AUR-' || upper(substr(md5(random()::text || c.id::text), 1, 16)),
       CASE WHEN row_number() OVER (ORDER BY c.periodo, ap.numero) % 3 = 0 THEN NULL
            ELSE 'BCO-' || (70000 + row_number() OVER (ORDER BY c.periodo, ap.numero)) END,
       (SELECT id FROM usuarios WHERE numero_documento = '123456780'),
       least(c.fecha_vencimiento - 3, current_date), least(c.fecha_vencimiento - 3, current_date)
FROM cargos c
JOIN apartamentos ap ON ap.id = c.apartamento_id
WHERE (c.periodo = to_char(current_date - interval '1 month', 'YYYY-MM')
       AND ap.numero NOT IN ('203', '304', '502'))
   OR (c.periodo = to_char(current_date, 'YYYY-MM')
       AND ap.numero NOT IN ('203', '304', '502', '401', '102', '103'));
