INSERT INTO rubro (id_rubro, nombre) VALUES
(1, 'Inmobiliaria'),
(2, 'Educación escolar'),
(3, 'Crédito de consumo'),
(4, 'Seguros'),
(5, 'ONG'),
(6, 'Club deportivo'),
(7, 'Educación superior'),
(8, 'Administración de arriendos'),
(9, 'Educación parvularia'),
(10, 'Administración de condominio'),
(11, 'Deporte'),
(12, 'Educación artística'),
(13, 'Salud'),
(14, 'Otro');

INSERT INTO empresa_cliente (id_empresa, codigo, razon_social, nombre_fantasia, rut, id_rubro, correo_atencion, telefono_atencion, recargo_atraso, activa) VALUES
(1, 'EC-01', 'Inmobiliaria Altos del Maipo SpA', 'Altos del Maipo', '76.845.213-K', 1, 'atencion@altosmaipo.ejemplo', '+56 2 0000 0201', 8500, TRUE),
(2, 'EC-02', 'Corporación Educacional Bosque Nativo', 'Colegio Bosque Nativo', '65.132.874-8', 2, 'pagos@bosquenativo.ejemplo', '+56 2 0000 0202', 5000, TRUE),
(3, 'EC-03', 'Crédito Andino S.A.', 'Crédito Andino', '96.571.438-3', 3, 'clientes@creditoandino.ejemplo', '+56 2 0000 0203', 12000, TRUE),
(4, 'EC-04', 'Seguros Horizonte Sur S.A.', 'Horizonte Sur', '96.814.205-4', 4, 'contacto@horizontesur.ejemplo', '+56 2 0000 0204', 3000, TRUE),
(5, 'EC-05', 'Fundación Manos Abiertas', 'Manos Abiertas', '65.247.190-0', 5, 'socios@manosabiertas.ejemplo', '+56 2 0000 0205', 0, TRUE),
(6, 'EC-06', 'Club Deportivo Los Halcones', 'Club Los Halcones', '65.398.412-K', 6, 'tesoreria@loshalcones.ejemplo', '+56 2 0000 0206', 2000, TRUE),
(7, 'EC-07', 'Instituto Técnico Cordillera SpA', 'Instituto Cordillera', '77.214.583-7', 7, 'finanzas@itcordillera.ejemplo', '+56 2 0000 0207', 6000, TRUE),
(8, 'EC-08', 'Arriendos Plaza Oriente Ltda.', 'Plaza Oriente', '76.432.871-K', 8, 'cobranza@plazaoriente.ejemplo', '+56 2 0000 0208', 7500, TRUE);

INSERT INTO compromiso (id_compromiso, folio, id_usuario_final, id_empresa, concepto, numero_cuota, total_cuotas, monto, fecha_vencimiento, estado, recargo_aplicado, fecha_cambio_estado, id_admin_registro, fecha_registro) VALUES
(101, 'CP-2026-0101', 1, 8, 'Arriendo departamento octubre', 10, 12, 520000, '2026-10-10', 'PENDIENTE', 0, '2026-10-01 09:00:00', 9, '2026-10-01 09:00:00'),
(102, 'CP-2026-0102', 1, 6, 'Cuota social octubre', 10, 12, 15000, '2026-10-05', 'VENCIDO', 2000, '2026-10-06 00:00:00', 13, '2026-10-01 09:00:00'),
(103, 'CP-2026-0103', 1, 5, 'Aporte mensual octubre', NULL, NULL, 10000, '2026-10-15', 'PENDIENTE', 0, '2026-10-01 09:00:00', NULL, '2026-10-01 09:00:00'),
(104, 'CP-2026-0104', 1, 3, 'Crédito de consumo', 7, 24, 86400, '2026-09-25', 'PAGADO', 0, '2026-09-24 10:42:00', 12, '2026-09-01 09:00:00'),
(105, 'CP-2026-0105', 1, 3, 'Crédito de consumo', 8, 24, 86400, '2026-10-25', 'PENDIENTE', 0, '2026-10-01 09:00:00', 12, '2026-10-01 09:00:00'),
(106, 'CP-2026-0106', 2, 1, 'Cuota pie departamento', 14, 36, 310000, '2026-09-30', 'VENCIDO', 8500, '2026-10-01 00:00:00', 9, '2026-09-01 09:00:00'),
(107, 'CP-2026-0107', 2, 1, 'Cuota pie departamento', 15, 36, 310000, '2026-10-30', 'PENDIENTE', 0, '2026-10-01 09:00:00', 9, '2026-10-01 09:00:00'),
(108, 'CP-2026-0108', 3, 4, 'Seguro de hogar octubre', 10, 12, 18900, '2026-10-01', 'PAGADO', 0, '2026-09-30 19:05:00', NULL, '2026-09-01 09:00:00'),
(109, 'CP-2026-0109', 4, 7, 'Arancel octubre', 8, 10, 142000, '2026-10-05', 'EN_REVISION', 0, '2026-10-03 10:30:00', 10, '2026-10-01 09:00:00'),
(110, 'CP-2026-0110', 5, 2, 'Mensualidad octubre', 8, 10, 185000, '2026-10-10', 'PENDIENTE', 0, '2026-10-01 09:00:00', 10, '2026-10-01 09:00:00'),
(111, 'CP-2026-0111', 6, 3, 'Crédito de consumo', 19, 36, 54200, '2026-09-20', 'ANULADO', 0, '2026-09-22 10:00:00', 12, '2026-09-01 09:00:00'),
(112, 'CP-2026-0112', 7, 6, 'Cuota social octubre', 10, 12, 15000, '2026-10-05', 'PAGADO', 0, '2026-10-04 08:15:00', 13, '2026-10-01 09:00:00'),
(64, 'CP-2026-0064', 1, 6, 'Cuota social julio', 7, 12, 15000, '2026-08-05', 'PAGADO', 0, '2026-08-04 19:20:00', 13, '2026-07-01 09:00:00'),
(72, 'CP-2026-0072', 2, 1, 'Cuota pie departamento', 13, 36, 310000, '2026-08-31', 'PAGADO', 8500, '2026-09-02 21:14:00', 9, '2026-08-01 09:00:00'),
(81, 'CP-2026-0081', 5, 2, 'Matrícula 2026', NULL, NULL, 120000, '2026-09-10', 'ANULADO', 0, '2026-09-05 11:00:00', 10, '2026-09-01 09:00:00'),
(89, 'CP-2026-0089', 1, 8, 'Arriendo departamento septiembre', 9, 12, 520000, '2026-09-10', 'PAGADO', 0, '2026-09-09 13:05:00', 9, '2026-09-01 09:00:00'),
(90, 'CP-2026-0090', 1, 6, 'Cuota social septiembre', 9, 12, 15000, '2026-09-05', 'PAGADO', 2000, '2026-09-07 18:40:00', 13, '2026-09-01 09:00:00'),
(91, 'CP-2026-0091', 1, 5, 'Aporte mensual septiembre', NULL, NULL, 10000, '2026-09-15', 'PAGADO', 0, '2026-09-15 08:22:00', NULL, '2026-09-01 09:00:00'),
(95, 'CP-2026-0095', 8, 6, 'Cuota social octubre', 10, 12, 15000, '2026-10-05', 'EN_REVISION', 0, '2026-09-30 16:00:00', 13, '2026-09-01 09:00:00'),
(98, 'CP-2026-0098', 5, 2, 'Mensualidad septiembre', 7, 10, 185000, '2026-09-10', 'PAGADO', 0, '2026-09-10 22:31:00', 10, '2026-09-01 09:00:00');

INSERT INTO anulacion (id_compromiso, id_administrador, motivo, comentario, fecha) VALUES
(111, 12, 'REPACTACION', NULL, '2026-09-22 10:00:00'),
(81, 10, 'ERROR_REGISTRO', 'Cobro duplicado: la matrícula se pagó en marzo.', '2026-09-05 11:00:00');

INSERT INTO reporte (id_reporte, numero, id_compromiso, motivo, comentario, fecha_creacion, estado, id_administrador, fecha_toma, respuesta, fecha_resolucion) VALUES
(5, 'REP-0005', 64, 'OTRO', 'El concepto dice "agosto" y es la cuota de julio.', '2026-07-28 12:00:00', 'RESUELTO', 9, '2026-07-29 09:00:00', 'Se corrigió el concepto; el monto era correcto (30-07-2026).', '2026-07-30 11:00:00'),
(7, 'REP-0007', 81, 'NO_RECONOZCO', 'Me aparece una matrícula que ya pagué en marzo.', '2026-09-02 18:00:00', 'RESUELTO', 10, '2026-09-03 09:00:00', 'Cobro duplicado anulado por el colegio (05-09-2026).', '2026-09-05 12:00:00'),
(9, 'REP-0009', 111, 'MONTO_INCORRECTO', 'Repacté el crédito y me siguen cobrando la cuota antigua.', '2026-09-18 20:00:00', 'RESUELTO', 9, '2026-09-21 09:00:00', 'La empresa anuló la cuota por repactación (22-09-2026).', '2026-09-22 12:00:00'),
(10, 'REP-0010', 95, 'NO_RECONOZCO', 'Me llegó un cobro de un club al que no pertenezco.', '2026-09-30 16:00:00', 'ABIERTO', NULL, NULL, NULL, NULL),
(11, 'REP-0011', 109, 'YA_PAGUE', 'Pagué por transferencia directa al instituto el 03-10.', '2026-10-03 10:30:00', 'EN_GESTION', 10, '2026-10-05 09:40:00', NULL, NULL);

INSERT INTO recomendacion (id_recomendacion, numero, id_usuario_final, nombre_organizacion, rut_organizacion, id_rubro, id_comuna, fecha, estado) VALUES
(1, 'REC-0001', 1, 'Jardín Infantil Pequeños Exploradores', NULL, 9, 1, '2026-08-12 19:00:00', 'SE_UNIO'),
(2, 'REC-0002', 1, 'Comunidad Edificio Parque Los Olmos', NULL, 10, 1, '2026-09-02 20:00:00', 'CONTACTADA'),
(3, 'REC-0003', 3, 'Gimnasio Fuerza Sur', NULL, 11, 3, '2026-09-15 11:00:00', 'EN_EVALUACION'),
(4, 'REC-0004', 5, 'Academia de Música Clave de Sol', NULL, 12, 5, '2026-09-20 21:00:00', 'RECIBIDA'),
(5, 'REC-0005', 4, 'Colegio Bosque Nativo', NULL, 2, 4, '2026-09-22 13:00:00', 'DESCARTADA'),
(6, 'REC-0006', 7, 'Corporación Deportiva Cordillera', NULL, 6, 7, '2026-10-01 18:00:00', 'RECIBIDA');

INSERT INTO contacto_recomendacion (id_recomendacion, nombre, correo, telefono, autoriza_contacto) VALUES
(1, 'Paula Herrera', 'pherrera@pexploradores.ejemplo', NULL, TRUE),
(3, 'Raúl Ibáñez', NULL, '+56 9 0000 0301', TRUE),
(6, 'Ana Valdés', 'avaldes@cdcordillera.ejemplo', NULL, TRUE);

INSERT INTO historial_recomendacion (id_historial, id_recomendacion, estado, id_administrador, fecha, motivo) VALUES
(1, 1, 'SE_UNIO', 12, '2026-09-25 10:00:00', NULL),
(2, 2, 'CONTACTADA', 13, '2026-09-10 10:00:00', NULL),
(3, 3, 'EN_EVALUACION', 12, '2026-09-28 10:00:00', NULL),
(4, 5, 'DESCARTADA', 13, '2026-09-23 10:00:00', 'La organización ya es cliente de PagoFlex');
