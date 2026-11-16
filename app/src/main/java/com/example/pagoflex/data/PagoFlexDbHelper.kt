package com.example.pagoflex.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pagoflex.model.Anulacion
import com.example.pagoflex.model.BaseDatos
import com.example.pagoflex.model.CLAVE_ANTERIOR_DE_PRUEBA
import com.example.pagoflex.model.CLAVE_DE_PRUEBA
import com.example.pagoflex.model.Canal
import com.example.pagoflex.model.CodigoRecuperacion
import com.example.pagoflex.model.Compromiso
import com.example.pagoflex.model.Comuna
import com.example.pagoflex.model.ContactoRecomendacion
import com.example.pagoflex.model.DatosAnexo9
import com.example.pagoflex.model.EmpresaCliente
import com.example.pagoflex.model.EstadoCompromiso
import com.example.pagoflex.model.EstadoRecomendacion
import com.example.pagoflex.model.EstadoReporte
import com.example.pagoflex.model.HistorialRecomendacion
import com.example.pagoflex.model.MarcaTarjeta
import com.example.pagoflex.model.MedioPago
import com.example.pagoflex.model.MotivoAnulacion
import com.example.pagoflex.model.MotivoReporte
import com.example.pagoflex.model.Movimiento
import com.example.pagoflex.model.Pago
import com.example.pagoflex.model.Recomendacion
import com.example.pagoflex.model.Reconocimiento
import com.example.pagoflex.model.Reporte
import com.example.pagoflex.model.Rol
import com.example.pagoflex.model.Rubro
import com.example.pagoflex.model.Tarjeta
import com.example.pagoflex.model.TipoMovimiento
import com.example.pagoflex.model.Usuario
import com.example.pagoflex.model.UsuarioReconocimiento
import com.example.pagoflex.model.actualizarVencimientos
import com.example.pagoflex.model.hashClave
import com.example.pagoflex.model.movimientosIniciales
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

class PagoFlexDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {
    companion object {
        const val NOMBRE_BD = "pagoflex.db"
        private const val VERSION_BD = 7

        private val TABLAS = listOf(
            "rol", "comuna", "rubro", "estado_compromiso", "motivo_anulacion", "canal",
            "medio_pago", "marca_tarjeta", "motivo_reporte", "estado_reporte", "estado_recomendacion", "tipo_movimiento",
            "reconocimiento", "empresa_cliente", "usuario", "tarjeta", "compromiso", "anulacion",
            "pago", "reporte", "recomendacion", "contacto_recomendacion", "historial_recomendacion",
            "usuario_reconocimiento", "codigo_recuperacion", "movimiento", "boleta"
        )

        private val TABLAS_ANTIGUAS = listOf("area")

        private fun idDe(valor: Enum<*>): Int = valor.ordinal + 1

        private const val SQL_CREAR_CODIGO_RECUPERACION =
            "CREATE TABLE codigo_recuperacion (" +
                "id_codigo INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "codigo_hash TEXT NOT NULL CHECK (length(codigo_hash) = 64), " +
                "fecha_creacion TEXT NOT NULL, " +
                "fecha_expiracion TEXT NOT NULL, " +
                "intentos INTEGER NOT NULL DEFAULT 0 CHECK (intentos >= 0), " +
                "usado INTEGER NOT NULL DEFAULT 0 CHECK (usado IN (0, 1)))"

        private const val SQL_CREAR_TIPO_MOVIMIENTO =
            "CREATE TABLE tipo_movimiento (id_tipo_movimiento INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_MOVIMIENTO =
            "CREATE TABLE movimiento (" +
                "id_movimiento INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_tipo_movimiento INTEGER NOT NULL REFERENCES tipo_movimiento(id_tipo_movimiento), " +
                "fecha TEXT NOT NULL, " +
                "id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "id_autor INTEGER REFERENCES usuario(id_usuario), " +
                "id_compromiso INTEGER REFERENCES compromiso(id_compromiso), " +
                "detalle TEXT NOT NULL)"

        private const val SQL_INDICE_MOVIMIENTO =
            "CREATE INDEX ix_movimiento_usuario ON movimiento (id_usuario, fecha)"

        private const val SQL_CREAR_BOLETA =
            "CREATE TABLE boleta (" +
                "id_compromiso INTEGER PRIMARY KEY REFERENCES compromiso(id_compromiso), " +
                "imagen BLOB NOT NULL, " +
                "fecha_registro TEXT NOT NULL)"

        private val SQL_CREAR_TABLAS = listOf(
            "CREATE TABLE rol (id_rol INTEGER PRIMARY KEY, codigo TEXT NOT NULL UNIQUE, " +
                "nombre TEXT NOT NULL, descripcion TEXT)",
            "CREATE TABLE comuna (id_comuna INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE rubro (id_rubro INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE estado_compromiso (id_estado_compromiso INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE motivo_anulacion (id_motivo_anulacion INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE canal (id_canal INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE medio_pago (id_medio_pago INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE marca_tarjeta (id_marca_tarjeta INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE motivo_reporte (id_motivo_reporte INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE estado_reporte (id_estado_reporte INTEGER PRIMARY KEY, nombre TEXT NOT NULL UNIQUE)",
            "CREATE TABLE estado_recomendacion (id_estado_recomendacion INTEGER PRIMARY KEY, " +
                "nombre TEXT NOT NULL UNIQUE, orden INTEGER NOT NULL)",
            "CREATE TABLE reconocimiento (id_reconocimiento INTEGER PRIMARY KEY, " +
                "nombre TEXT NOT NULL UNIQUE, descripcion TEXT NOT NULL)",

            "CREATE TABLE empresa_cliente (" +
                "id_empresa INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "codigo TEXT NOT NULL UNIQUE, " +
                "razon_social TEXT NOT NULL, " +
                "nombre_fantasia TEXT NOT NULL, " +
                "rut TEXT NOT NULL UNIQUE, " +
                "id_rubro INTEGER NOT NULL REFERENCES rubro(id_rubro), " +
                "correo_atencion TEXT NOT NULL, " +
                "telefono_atencion TEXT NOT NULL, " +
                "recargo_atraso INTEGER NOT NULL CHECK (recargo_atraso >= 0), " +
                "activa INTEGER NOT NULL DEFAULT 1)",
            "CREATE TABLE usuario (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_rol INTEGER NOT NULL REFERENCES rol(id_rol), " +
                "rut TEXT NOT NULL UNIQUE, " +
                "nombres TEXT NOT NULL, " +
                "apellidos TEXT NOT NULL, " +
                "correo TEXT NOT NULL UNIQUE COLLATE NOCASE, " +
                "clave_hash TEXT NOT NULL CHECK (length(clave_hash) = 64), " +
                "telefono TEXT, " +
                "activo INTEGER NOT NULL DEFAULT 1, " +
                "fecha_creacion TEXT NOT NULL, " +
                "id_comuna INTEGER REFERENCES comuna(id_comuna), " +
                "fecha_incorporacion TEXT, " +
                "avisos_activos INTEGER, " +
                "dias_anticipacion INTEGER CHECK (dias_anticipacion IS NULL OR dias_anticipacion IN (1, 3, 5)), " +
                "avatar BLOB)",
            "CREATE TABLE tarjeta (" +
                "id_tarjeta INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_usuario INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "id_medio_pago INTEGER NOT NULL REFERENCES medio_pago(id_medio_pago) " +
                "CHECK (id_medio_pago IN (${idDe(MedioPago.DEBITO)}, ${idDe(MedioPago.CREDITO)})), " +
                "id_marca_tarjeta INTEGER NOT NULL REFERENCES marca_tarjeta(id_marca_tarjeta), " +
                "ultimos4 TEXT NOT NULL CHECK (length(ultimos4) = 4), " +
                "titular TEXT NOT NULL, " +
                "vencimiento TEXT NOT NULL, " +
                "guardada INTEGER NOT NULL DEFAULT 1, " +
                "fecha_registro TEXT NOT NULL)",

            "CREATE TABLE compromiso (" +
                "id_compromiso INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "folio TEXT NOT NULL UNIQUE, " +
                "id_usuario_final INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "id_empresa INTEGER NOT NULL REFERENCES empresa_cliente(id_empresa), " +
                "concepto TEXT NOT NULL COLLATE NOCASE, " +
                "numero_cuota INTEGER, " +
                "total_cuotas INTEGER, " +
                "monto INTEGER NOT NULL CHECK (monto > 0), " +
                "fecha_vencimiento TEXT NOT NULL, " +
                "id_estado_compromiso INTEGER NOT NULL REFERENCES estado_compromiso(id_estado_compromiso), " +
                "recargo_aplicado INTEGER NOT NULL DEFAULT 0, " +
                "fecha_cambio_estado TEXT NOT NULL, " +
                "id_admin_registro INTEGER REFERENCES usuario(id_usuario), " +
                "fecha_registro TEXT NOT NULL, " +
                "CHECK (numero_cuota IS NULL OR numero_cuota <= total_cuotas))",
            "CREATE UNIQUE INDEX ux_compromiso_sin_repetir ON compromiso " +
                "(id_usuario_final, id_empresa, concepto, numero_cuota) " +
                "WHERE id_estado_compromiso <> ${idDe(EstadoCompromiso.ANULADO)}",
            "CREATE TABLE anulacion (" +
                "id_compromiso INTEGER PRIMARY KEY REFERENCES compromiso(id_compromiso), " +
                "id_administrador INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "id_motivo_anulacion INTEGER NOT NULL REFERENCES motivo_anulacion(id_motivo_anulacion), " +
                "comentario TEXT, " +
                "fecha TEXT NOT NULL, " +
                "CHECK (id_motivo_anulacion <> ${idDe(MotivoAnulacion.OTRO)} OR comentario IS NOT NULL))",
            "CREATE TABLE pago (" +
                "id_pago INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero_comprobante TEXT NOT NULL UNIQUE, " +
                "id_compromiso INTEGER NOT NULL UNIQUE REFERENCES compromiso(id_compromiso), " +
                "fecha_hora TEXT NOT NULL, " +
                "monto_pagado INTEGER NOT NULL CHECK (monto_pagado > 0), " +
                "id_canal INTEGER NOT NULL REFERENCES canal(id_canal), " +
                "id_medio_pago INTEGER NOT NULL REFERENCES medio_pago(id_medio_pago), " +
                "id_tarjeta INTEGER REFERENCES tarjeta(id_tarjeta))",

            "CREATE TABLE reporte (" +
                "id_reporte INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero TEXT NOT NULL UNIQUE, " +
                "id_compromiso INTEGER NOT NULL REFERENCES compromiso(id_compromiso), " +
                "id_motivo_reporte INTEGER NOT NULL REFERENCES motivo_reporte(id_motivo_reporte), " +
                "comentario TEXT, " +
                "fecha_creacion TEXT NOT NULL, " +
                "id_estado_reporte INTEGER NOT NULL REFERENCES estado_reporte(id_estado_reporte), " +
                "id_administrador INTEGER REFERENCES usuario(id_usuario), " +
                "fecha_toma TEXT, " +
                "respuesta TEXT, " +
                "fecha_resolucion TEXT, " +
                "CHECK (id_estado_reporte <> ${idDe(EstadoReporte.RESUELTO)} OR respuesta IS NOT NULL))",

            "CREATE TABLE recomendacion (" +
                "id_recomendacion INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "numero TEXT NOT NULL UNIQUE, " +
                "id_usuario_final INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "nombre_organizacion TEXT NOT NULL COLLATE NOCASE, " +
                "rut_organizacion TEXT, " +
                "id_rubro INTEGER NOT NULL REFERENCES rubro(id_rubro), " +
                "id_comuna INTEGER NOT NULL REFERENCES comuna(id_comuna), " +
                "fecha TEXT NOT NULL, " +
                "id_estado_recomendacion INTEGER NOT NULL REFERENCES estado_recomendacion(id_estado_recomendacion), " +
                "UNIQUE (id_usuario_final, nombre_organizacion))",
            "CREATE TABLE contacto_recomendacion (" +
                "id_recomendacion INTEGER PRIMARY KEY REFERENCES recomendacion(id_recomendacion), " +
                "nombre TEXT NOT NULL, " +
                "correo TEXT, " +
                "telefono TEXT, " +
                "autoriza_contacto INTEGER NOT NULL CHECK (autoriza_contacto = 1))",
            "CREATE TABLE historial_recomendacion (" +
                "id_historial INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "id_recomendacion INTEGER NOT NULL REFERENCES recomendacion(id_recomendacion), " +
                "id_estado_recomendacion INTEGER NOT NULL REFERENCES estado_recomendacion(id_estado_recomendacion), " +
                "id_administrador INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "fecha TEXT NOT NULL, " +
                "motivo TEXT)",

            "CREATE TABLE usuario_reconocimiento (" +
                "id_usuario_final INTEGER NOT NULL REFERENCES usuario(id_usuario), " +
                "id_reconocimiento INTEGER NOT NULL REFERENCES reconocimiento(id_reconocimiento), " +
                "fecha_obtencion TEXT NOT NULL, " +
                "motivo TEXT NOT NULL, " +
                "PRIMARY KEY (id_usuario_final, id_reconocimiento))",
            SQL_CREAR_CODIGO_RECUPERACION,
            SQL_CREAR_TIPO_MOVIMIENTO,
            SQL_CREAR_MOVIMIENTO,
            SQL_INDICE_MOVIMIENTO,
            SQL_CREAR_BOLETA
        )

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.codigo AS codigo_rol FROM usuario u INNER JOIN rol r ON u.id_rol = r.id_rol"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        SQL_CREAR_TABLAS.forEach { db.execSQL(it) }
        precargar(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            (TABLAS.reversed() + TABLAS_ANTIGUAS).forEach { db.execSQL("DROP TABLE IF EXISTS $it") }
            onCreate(db)
            return
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE usuario ADD COLUMN avatar BLOB")
        }
        if (oldVersion < 4) {
            actualizarClavesDePrueba(db)
        }
        if (oldVersion < 5) {
            db.execSQL(SQL_CREAR_CODIGO_RECUPERACION)
        }
        if (oldVersion < 6) {
            db.execSQL(SQL_CREAR_TIPO_MOVIMIENTO)
            db.execSQL(SQL_CREAR_MOVIMIENTO)
            db.execSQL(SQL_INDICE_MOVIMIENTO)
            insertarTiposMovimiento(db)
            val actuales = cargarDatos(db)
            movimientosIniciales(actuales.compromisos, actuales.pagos, actuales.anulaciones, actuales.reportes)
                .forEach { insertarMovimiento(db, it) }
        }
        if (oldVersion < 7) {
            db.execSQL(SQL_CREAR_BOLETA)
        }
    }

    private fun insertarTiposMovimiento(db: SQLiteDatabase) {
        insertarCatalogo(db, "tipo_movimiento", "id_tipo_movimiento", TipoMovimiento.entries.map { idDe(it) to it.etiqueta })
    }

    private fun insertarMovimiento(db: SQLiteDatabase, movimiento: Movimiento) {
        db.insertOrThrow("movimiento", null, valoresDe(movimiento))
    }

    private fun actualizarClavesDePrueba(db: SQLiteDatabase) {
        val conClaveAnterior = mutableListOf<Pair<Int, String>>()
        db.rawQuery("SELECT id_usuario, rut, clave_hash FROM usuario", null).use { cursor ->
            while (cursor.moveToNext()) {
                val rut = cursor.texto("rut")
                if (cursor.texto("clave_hash") == hashClave(rut, CLAVE_ANTERIOR_DE_PRUEBA)) {
                    conClaveAnterior.add(cursor.entero("id_usuario") to rut)
                }
            }
        }
        conClaveAnterior.forEach { (idUsuario, rut) ->
            val valores = ContentValues().apply { put("clave_hash", hashClave(rut, CLAVE_DE_PRUEBA)) }
            db.update("usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString()))
        }
    }

    private fun precargar(db: SQLiteDatabase) {
        Rol.entries.forEach {
            db.insertOrThrow("rol", null, ContentValues().apply {
                put("id_rol", idDe(it)); put("codigo", it.name); put("nombre", it.etiqueta); put("descripcion", it.descripcion)
            })
        }
        insertarCatalogo(db, "estado_compromiso", "id_estado_compromiso", EstadoCompromiso.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "motivo_anulacion", "id_motivo_anulacion", MotivoAnulacion.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "canal", "id_canal", Canal.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "medio_pago", "id_medio_pago", MedioPago.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "marca_tarjeta", "id_marca_tarjeta", MarcaTarjeta.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "motivo_reporte", "id_motivo_reporte", MotivoReporte.entries.map { idDe(it) to it.etiqueta })
        insertarCatalogo(db, "estado_reporte", "id_estado_reporte", EstadoReporte.entries.map { idDe(it) to it.etiqueta })
        insertarTiposMovimiento(db)
        EstadoRecomendacion.entries.forEach {
            db.insertOrThrow("estado_recomendacion", null, ContentValues().apply {
                put("id_estado_recomendacion", idDe(it)); put("nombre", it.etiqueta); put("orden", it.orden)
            })
        }
        insertarCatalogo(db, "comuna", "id_comuna", DatosAnexo9.comunas.map { it.idComuna to it.nombre })
        insertarCatalogo(db, "rubro", "id_rubro", DatosAnexo9.rubros.map { it.idRubro to it.nombre })
        DatosAnexo9.reconocimientos.forEach {
            db.insertOrThrow("reconocimiento", null, ContentValues().apply {
                put("id_reconocimiento", it.idReconocimiento); put("nombre", it.nombre); put("descripcion", it.descripcion)
            })
        }

        DatosAnexo9.empresas.forEach { db.insertOrThrow("empresa_cliente", null, valoresDe(it)) }
        DatosAnexo9.usuarios.forEach {
            db.insertOrThrow("usuario", null, valoresDe(it).apply { put("clave_hash", hashClave(it.rut, CLAVE_DE_PRUEBA)) })
        }
        DatosAnexo9.tarjetas.forEach { db.insertOrThrow("tarjeta", null, valoresDe(it)) }
        val compromisos = actualizarVencimientos(DatosAnexo9.compromisos, DatosAnexo9.empresas, DatosAnexo9.FECHA_HOY)
        compromisos.forEach { db.insertOrThrow("compromiso", null, valoresDe(it)) }
        DatosAnexo9.anulaciones.forEach { db.insertOrThrow("anulacion", null, valoresDe(it)) }
        DatosAnexo9.pagos.forEach { db.insertOrThrow("pago", null, valoresDe(it)) }
        DatosAnexo9.reportes.forEach { db.insertOrThrow("reporte", null, valoresDe(it)) }
        DatosAnexo9.recomendaciones.forEach { db.insertOrThrow("recomendacion", null, valoresDe(it)) }
        DatosAnexo9.contactosRecomendacion.forEach { db.insertOrThrow("contacto_recomendacion", null, valoresDe(it)) }
        DatosAnexo9.historialRecomendaciones.forEach { db.insertOrThrow("historial_recomendacion", null, valoresDe(it)) }
        DatosAnexo9.reconocimientosObtenidos.forEach { db.insertOrThrow("usuario_reconocimiento", null, valoresDe(it)) }
        movimientosIniciales(compromisos, DatosAnexo9.pagos, DatosAnexo9.anulaciones, DatosAnexo9.reportes)
            .forEach { insertarMovimiento(db, it) }
    }

    private fun insertarCatalogo(db: SQLiteDatabase, tabla: String, columnaId: String, filas: List<Pair<Int, String>>) {
        filas.forEach { (id, nombre) ->
            db.insertOrThrow(tabla, null, ContentValues().apply { put(columnaId, id); put("nombre", nombre) })
        }
    }

    fun restablecerDatos() {
        val db = writableDatabase
        db.beginTransaction()
        try {
            TABLAS.reversed().forEach { db.delete(it, null, null) }
            precargar(db)
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun cargarDatos(): BaseDatos = cargarDatos(readableDatabase)

    private fun cargarDatos(db: SQLiteDatabase): BaseDatos = BaseDatos(
        usuarios = consultar("$SQL_SELECT_USUARIOS ORDER BY u.id_usuario", db = db) { leerUsuario(it) },
        empresas = consultar("SELECT * FROM empresa_cliente ORDER BY id_empresa", db = db) { leerEmpresa(it) },
        compromisos = consultar("SELECT * FROM compromiso ORDER BY id_compromiso", db = db) { leerCompromiso(it) },
        pagos = consultar("SELECT * FROM pago ORDER BY id_pago", db = db) { leerPago(it) },
        anulaciones = consultar("SELECT * FROM anulacion", db = db) { leerAnulacion(it) },
        reportes = consultar("SELECT * FROM reporte ORDER BY id_reporte", db = db) { leerReporte(it) },
        recomendaciones = consultar("SELECT * FROM recomendacion ORDER BY id_recomendacion", db = db) { leerRecomendacion(it) },
        contactosRecomendacion = consultar("SELECT * FROM contacto_recomendacion", db = db) { leerContacto(it) },
        historialRecomendaciones = consultar("SELECT * FROM historial_recomendacion ORDER BY id_historial", db = db) { leerHistorial(it) },
        reconocimientosObtenidos = consultar("SELECT * FROM usuario_reconocimiento", db = db) { leerUsuarioReconocimiento(it) },
        tarjetas = consultar("SELECT * FROM tarjeta ORDER BY id_tarjeta", db = db) { leerTarjeta(it) },
        rubros = consultar("SELECT * FROM rubro ORDER BY id_rubro", db = db) { Rubro(it.entero("id_rubro"), it.texto("nombre")) },
        comunas = consultar("SELECT * FROM comuna ORDER BY nombre", db = db) { Comuna(it.entero("id_comuna"), it.texto("nombre")) },
        reconocimientos = consultar("SELECT * FROM reconocimiento ORDER BY id_reconocimiento", db = db) {
            Reconocimiento(it.entero("id_reconocimiento"), it.texto("nombre"), it.texto("descripcion"))
        },
        movimientos = consultar("SELECT * FROM movimiento ORDER BY id_movimiento", db = db) { leerMovimiento(it) },
        fechaHoy = DatosAnexo9.FECHA_HOY
    )

    fun buscarPorCredenciales(correo: String, clave: String): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.correo = ?", arrayOf(correo.trim())) { cursor ->
            val usuario = leerUsuario(cursor)
            if (cursor.texto("clave_hash") == hashClave(usuario.rut, clave)) usuario else null
        }.firstOrNull()

    fun buscarUsuarioPorId(id: Int): Usuario? =
        consultar("$SQL_SELECT_USUARIOS WHERE u.id_usuario = ?", arrayOf(id.toString())) { leerUsuario(it) }.firstOrNull()

    fun buscarCompromisos(texto: String): List<Compromiso> {
        val patron = "%${texto.trim()}%"
        val patronRut = "%${texto.filter { it.isLetterOrDigit() }}%"
        return consultar(
            "SELECT c.* FROM compromiso c " +
                "INNER JOIN usuario u ON c.id_usuario_final = u.id_usuario " +
                "INNER JOIN empresa_cliente e ON c.id_empresa = e.id_empresa " +
                "WHERE (u.nombres || ' ' || u.apellidos) LIKE ? OR " +
                "REPLACE(REPLACE(u.rut, '.', ''), '-', '') LIKE ? OR " +
                "c.concepto LIKE ? OR e.nombre_fantasia LIKE ? " +
                "ORDER BY c.fecha_vencimiento, c.id_compromiso",
            arrayOf(patron, patronRut, patron, patron)
        ) { leerCompromiso(it) }
    }

    fun registrarPago(
        pago: Pago,
        compromiso: Compromiso,
        reconocimientos: List<UsuarioReconocimiento>,
        tarjetaNueva: Tarjeta? = null,
        siguienteCuota: Compromiso? = null,
        movimientos: List<Movimiento> = emptyList()
    ): Boolean =
        enTransaccion { db ->
            if (tarjetaNueva != null) db.insertOrThrow("tarjeta", null, valoresDe(tarjetaNueva))
            db.insertOrThrow("pago", null, valoresDe(pago))
            actualizarCompromiso(db, compromiso)
            if (siguienteCuota != null) db.insertOrThrow("compromiso", null, valoresDe(siguienteCuota))
            reconocimientos.forEach { db.insertOrThrow("usuario_reconocimiento", null, valoresDe(it)) }
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun registrarReporte(reporte: Reporte, compromiso: Compromiso, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            db.insertOrThrow("reporte", null, valoresDe(reporte))
            actualizarCompromiso(db, compromiso)
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun actualizarReporte(reporte: Reporte, compromiso: Compromiso?, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            db.update("reporte", valoresDe(reporte), "id_reporte = ?", arrayOf(reporte.idReporte.toString()))
            if (compromiso != null) actualizarCompromiso(db, compromiso)
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun registrarRecomendacion(recomendacion: Recomendacion, contacto: ContactoRecomendacion?): Boolean =
        enTransaccion { db ->
            db.insertOrThrow("recomendacion", null, valoresDe(recomendacion))
            if (contacto != null) db.insertOrThrow("contacto_recomendacion", null, valoresDe(contacto))
        }

    fun cambiarEstadoRecomendacion(
        recomendacion: Recomendacion,
        historial: HistorialRecomendacion,
        reconocimiento: UsuarioReconocimiento?
    ): Boolean =
        enTransaccion { db ->
            db.update(
                "recomendacion", valoresDe(recomendacion),
                "id_recomendacion = ?", arrayOf(recomendacion.idRecomendacion.toString())
            )
            db.insertOrThrow("historial_recomendacion", null, valoresDe(historial))
            if (reconocimiento != null) db.insertOrThrow("usuario_reconocimiento", null, valoresDe(reconocimiento))
        }

    fun anularCompromiso(anulacion: Anulacion, compromiso: Compromiso, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            db.insertOrThrow("anulacion", null, valoresDe(anulacion))
            actualizarCompromiso(db, compromiso)
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun registrarCompromiso(
        compromiso: Compromiso,
        personaNueva: Usuario?,
        movimientos: List<Movimiento> = emptyList(),
        fotoBoleta: ByteArray? = null
    ): Boolean =
        enTransaccion { db ->
            if (personaNueva != null) {
                db.insertOrThrow("usuario", null, valoresDe(personaNueva).apply {
                    put("clave_hash", hashClave(personaNueva.rut, CLAVE_DE_PRUEBA))
                })
            }
            db.insertOrThrow("compromiso", null, valoresDe(compromiso))
            if (fotoBoleta != null) {
                db.insertOrThrow("boleta", null, ContentValues().apply {
                    put("id_compromiso", compromiso.idCompromiso)
                    put("imagen", fotoBoleta)
                    put("fecha_registro", compromiso.fechaRegistro.toString())
                })
            }
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun boletaDe(idCompromiso: Int): ByteArray? =
        consultar("SELECT imagen FROM boleta WHERE id_compromiso = ?", arrayOf(idCompromiso.toString())) { cursor ->
            cursor.getBlob(cursor.getColumnIndexOrThrow("imagen"))
        }.firstOrNull()

    fun registrarUsuario(usuario: Usuario, clave: String, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            db.insertOrThrow("usuario", null, valoresDe(usuario).apply { put("clave_hash", hashClave(usuario.rut, clave)) })
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun actualizarUsuario(usuario: Usuario, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            db.update("usuario", valoresDe(usuario), "id_usuario = ?", arrayOf(usuario.idUsuario.toString()))
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun actualizarAvatar(idUsuario: Int, avatar: ByteArray): Boolean =
        enTransaccion { db ->
            val valores = ContentValues().apply { put("avatar", avatar) }
            db.update("usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString()))
        }

    fun cambiarClave(usuario: Usuario, claveNueva: String, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            val valores = ContentValues().apply { put("clave_hash", hashClave(usuario.rut, claveNueva)) }
            db.update("usuario", valores, "id_usuario = ?", arrayOf(usuario.idUsuario.toString()))
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun guardarCodigoRecuperacion(codigo: CodigoRecuperacion): Boolean =
        enTransaccion { db ->
            val anteriores = ContentValues().apply { put("usado", 1) }
            db.update("codigo_recuperacion", anteriores, "id_usuario = ? AND usado = 0", arrayOf(codigo.idUsuario.toString()))
            db.insertOrThrow("codigo_recuperacion", null, valoresDe(codigo))
        }

    fun codigoVigenteDe(idUsuario: Int): CodigoRecuperacion? =
        consultar(
            "SELECT * FROM codigo_recuperacion WHERE id_usuario = ? AND usado = 0 ORDER BY id_codigo DESC LIMIT 1",
            arrayOf(idUsuario.toString())
        ) { leerCodigo(it) }.firstOrNull()

    fun sumarIntentoCodigo(codigo: CodigoRecuperacion): Boolean =
        enTransaccion { db ->
            val valores = ContentValues().apply { put("intentos", codigo.intentos + 1) }
            db.update("codigo_recuperacion", valores, "id_codigo = ?", arrayOf(codigo.idCodigo.toString()))
        }

    fun cambiarClaveConCodigo(
        usuario: Usuario,
        codigo: CodigoRecuperacion,
        claveNueva: String,
        movimientos: List<Movimiento> = emptyList()
    ): Boolean =
        enTransaccion { db ->
            val usado = ContentValues().apply { put("usado", 1) }
            db.update("codigo_recuperacion", usado, "id_codigo = ?", arrayOf(codigo.idCodigo.toString()))
            val valores = ContentValues().apply { put("clave_hash", hashClave(usuario.rut, claveNueva)) }
            db.update("usuario", valores, "id_usuario = ?", arrayOf(usuario.idUsuario.toString()))
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    fun registrarTarjeta(tarjeta: Tarjeta): Boolean =
        enTransaccion { db -> db.insertOrThrow("tarjeta", null, valoresDe(tarjeta)) }

    fun quitarTarjeta(idTarjeta: Int): Boolean =
        enTransaccion { db ->
            val valores = ContentValues().apply { put("guardada", 0) }
            db.update("tarjeta", valores, "id_tarjeta = ?", arrayOf(idTarjeta.toString()))
        }

    fun actualizarCompromisos(compromisos: List<Compromiso>, movimientos: List<Movimiento> = emptyList()): Boolean =
        enTransaccion { db ->
            compromisos.forEach { actualizarCompromiso(db, it) }
            movimientos.forEach { insertarMovimiento(db, it) }
        }

    private fun actualizarCompromiso(db: SQLiteDatabase, compromiso: Compromiso) {
        db.update("compromiso", valoresDe(compromiso), "id_compromiso = ?", arrayOf(compromiso.idCompromiso.toString()))
    }

    private fun enTransaccion(cambios: (SQLiteDatabase) -> Unit): Boolean {
        val db = writableDatabase
        db.beginTransaction()
        return try {
            cambios(db)
            db.setTransactionSuccessful()
            true
        } catch (e: SQLException) {
            false
        } finally {
            db.endTransaction()
        }
    }

    private fun <T> consultar(
        sql: String,
        args: Array<String>? = null,
        db: SQLiteDatabase = readableDatabase,
        leerFila: (Cursor) -> T
    ): List<T> {
        val lista = mutableListOf<T>()
        db.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerFila(cursor))
            }
        }
        return lista
    }

    private fun Cursor.texto(columna: String): String = getString(getColumnIndexOrThrow(columna))
    private fun Cursor.textoONulo(columna: String): String? =
        getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getString(it) }
    private fun Cursor.entero(columna: String): Int = getInt(getColumnIndexOrThrow(columna))
    private fun Cursor.enteroONulo(columna: String): Int? =
        getColumnIndexOrThrow(columna).let { if (isNull(it)) null else getInt(it) }
    private fun Cursor.fecha(columna: String): LocalDate = LocalDate.parse(texto(columna))
    private fun Cursor.fechaHora(columna: String): LocalDateTime = LocalDateTime.parse(texto(columna))
    private fun Cursor.fechaHoraONula(columna: String): LocalDateTime? = textoONulo(columna)?.let { LocalDateTime.parse(it) }

    private fun leerUsuario(c: Cursor) = Usuario(
        idUsuario = c.entero("id_usuario"),
        rol = Rol.valueOf(c.texto("codigo_rol")),
        rut = c.texto("rut"),
        nombres = c.texto("nombres"),
        apellidos = c.texto("apellidos"),
        correo = c.texto("correo"),
        telefono = c.textoONulo("telefono"),
        activo = c.entero("activo") == 1,
        fechaCreacion = c.fechaHora("fecha_creacion"),
        idComuna = c.enteroONulo("id_comuna"),
        fechaIncorporacion = c.textoONulo("fecha_incorporacion")?.let { LocalDate.parse(it) },
        avisosActivos = c.enteroONulo("avisos_activos")?.let { it == 1 },
        diasAnticipacion = c.enteroONulo("dias_anticipacion"),
        avatar = c.getColumnIndexOrThrow("avatar").let { columna -> if (c.isNull(columna)) null else c.getBlob(columna) }
    )

    private fun leerTarjeta(c: Cursor) = Tarjeta(
        idTarjeta = c.entero("id_tarjeta"),
        idUsuario = c.entero("id_usuario"),
        medioPago = MedioPago.entries[c.entero("id_medio_pago") - 1],
        marca = MarcaTarjeta.entries[c.entero("id_marca_tarjeta") - 1],
        ultimos4 = c.texto("ultimos4"),
        titular = c.texto("titular"),
        vencimiento = YearMonth.parse(c.texto("vencimiento")),
        guardada = c.entero("guardada") == 1,
        fechaRegistro = c.fechaHora("fecha_registro")
    )

    private fun leerEmpresa(c: Cursor) = EmpresaCliente(
        idEmpresa = c.entero("id_empresa"),
        codigo = c.texto("codigo"),
        razonSocial = c.texto("razon_social"),
        nombreFantasia = c.texto("nombre_fantasia"),
        rut = c.texto("rut"),
        idRubro = c.entero("id_rubro"),
        correoAtencion = c.texto("correo_atencion"),
        telefonoAtencion = c.texto("telefono_atencion"),
        recargoAtraso = c.entero("recargo_atraso"),
        activa = c.entero("activa") == 1
    )

    private fun leerCompromiso(c: Cursor) = Compromiso(
        idCompromiso = c.entero("id_compromiso"),
        folio = c.texto("folio"),
        idUsuarioFinal = c.entero("id_usuario_final"),
        idEmpresa = c.entero("id_empresa"),
        concepto = c.texto("concepto"),
        numeroCuota = c.enteroONulo("numero_cuota"),
        totalCuotas = c.enteroONulo("total_cuotas"),
        monto = c.entero("monto"),
        fechaVencimiento = c.fecha("fecha_vencimiento"),
        estado = EstadoCompromiso.entries[c.entero("id_estado_compromiso") - 1],
        recargoAplicado = c.entero("recargo_aplicado"),
        fechaCambioEstado = c.fechaHora("fecha_cambio_estado"),
        idAdminRegistro = c.enteroONulo("id_admin_registro"),
        fechaRegistro = c.fechaHora("fecha_registro")
    )

    private fun leerPago(c: Cursor) = Pago(
        idPago = c.entero("id_pago"),
        numeroComprobante = c.texto("numero_comprobante"),
        idCompromiso = c.entero("id_compromiso"),
        fechaHora = c.fechaHora("fecha_hora"),
        montoPagado = c.entero("monto_pagado"),
        canal = Canal.entries[c.entero("id_canal") - 1],
        medioPago = MedioPago.entries[c.entero("id_medio_pago") - 1],
        idTarjeta = c.enteroONulo("id_tarjeta")
    )

    private fun leerAnulacion(c: Cursor) = Anulacion(
        idCompromiso = c.entero("id_compromiso"),
        idAdministrador = c.entero("id_administrador"),
        motivo = MotivoAnulacion.entries[c.entero("id_motivo_anulacion") - 1],
        comentario = c.textoONulo("comentario"),
        fecha = c.fechaHora("fecha")
    )

    private fun leerReporte(c: Cursor) = Reporte(
        idReporte = c.entero("id_reporte"),
        numero = c.texto("numero"),
        idCompromiso = c.entero("id_compromiso"),
        motivo = MotivoReporte.entries[c.entero("id_motivo_reporte") - 1],
        comentario = c.textoONulo("comentario"),
        fechaCreacion = c.fechaHora("fecha_creacion"),
        estado = EstadoReporte.entries[c.entero("id_estado_reporte") - 1],
        idAdministrador = c.enteroONulo("id_administrador"),
        fechaToma = c.fechaHoraONula("fecha_toma"),
        respuesta = c.textoONulo("respuesta"),
        fechaResolucion = c.fechaHoraONula("fecha_resolucion")
    )

    private fun leerRecomendacion(c: Cursor) = Recomendacion(
        idRecomendacion = c.entero("id_recomendacion"),
        numero = c.texto("numero"),
        idUsuarioFinal = c.entero("id_usuario_final"),
        nombreOrganizacion = c.texto("nombre_organizacion"),
        rutOrganizacion = c.textoONulo("rut_organizacion"),
        idRubro = c.entero("id_rubro"),
        idComuna = c.entero("id_comuna"),
        fecha = c.fechaHora("fecha"),
        estado = EstadoRecomendacion.entries[c.entero("id_estado_recomendacion") - 1]
    )

    private fun leerContacto(c: Cursor) = ContactoRecomendacion(
        idRecomendacion = c.entero("id_recomendacion"),
        nombre = c.texto("nombre"),
        correo = c.textoONulo("correo"),
        telefono = c.textoONulo("telefono"),
        autorizaContacto = c.entero("autoriza_contacto") == 1
    )

    private fun leerHistorial(c: Cursor) = HistorialRecomendacion(
        idHistorial = c.entero("id_historial"),
        idRecomendacion = c.entero("id_recomendacion"),
        estado = EstadoRecomendacion.entries[c.entero("id_estado_recomendacion") - 1],
        idAdministrador = c.entero("id_administrador"),
        fecha = c.fechaHora("fecha"),
        motivo = c.textoONulo("motivo")
    )

    private fun leerCodigo(c: Cursor) = CodigoRecuperacion(
        idCodigo = c.entero("id_codigo"),
        idUsuario = c.entero("id_usuario"),
        codigoHash = c.texto("codigo_hash"),
        fechaCreacion = c.fechaHora("fecha_creacion"),
        fechaExpiracion = c.fechaHora("fecha_expiracion"),
        intentos = c.entero("intentos"),
        usado = c.entero("usado") == 1
    )

    private fun leerMovimiento(c: Cursor) = Movimiento(
        idMovimiento = c.entero("id_movimiento"),
        tipo = TipoMovimiento.entries[c.entero("id_tipo_movimiento") - 1],
        fecha = c.fechaHora("fecha"),
        idUsuario = c.entero("id_usuario"),
        idAutor = c.enteroONulo("id_autor"),
        idCompromiso = c.enteroONulo("id_compromiso"),
        detalle = c.texto("detalle")
    )

    private fun leerUsuarioReconocimiento(c: Cursor) = UsuarioReconocimiento(
        idUsuarioFinal = c.entero("id_usuario_final"),
        idReconocimiento = c.entero("id_reconocimiento"),
        fechaObtencion = c.fechaHora("fecha_obtencion"),
        motivo = c.texto("motivo")
    )

    private fun valoresDe(e: EmpresaCliente) = ContentValues().apply {
        put("id_empresa", e.idEmpresa); put("codigo", e.codigo); put("razon_social", e.razonSocial)
        put("nombre_fantasia", e.nombreFantasia); put("rut", e.rut); put("id_rubro", e.idRubro)
        put("correo_atencion", e.correoAtencion); put("telefono_atencion", e.telefonoAtencion)
        put("recargo_atraso", e.recargoAtraso); put("activa", if (e.activa) 1 else 0)
    }

    private fun valoresDe(u: Usuario) = ContentValues().apply {
        put("id_usuario", u.idUsuario); put("id_rol", idDe(u.rol)); put("rut", u.rut)
        put("nombres", u.nombres); put("apellidos", u.apellidos); put("correo", u.correo)
        put("telefono", u.telefono); put("activo", if (u.activo) 1 else 0)
        put("fecha_creacion", u.fechaCreacion.toString())
        put("id_comuna", u.idComuna); put("fecha_incorporacion", u.fechaIncorporacion?.toString())
        put("avisos_activos", u.avisosActivos?.let { if (it) 1 else 0 }); put("dias_anticipacion", u.diasAnticipacion)
    }

    private fun valoresDe(t: Tarjeta) = ContentValues().apply {
        put("id_tarjeta", t.idTarjeta); put("id_usuario", t.idUsuario); put("id_medio_pago", idDe(t.medioPago))
        put("id_marca_tarjeta", idDe(t.marca)); put("ultimos4", t.ultimos4); put("titular", t.titular)
        put("vencimiento", t.vencimiento.toString()); put("guardada", if (t.guardada) 1 else 0)
        put("fecha_registro", t.fechaRegistro.toString())
    }

    private fun valoresDe(c: Compromiso) = ContentValues().apply {
        put("id_compromiso", c.idCompromiso); put("folio", c.folio); put("id_usuario_final", c.idUsuarioFinal)
        put("id_empresa", c.idEmpresa); put("concepto", c.concepto)
        put("numero_cuota", c.numeroCuota); put("total_cuotas", c.totalCuotas); put("monto", c.monto)
        put("fecha_vencimiento", c.fechaVencimiento.toString()); put("id_estado_compromiso", idDe(c.estado))
        put("recargo_aplicado", c.recargoAplicado); put("fecha_cambio_estado", c.fechaCambioEstado.toString())
        put("id_admin_registro", c.idAdminRegistro); put("fecha_registro", c.fechaRegistro.toString())
    }

    private fun valoresDe(a: Anulacion) = ContentValues().apply {
        put("id_compromiso", a.idCompromiso); put("id_administrador", a.idAdministrador)
        put("id_motivo_anulacion", idDe(a.motivo)); put("comentario", a.comentario); put("fecha", a.fecha.toString())
    }

    private fun valoresDe(p: Pago) = ContentValues().apply {
        put("id_pago", p.idPago); put("numero_comprobante", p.numeroComprobante); put("id_compromiso", p.idCompromiso)
        put("fecha_hora", p.fechaHora.toString()); put("monto_pagado", p.montoPagado)
        put("id_canal", idDe(p.canal)); put("id_medio_pago", idDe(p.medioPago)); put("id_tarjeta", p.idTarjeta)
    }

    private fun valoresDe(r: Reporte) = ContentValues().apply {
        put("id_reporte", r.idReporte); put("numero", r.numero); put("id_compromiso", r.idCompromiso)
        put("id_motivo_reporte", idDe(r.motivo)); put("comentario", r.comentario)
        put("fecha_creacion", r.fechaCreacion.toString()); put("id_estado_reporte", idDe(r.estado))
        put("id_administrador", r.idAdministrador); put("fecha_toma", r.fechaToma?.toString())
        put("respuesta", r.respuesta); put("fecha_resolucion", r.fechaResolucion?.toString())
    }

    private fun valoresDe(r: Recomendacion) = ContentValues().apply {
        put("id_recomendacion", r.idRecomendacion); put("numero", r.numero); put("id_usuario_final", r.idUsuarioFinal)
        put("nombre_organizacion", r.nombreOrganizacion); put("rut_organizacion", r.rutOrganizacion)
        put("id_rubro", r.idRubro); put("id_comuna", r.idComuna); put("fecha", r.fecha.toString())
        put("id_estado_recomendacion", idDe(r.estado))
    }

    private fun valoresDe(c: ContactoRecomendacion) = ContentValues().apply {
        put("id_recomendacion", c.idRecomendacion); put("nombre", c.nombre); put("correo", c.correo)
        put("telefono", c.telefono); put("autoriza_contacto", if (c.autorizaContacto) 1 else 0)
    }

    private fun valoresDe(h: HistorialRecomendacion) = ContentValues().apply {
        put("id_historial", h.idHistorial); put("id_recomendacion", h.idRecomendacion)
        put("id_estado_recomendacion", idDe(h.estado)); put("id_administrador", h.idAdministrador)
        put("fecha", h.fecha.toString()); put("motivo", h.motivo)
    }

    private fun valoresDe(r: UsuarioReconocimiento) = ContentValues().apply {
        put("id_usuario_final", r.idUsuarioFinal); put("id_reconocimiento", r.idReconocimiento)
        put("fecha_obtencion", r.fechaObtencion.toString()); put("motivo", r.motivo)
    }

    private fun valoresDe(c: CodigoRecuperacion) = ContentValues().apply {
        put("id_usuario", c.idUsuario); put("codigo_hash", c.codigoHash)
        put("fecha_creacion", c.fechaCreacion.toString()); put("fecha_expiracion", c.fechaExpiracion.toString())
        put("intentos", c.intentos); put("usado", if (c.usado) 1 else 0)
    }

    private fun valoresDe(m: Movimiento) = ContentValues().apply {
        put("id_tipo_movimiento", idDe(m.tipo)); put("fecha", m.fecha.toString()); put("id_usuario", m.idUsuario)
        put("id_autor", m.idAutor); put("id_compromiso", m.idCompromiso); put("detalle", m.detalle)
    }
}
