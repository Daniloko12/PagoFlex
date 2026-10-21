package com.example.pagoflex.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class ColoresExtra(
    val pendiente: Color,
    val vencido: Color,
    val pagado: Color,
    val enRevision: Color,
    val anulado: Color,
    val exito: Color,
    val exitoFondo: Color,
    val informacion: Color,
    val informacionFondo: Color,
    val advertencia: Color,
    val advertenciaFondo: Color,
    val deshabilitado: Color,
    val avatares: List<Color>,
    val encabezado: List<Color>,
    val encabezadoMenu: List<Color>,
    val textoEncabezado: Color,
    val textoEncabezadoSuave: Color
)

private val EsquemaOscuro = darkColorScheme(
    primary = Primario,
    onPrimary = Negro,
    primaryContainer = PrimarioOscuro,
    onPrimaryContainer = PrimarioClaro,
    secondary = Secundario,
    onSecondary = Negro,
    secondaryContainer = PrimarioOscuro,
    onSecondaryContainer = PrimarioClaro,
    tertiary = Blanco,
    onTertiary = Negro,
    background = Negro,
    onBackground = Blanco,
    surface = NegroTarjeta,
    onSurface = Blanco,
    surfaceVariant = NegroElevado,
    onSurfaceVariant = GrisTexto,
    outline = GrisContorno,
    outlineVariant = NegroBorde,
    surfaceContainerLowest = Negro,
    surfaceContainerLow = NegroTarjeta,
    surfaceContainer = NegroTarjeta,
    surfaceContainerHigh = NegroElevado,
    surfaceContainerHighest = NegroMaximo,
    inverseSurface = Blanco,
    inverseOnSurface = Negro,
    inversePrimary = PrimarioOscuro,
    scrim = Negro
)

private val ExtraOscuro = ColoresExtra(
    pendiente = EstadoPendiente,
    vencido = EstadoVencido,
    pagado = EstadoPagado,
    enRevision = EstadoEnRevision,
    anulado = EstadoAnulado,
    exito = Exito,
    exitoFondo = ExitoFondo,
    informacion = Informacion,
    informacionFondo = InformacionFondo,
    advertencia = Advertencia,
    advertenciaFondo = AdvertenciaFondo,
    deshabilitado = GrisDeshabilitado,
    avatares = listOf(Informacion, Primario, EstadoPendiente, VerdeLima),
    encabezado = listOf(Negro, PrimarioOscuro),
    encabezadoMenu = listOf(PrimarioOscuro, NegroTarjeta),
    textoEncabezado = Blanco,
    textoEncabezadoSuave = PrimarioClaro
)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeIntenso,
    onPrimary = Blanco,
    primaryContainer = VerdeNieve,
    onPrimaryContainer = VerdeNoche,
    secondary = VerdeHoja,
    onSecondary = Blanco,
    secondaryContainer = VerdeNieve,
    onSecondaryContainer = VerdeNoche,
    tertiary = Negro,
    onTertiary = Blanco,
    background = Blanco,
    onBackground = TextoPrincipalClaro,
    surface = BlancoTarjeta,
    onSurface = TextoPrincipalClaro,
    surfaceVariant = BlancoElevado,
    onSurfaceVariant = GrisTextoOscuro,
    outline = GrisContornoClaro,
    outlineVariant = BlancoBorde,
    surfaceContainerLowest = Blanco,
    surfaceContainerLow = Blanco,
    surfaceContainer = Blanco,
    surfaceContainerHigh = Blanco,
    surfaceContainerHighest = BlancoMaximo,
    inverseSurface = NegroSnackbar,
    inverseOnSurface = Blanco,
    inversePrimary = Primario,
    scrim = Negro
)

private val ExtraClaro = ColoresExtra(
    pendiente = EstadoPendienteClaro,
    vencido = EstadoVencidoClaro,
    pagado = EstadoPagadoClaro,
    enRevision = EstadoEnRevisionClaro,
    anulado = EstadoAnuladoClaro,
    exito = ExitoClaro,
    exitoFondo = ExitoFondoClaro,
    informacion = InformacionClaro,
    informacionFondo = InformacionFondoClaro,
    advertencia = AdvertenciaClaro,
    advertenciaFondo = AdvertenciaFondoClaro,
    deshabilitado = GrisDeshabilitadoClaro,
    avatares = listOf(EstadoEnRevisionClaro, VerdeIntenso, EstadoPendienteClaro, VerdeOliva),
    encabezado = listOf(VerdeNieve, VerdeBrote),
    encabezadoMenu = listOf(VerdeNieve, VerdeBrote),
    textoEncabezado = VerdeNoche,
    textoEncabezadoSuave = VerdeProfundo
)

private val LocalColoresExtra = staticCompositionLocalOf { ExtraOscuro }

val MaterialTheme.coloresExtra: ColoresExtra
    @Composable
    @ReadOnlyComposable
    get() = LocalColoresExtra.current

private val Formas = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

@Composable
fun PagoFlexTheme(
    modoOscuro: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalColoresExtra provides if (modoOscuro) ExtraOscuro else ExtraClaro) {
        MaterialTheme(
            colorScheme = if (modoOscuro) EsquemaOscuro else EsquemaClaro,
            typography = Tipografia,
            shapes = Formas,
            content = content
        )
    }
}
