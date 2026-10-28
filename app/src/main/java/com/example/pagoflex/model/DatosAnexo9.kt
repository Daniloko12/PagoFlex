package com.example.pagoflex.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

object DatosAnexo9 {
    private fun f(dia: Int, mes: Int, anio: Int): LocalDate = LocalDate.of(anio, mes, dia)
    private fun fh(dia: Int, mes: Int, anio: Int, hora: Int = 0, minuto: Int = 0): LocalDateTime =
        LocalDateTime.of(anio, mes, dia, hora, minuto)

    val FECHA_HOY: LocalDate = f(6, 10, 2026)
    val ULTIMA_ACTUALIZACION: LocalDateTime = fh(6, 10, 2026, 8, 30)

    val comunas = listOf(
        Comuna(1, "Maipú"),
        Comuna(2, "Puente Alto"),
        Comuna(3, "San Miguel"),
        Comuna(4, "La Florida"),
        Comuna(5, "Ñuñoa"),
        Comuna(6, "Quilicura"),
        Comuna(7, "San Bernardo"),
        Comuna(8, "Estación Central"),
        Comuna(9, "Huechuraba"),
        Comuna(10, "Independencia"),
        Comuna(11, "Las Condes"),
        Comuna(12, "Peñalolén"),
        Comuna(13, "Providencia"),
        Comuna(14, "Recoleta"),
        Comuna(15, "Santiago")
    )

    val rubros = listOf(
        Rubro(1, "Inmobiliaria"),
        Rubro(2, "Educación escolar"),
        Rubro(3, "Crédito de consumo"),
        Rubro(4, "Seguros"),
        Rubro(5, "ONG"),
        Rubro(6, "Club deportivo"),
        Rubro(7, "Educación superior"),
        Rubro(8, "Administración de arriendos"),
        Rubro(9, "Educación parvularia"),
        Rubro(10, "Administración de condominio"),
        Rubro(11, "Deporte"),
        Rubro(12, "Educación artística"),
        Rubro(13, "Salud"),
        Rubro(14, "Otro")
    )

    val usuarios = listOf(
        Usuario(1, Rol.USUARIO_FINAL, "15.834.207-3", "Camila Andrea", "Rojas Peña", "crojas@correo.ejemplo",
            "+56 9 0000 0101", fechaCreacion = fh(14, 3, 2023), idComuna = 1,
            fechaIncorporacion = f(14, 3, 2023), avisosActivos = true, diasAnticipacion = 3),
        Usuario(2, Rol.USUARIO_FINAL, "12.476.318-5", "Luis Alberto", "Muñoz Tapia", "lmunoz@correo.ejemplo",
            "+56 9 0000 0102", fechaCreacion = fh(2, 11, 2023), idComuna = 2,
            fechaIncorporacion = f(2, 11, 2023), avisosActivos = true, diasAnticipacion = 1),
        Usuario(3, Rol.USUARIO_FINAL, "6.893.541-5", "Rosa Elena", "Contreras Díaz", "rcontreras@correo.ejemplo",
            "+56 9 0000 0103", fechaCreacion = fh(20, 6, 2024), idComuna = 3,
            fechaIncorporacion = f(20, 6, 2024), avisosActivos = true, diasAnticipacion = 5),
        Usuario(4, Rol.USUARIO_FINAL, "21.045.873-5", "Matías Ignacio", "Soto Vergara", "msoto@correo.ejemplo",
            "+56 9 0000 0104", fechaCreacion = fh(7, 3, 2025), idComuna = 4,
            fechaIncorporacion = f(7, 3, 2025), avisosActivos = false, diasAnticipacion = 3),
        Usuario(5, Rol.USUARIO_FINAL, "17.562.904-1", "Fernanda Paz", "Godoy Lagos", "fgodoy@correo.ejemplo",
            "+56 9 0000 0105", fechaCreacion = fh(15, 1, 2024), idComuna = 5,
            fechaIncorporacion = f(15, 1, 2024), avisosActivos = true, diasAnticipacion = 3),
        Usuario(6, Rol.USUARIO_FINAL, "10.318.265-4", "Héctor Manuel", "Pizarro Campos", "hpizarro@correo.ejemplo",
            "+56 9 0000 0106", fechaCreacion = fh(28, 8, 2023), idComuna = 6,
            fechaIncorporacion = f(28, 8, 2023), avisosActivos = true, diasAnticipacion = 3),
        Usuario(7, Rol.USUARIO_FINAL, "19.287.430-0", "Javiera Constanza", "Reyes Molina", "jreyes@correo.ejemplo",
            "+56 9 0000 0107", fechaCreacion = fh(3, 5, 2025), idComuna = 7,
            fechaIncorporacion = f(3, 5, 2025), avisosActivos = true, diasAnticipacion = 1),
        Usuario(8, Rol.USUARIO_FINAL, "18.704.396-4", "Benjamín Esteban", "Araya Fuentes", "baraya@correo.ejemplo",
            "+56 9 0000 0108", fechaCreacion = fh(11, 9, 2026), idComuna = 8,
            fechaIncorporacion = f(11, 9, 2026), avisosActivos = true, diasAnticipacion = 3),
        Usuario(9, Rol.ADMINISTRADOR, "16.248.317-K", "Andrés Felipe", "Lagos Meza", "alagos@pagoflex.ejemplo",
            "+56 9 0000 0201", fechaCreacion = fh(2, 1, 2026)),
        Usuario(10, Rol.ADMINISTRADOR, "18.352.091-1", "Daniela Sofía", "Ortiz Bravo", "dortiz@pagoflex.ejemplo",
            "+56 9 0000 0202", fechaCreacion = fh(2, 1, 2026)),
        Usuario(11, Rol.ADMINISTRADOR, "13.907.264-2", "Ricardo José", "Salinas Cáceres", "rsalinas@pagoflex.ejemplo",
            "+56 9 0000 0203", activo = false, fechaCreacion = fh(2, 1, 2026)),
        Usuario(12, Rol.ADMINISTRADOR, "17.781.435-0", "Valentina Ignacia", "Paredes Silva", "vparedes@pagoflex.ejemplo",
            "+56 9 0000 0204", fechaCreacion = fh(2, 1, 2026)),
        Usuario(13, Rol.ADMINISTRADOR, "19.564.028-9", "Tomás Alejandro", "Vidal Rojas", "tvidal@pagoflex.ejemplo",
            "+56 9 0000 0205", fechaCreacion = fh(2, 1, 2026))
    )

    val empresas = listOf(
        EmpresaCliente(1, "EC-01", "Inmobiliaria Altos del Maipo SpA", "Altos del Maipo", "76.845.213-K", 1,
            "atencion@altosmaipo.ejemplo", "+56 2 0000 0201", 8_500),
        EmpresaCliente(2, "EC-02", "Corporación Educacional Bosque Nativo", "Colegio Bosque Nativo", "65.132.874-8", 2,
            "pagos@bosquenativo.ejemplo", "+56 2 0000 0202", 5_000),
        EmpresaCliente(3, "EC-03", "Crédito Andino S.A.", "Crédito Andino", "96.571.438-3", 3,
            "clientes@creditoandino.ejemplo", "+56 2 0000 0203", 12_000),
        EmpresaCliente(4, "EC-04", "Seguros Horizonte Sur S.A.", "Horizonte Sur", "96.814.205-4", 4,
            "contacto@horizontesur.ejemplo", "+56 2 0000 0204", 3_000),
        EmpresaCliente(5, "EC-05", "Fundación Manos Abiertas", "Manos Abiertas", "65.247.190-0", 5,
            "socios@manosabiertas.ejemplo", "+56 2 0000 0205", 0),
        EmpresaCliente(6, "EC-06", "Club Deportivo Los Halcones", "Club Los Halcones", "65.398.412-K", 6,
            "tesoreria@loshalcones.ejemplo", "+56 2 0000 0206", 2_000),
        EmpresaCliente(7, "EC-07", "Instituto Técnico Cordillera SpA", "Instituto Cordillera", "77.214.583-7", 7,
            "finanzas@itcordillera.ejemplo", "+56 2 0000 0207", 6_000),
        EmpresaCliente(8, "EC-08", "Arriendos Plaza Oriente Ltda.", "Plaza Oriente", "76.432.871-K", 8,
            "cobranza@plazaoriente.ejemplo", "+56 2 0000 0208", 7_500)
    )

    val compromisos = listOf(
        Compromiso(101, "CP-2026-0101", 1, 8, "Arriendo departamento octubre", 10, 12, 520_000, f(10, 10, 2026),
            EstadoCompromiso.PENDIENTE, 0, fh(1, 10, 2026, 9), 9, fh(1, 10, 2026, 9)),
        Compromiso(102, "CP-2026-0102", 1, 6, "Cuota social octubre", 10, 12, 15_000, f(5, 10, 2026),
            EstadoCompromiso.VENCIDO, 2_000, fh(6, 10, 2026), 13, fh(1, 10, 2026, 9)),
        Compromiso(103, "CP-2026-0103", 1, 5, "Aporte mensual octubre", null, null, 10_000, f(15, 10, 2026),
            EstadoCompromiso.PENDIENTE, 0, fh(1, 10, 2026, 9), null, fh(1, 10, 2026, 9)),
        Compromiso(104, "CP-2026-0104", 1, 3, "Crédito de consumo", 7, 24, 86_400, f(25, 9, 2026),
            EstadoCompromiso.PAGADO, 0, fh(24, 9, 2026, 10, 42), 12, fh(1, 9, 2026, 9)),
        Compromiso(105, "CP-2026-0105", 1, 3, "Crédito de consumo", 8, 24, 86_400, f(25, 10, 2026),
            EstadoCompromiso.PENDIENTE, 0, fh(1, 10, 2026, 9), 12, fh(1, 10, 2026, 9)),
        Compromiso(106, "CP-2026-0106", 2, 1, "Cuota pie departamento", 14, 36, 310_000, f(30, 9, 2026),
            EstadoCompromiso.VENCIDO, 8_500, fh(1, 10, 2026), 9, fh(1, 9, 2026, 9)),
        Compromiso(107, "CP-2026-0107", 2, 1, "Cuota pie departamento", 15, 36, 310_000, f(30, 10, 2026),
            EstadoCompromiso.PENDIENTE, 0, fh(1, 10, 2026, 9), 9, fh(1, 10, 2026, 9)),
        Compromiso(108, "CP-2026-0108", 3, 4, "Seguro de hogar octubre", 10, 12, 18_900, f(1, 10, 2026),
            EstadoCompromiso.PAGADO, 0, fh(30, 9, 2026, 19, 5), null, fh(1, 9, 2026, 9)),
        Compromiso(109, "CP-2026-0109", 4, 7, "Arancel octubre", 8, 10, 142_000, f(5, 10, 2026),
            EstadoCompromiso.EN_REVISION, 0, fh(3, 10, 2026, 10, 30), 10, fh(1, 10, 2026, 9)),
        Compromiso(110, "CP-2026-0110", 5, 2, "Mensualidad octubre", 8, 10, 185_000, f(10, 10, 2026),
            EstadoCompromiso.PENDIENTE, 0, fh(1, 10, 2026, 9), 10, fh(1, 10, 2026, 9)),
        Compromiso(111, "CP-2026-0111", 6, 3, "Crédito de consumo", 19, 36, 54_200, f(20, 9, 2026),
            EstadoCompromiso.ANULADO, 0, fh(22, 9, 2026, 10), 12, fh(1, 9, 2026, 9)),
        Compromiso(112, "CP-2026-0112", 7, 6, "Cuota social octubre", 10, 12, 15_000, f(5, 10, 2026),
            EstadoCompromiso.PAGADO, 0, fh(4, 10, 2026, 8, 15), 13, fh(1, 10, 2026, 9)),
        Compromiso(64, "CP-2026-0064", 1, 6, "Cuota social julio", 7, 12, 15_000, f(5, 8, 2026),
            EstadoCompromiso.PAGADO, 0, fh(4, 8, 2026, 19, 20), 13, fh(1, 7, 2026, 9)),
        Compromiso(72, "CP-2026-0072", 2, 1, "Cuota pie departamento", 13, 36, 310_000, f(31, 8, 2026),
            EstadoCompromiso.PAGADO, 8_500, fh(2, 9, 2026, 21, 14), 9, fh(1, 8, 2026, 9)),
        Compromiso(81, "CP-2026-0081", 5, 2, "Matrícula 2026", null, null, 120_000, f(10, 9, 2026),
            EstadoCompromiso.ANULADO, 0, fh(5, 9, 2026, 11), 10, fh(1, 9, 2026, 9)),
        Compromiso(89, "CP-2026-0089", 1, 8, "Arriendo departamento septiembre", 9, 12, 520_000, f(10, 9, 2026),
            EstadoCompromiso.PAGADO, 0, fh(9, 9, 2026, 13, 5), 9, fh(1, 9, 2026, 9)),
        Compromiso(90, "CP-2026-0090", 1, 6, "Cuota social septiembre", 9, 12, 15_000, f(5, 9, 2026),
            EstadoCompromiso.PAGADO, 2_000, fh(7, 9, 2026, 18, 40), 13, fh(1, 9, 2026, 9)),
        Compromiso(91, "CP-2026-0091", 1, 5, "Aporte mensual septiembre", null, null, 10_000, f(15, 9, 2026),
            EstadoCompromiso.PAGADO, 0, fh(15, 9, 2026, 8, 22), null, fh(1, 9, 2026, 9)),
        Compromiso(95, "CP-2026-0095", 8, 6, "Cuota social octubre", 10, 12, 15_000, f(5, 10, 2026),
            EstadoCompromiso.EN_REVISION, 0, fh(30, 9, 2026, 16), 13, fh(1, 9, 2026, 9)),
        Compromiso(98, "CP-2026-0098", 5, 2, "Mensualidad septiembre", 7, 10, 185_000, f(10, 9, 2026),
            EstadoCompromiso.PAGADO, 0, fh(10, 9, 2026, 22, 31), 10, fh(1, 9, 2026, 9))
    )

    val pagos = listOf(
        Pago(1, "CPR-000377", 72, fh(2, 9, 2026, 21, 14), 318_500, Canal.AGENTE_CONVERSACIONAL, MedioPago.SIMULADO),
        Pago(2, "CPR-000398", 89, fh(9, 9, 2026, 13, 5), 520_000, Canal.PORTAL, MedioPago.SIMULADO),
        Pago(3, "CPR-000402", 90, fh(7, 9, 2026, 18, 40), 17_000, Canal.PORTAL, MedioPago.SIMULADO),
        Pago(4, "CPR-000405", 91, fh(15, 9, 2026, 8, 22), 10_000, Canal.AGENTE_CONVERSACIONAL, MedioPago.SIMULADO),
        Pago(5, "CPR-000451", 104, fh(24, 9, 2026, 10, 42), 86_400, Canal.PORTAL, MedioPago.DEBITO),
        Pago(6, "CPR-000452", 108, fh(30, 9, 2026, 19, 5), 18_900, Canal.AGENTE_CONVERSACIONAL, MedioPago.SIMULADO),
        Pago(7, "CPR-000453", 112, fh(4, 10, 2026, 8, 15), 15_000, Canal.APP, MedioPago.SIMULADO),
        Pago(8, "CPR-000447", 98, fh(10, 9, 2026, 22, 31), 185_000, Canal.PORTAL, MedioPago.SIMULADO),
        Pago(9, "CPR-000331", 64, fh(4, 8, 2026, 19, 20), 15_000, Canal.PORTAL, MedioPago.SIMULADO)
    )

    val anulaciones = listOf(
        Anulacion(111, 12, MotivoAnulacion.REPACTACION, null, fh(22, 9, 2026, 10)),
        Anulacion(81, 10, MotivoAnulacion.ERROR_REGISTRO, "Cobro duplicado: la matrícula se pagó en marzo.", fh(5, 9, 2026, 11))
    )

    val reportes = listOf(
        Reporte(5, "REP-0005", 64, MotivoReporte.OTRO, "El concepto dice \"agosto\" y es la cuota de julio.",
            fh(28, 7, 2026, 12), EstadoReporte.RESUELTO, 9, fh(29, 7, 2026, 9),
            "Se corrigió el concepto; el monto era correcto (30-07-2026).", fh(30, 7, 2026, 11)),
        Reporte(7, "REP-0007", 81, MotivoReporte.NO_RECONOZCO, "Me aparece una matrícula que ya pagué en marzo.",
            fh(2, 9, 2026, 18), EstadoReporte.RESUELTO, 10, fh(3, 9, 2026, 9),
            "Cobro duplicado anulado por el colegio (05-09-2026).", fh(5, 9, 2026, 12)),
        Reporte(9, "REP-0009", 111, MotivoReporte.MONTO_INCORRECTO, "Repacté el crédito y me siguen cobrando la cuota antigua.",
            fh(18, 9, 2026, 20), EstadoReporte.RESUELTO, 9, fh(21, 9, 2026, 9),
            "La empresa anuló la cuota por repactación (22-09-2026).", fh(22, 9, 2026, 12)),
        Reporte(10, "REP-0010", 95, MotivoReporte.NO_RECONOZCO, "Me llegó un cobro de un club al que no pertenezco.",
            fh(30, 9, 2026, 16), EstadoReporte.ABIERTO),
        Reporte(11, "REP-0011", 109, MotivoReporte.YA_PAGUE, "Pagué por transferencia directa al instituto el 03-10.",
            fh(3, 10, 2026, 10, 30), EstadoReporte.EN_GESTION, 10, fh(5, 10, 2026, 9, 40))
    )

    val recomendaciones = listOf(
        Recomendacion(1, "REC-0001", 1, "Jardín Infantil Pequeños Exploradores", null, 9, 1, fh(12, 8, 2026, 19), EstadoRecomendacion.SE_UNIO),
        Recomendacion(2, "REC-0002", 1, "Comunidad Edificio Parque Los Olmos", null, 10, 1, fh(2, 9, 2026, 20), EstadoRecomendacion.CONTACTADA),
        Recomendacion(3, "REC-0003", 3, "Gimnasio Fuerza Sur", null, 11, 3, fh(15, 9, 2026, 11), EstadoRecomendacion.EN_EVALUACION),
        Recomendacion(4, "REC-0004", 5, "Academia de Música Clave de Sol", null, 12, 5, fh(20, 9, 2026, 21), EstadoRecomendacion.RECIBIDA),
        Recomendacion(5, "REC-0005", 4, "Colegio Bosque Nativo", null, 2, 4, fh(22, 9, 2026, 13), EstadoRecomendacion.DESCARTADA),
        Recomendacion(6, "REC-0006", 7, "Corporación Deportiva Cordillera", null, 6, 7, fh(1, 10, 2026, 18), EstadoRecomendacion.RECIBIDA)
    )

    val contactosRecomendacion = listOf(
        ContactoRecomendacion(1, "Paula Herrera", "pherrera@pexploradores.ejemplo", null, true),
        ContactoRecomendacion(3, "Raúl Ibáñez", null, "+56 9 0000 0301", true),
        ContactoRecomendacion(6, "Ana Valdés", "avaldes@cdcordillera.ejemplo", null, true)
    )

    val historialRecomendaciones = listOf(
        HistorialRecomendacion(1, 1, EstadoRecomendacion.SE_UNIO, 12, fh(25, 9, 2026, 10)),
        HistorialRecomendacion(2, 2, EstadoRecomendacion.CONTACTADA, 13, fh(10, 9, 2026, 10)),
        HistorialRecomendacion(3, 3, EstadoRecomendacion.EN_EVALUACION, 12, fh(28, 9, 2026, 10)),
        HistorialRecomendacion(4, 5, EstadoRecomendacion.DESCARTADA, 13, fh(23, 9, 2026, 10),
            "La organización ya es cliente de PagoFlex")
    )

    val tarjetas = listOf(
        Tarjeta(1, 1, MedioPago.DEBITO, MarcaTarjeta.VISA, "4821", "CAMILA ROJAS",
            YearMonth.of(2028, 8), true, fh(20, 8, 2026, 10)),
        Tarjeta(2, 1, MedioPago.CREDITO, MarcaTarjeta.MASTERCARD, "1934", "CAMILA ROJAS",
            YearMonth.of(2027, 11), true, fh(2, 9, 2026, 18)),
        Tarjeta(3, 2, MedioPago.DEBITO, MarcaTarjeta.VISA, "7310", "LUIS MUNOZ",
            YearMonth.of(2029, 3), true, fh(15, 9, 2026, 12))
    )

    const val RECONOCIMIENTO_PRIMER_PAGO_APP = 1
    const val RECONOCIMIENTO_AL_DIA = 2
    const val RECONOCIMIENTO_EMBAJADOR = 3

    val reconocimientos = listOf(
        Reconocimiento(RECONOCIMIENTO_PRIMER_PAGO_APP, "Primer pago desde la app",
            "Hiciste tu primer pago desde la app PagoFlex."),
        Reconocimiento(RECONOCIMIENTO_AL_DIA, "¡Te pusiste al día!",
            "Pagaste todos tus compromisos vencidos."),
        Reconocimiento(RECONOCIMIENTO_EMBAJADOR, "Embajador/a PagoFlex",
            "Una organización que recomendaste se unió a PagoFlex.")
    )

    val reconocimientosObtenidos = listOf(
        UsuarioReconocimiento(1, RECONOCIMIENTO_EMBAJADOR, fh(25, 9, 2026, 10),
            "Jardín Infantil Pequeños Exploradores se unió a PagoFlex gracias a tu recomendación"),
        UsuarioReconocimiento(7, RECONOCIMIENTO_PRIMER_PAGO_APP, fh(4, 10, 2026, 8, 15),
            "Pagaste \"Cuota social octubre\" desde la app")
    )

    val cumplimientoHistorico = listOf(
        CumplimientoHistorico(1, 36, 33),
        CumplimientoHistorico(2, 12, 8),
        CumplimientoHistorico(3, 28, 28),
        CumplimientoHistorico(4, 7, 6),
        CumplimientoHistorico(5, 20, 19),
        CumplimientoHistorico(6, 14, 12),
        CumplimientoHistorico(7, 5, 5),
        CumplimientoHistorico(8, 0, 0)
    )

    val MES_RESUMEN_ANEXO: YearMonth = YearMonth.of(2026, 9)

    val resumenCobranzaSeptiembre = listOf(
        ResumenCobranzaMes(1, MES_RESUMEN_ANEXO, 120, 2, 104, 14, 32_476_000),
        ResumenCobranzaMes(2, MES_RESUMEN_ANEXO, 310, 1, 296, 13, 54_835_000),
        ResumenCobranzaMes(3, MES_RESUMEN_ANEXO, 845, 6, 702, 137, 61_538_400),
        ResumenCobranzaMes(6, MES_RESUMEN_ANEXO, 96, 0, 89, 7, 1_349_000),
        ResumenCobranzaMes(7, MES_RESUMEN_ANEXO, 410, 3, 361, 46, 51_392_000),
        ResumenCobranzaMes(8, MES_RESUMEN_ANEXO, 64, 0, 61, 3, 31_845_000)
    )
}
