INSERT INTO comuna (id_comuna, nombre) VALUES
(1, 'Maipú'),
(2, 'Puente Alto'),
(3, 'San Miguel'),
(4, 'La Florida'),
(5, 'Ñuñoa'),
(6, 'Quilicura'),
(7, 'San Bernardo'),
(8, 'Estación Central'),
(9, 'Huechuraba'),
(10, 'Independencia'),
(11, 'Las Condes'),
(12, 'Peñalolén'),
(13, 'Providencia'),
(14, 'Recoleta'),
(15, 'Santiago');

INSERT INTO reconocimiento (id_reconocimiento, nombre, descripcion) VALUES
(1, 'Primer pago desde la app', 'Hiciste tu primer pago desde la app PagoFlex.'),
(2, '¡Te pusiste al día!', 'Pagaste todos tus compromisos vencidos.'),
(3, 'Embajador/a PagoFlex', 'Una organización que recomendaste se unió a PagoFlex.');

INSERT INTO usuario (id_usuario, rol, rut, nombres, apellidos, correo, telefono, activo, fecha_creacion, id_comuna, fecha_incorporacion, avisos_activos, dias_anticipacion, clave_hash) VALUES
(1, 'USUARIO_FINAL', '15.834.207-3', 'Camila Andrea', 'Rojas Peña', 'crojas@correo.ejemplo', '+56 9 0000 0101', TRUE, '2023-03-14 00:00:00', 1, '2023-03-14', TRUE, 3, '0cb67dfc6a5a4c1a04196cf90d05bada4f5f940092f5dcd26f563e2861e923f6'),
(2, 'USUARIO_FINAL', '12.476.318-5', 'Luis Alberto', 'Muñoz Tapia', 'lmunoz@correo.ejemplo', '+56 9 0000 0102', TRUE, '2023-11-02 00:00:00', 2, '2023-11-02', TRUE, 1, '6619149a9fb43a305c8852d807f04eeb19aed920afd6fff5eec18f8561a22da6'),
(3, 'USUARIO_FINAL', '6.893.541-5', 'Rosa Elena', 'Contreras Díaz', 'rcontreras@correo.ejemplo', '+56 9 0000 0103', TRUE, '2024-06-20 00:00:00', 3, '2024-06-20', TRUE, 5, '16753482354777560f3da6324be7e7a18796b5f00086daf329ad3eefd461c35d'),
(4, 'USUARIO_FINAL', '21.045.873-5', 'Matías Ignacio', 'Soto Vergara', 'msoto@correo.ejemplo', '+56 9 0000 0104', TRUE, '2025-03-07 00:00:00', 4, '2025-03-07', FALSE, 3, 'a473916292f4039cea089c82ccdb3f7b4eee76e047be08525f16950fb5baaefc'),
(5, 'USUARIO_FINAL', '17.562.904-1', 'Fernanda Paz', 'Godoy Lagos', 'fgodoy@correo.ejemplo', '+56 9 0000 0105', TRUE, '2024-01-15 00:00:00', 5, '2024-01-15', TRUE, 3, '70f00444c1421c7062d272837100f90699d0daf59104ef01ed4759eb80b614c2'),
(6, 'USUARIO_FINAL', '10.318.265-4', 'Héctor Manuel', 'Pizarro Campos', 'hpizarro@correo.ejemplo', '+56 9 0000 0106', TRUE, '2023-08-28 00:00:00', 6, '2023-08-28', TRUE, 3, 'cbd219aae56143005d33a220bebf4c72cb7963a9c7449c02aafeb6e5dfc963b7'),
(7, 'USUARIO_FINAL', '19.287.430-0', 'Javiera Constanza', 'Reyes Molina', 'jreyes@correo.ejemplo', '+56 9 0000 0107', TRUE, '2025-05-03 00:00:00', 7, '2025-05-03', TRUE, 1, 'f9e3e0abee3a2c14c59d5589f9788211ae4dda18ade25ffa7a74e420d7e405b8'),
(8, 'USUARIO_FINAL', '18.704.396-4', 'Benjamín Esteban', 'Araya Fuentes', 'baraya@correo.ejemplo', '+56 9 0000 0108', TRUE, '2026-09-11 00:00:00', 8, '2026-09-11', TRUE, 3, '8a1f06f318cf4ba6a1296f7271bb5352a86d4855fa932d97c104f7894080999a'),
(9, 'ADMINISTRADOR', '16.248.317-K', 'Andrés Felipe', 'Lagos Meza', 'alagos@pagoflex.ejemplo', '+56 9 0000 0201', TRUE, '2026-01-02 00:00:00', NULL, NULL, NULL, NULL, '7cad90ad1483c9e411d338670359fd625a888c09312b8c851851be15da23a0ca'),
(10, 'ADMINISTRADOR', '18.352.091-1', 'Daniela Sofía', 'Ortiz Bravo', 'dortiz@pagoflex.ejemplo', '+56 9 0000 0202', TRUE, '2026-01-02 00:00:00', NULL, NULL, NULL, NULL, 'd362cfa5b3045a7379f23ff40ebda53cbc2ca587b96a98a9055e7673b9b96a77'),
(11, 'ADMINISTRADOR', '13.907.264-2', 'Ricardo José', 'Salinas Cáceres', 'rsalinas@pagoflex.ejemplo', '+56 9 0000 0203', FALSE, '2026-01-02 00:00:00', NULL, NULL, NULL, NULL, '0b4e659160890f52d8b58235765322e0fb5792fdac4dc0cca46424cd2dd72906'),
(12, 'ADMINISTRADOR', '17.781.435-0', 'Valentina Ignacia', 'Paredes Silva', 'vparedes@pagoflex.ejemplo', '+56 9 0000 0204', TRUE, '2026-01-02 00:00:00', NULL, NULL, NULL, NULL, '0c11dff8c76abae5c8a706548683aa49cbeff9d2b87a251bb24fa344c24f5418'),
(13, 'ADMINISTRADOR', '19.564.028-9', 'Tomás Alejandro', 'Vidal Rojas', 'tvidal@pagoflex.ejemplo', '+56 9 0000 0205', TRUE, '2026-01-02 00:00:00', NULL, NULL, NULL, NULL, '44e2cc453abb0607232275d3bb04906a2300bc1126a48bdeadf47afba294285a');

INSERT INTO usuario_reconocimiento (id_usuario_final, id_reconocimiento, fecha_obtencion, motivo) VALUES
(1, 3, '2026-09-25 10:00:00', 'Jardín Infantil Pequeños Exploradores se unió a PagoFlex gracias a tu recomendación'),
(7, 1, '2026-10-04 08:15:00', 'Pagaste "Cuota social octubre" desde la app');
