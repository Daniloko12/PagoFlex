# PagoFlex

App Android para revisar y pagar compromisos de pago (cuotas, arriendos, mensualidades) con las organizaciones que cobran a través de PagoFlex. Proyecto del ramo **DSY1105 Desarrollo de Aplicaciones Móviles** de Duoc UC.

La app tiene dos roles que entran por el mismo inicio de sesión: el **usuario final** ve, paga y reporta sus compromisos, y el **administrador** registra, modifica y gestiona los compromisos de todos los clientes.

## Integrantes

| Integrante | GitHub | Aporte principal |
|---|---|---|
| Jose Veliz | [velizntd](https://github.com/velizntd) | Cámara y galería, contraseña segura, recuperación con código, sincronización con Flow, permisos por rol, historial de movimientos, inicio con filtros, animaciones y pruebas |
| (nombre del compañero) | [Daniloko12](https://github.com/Daniloko12) | Base del proyecto, base de datos SQLite, navegación por rol, panel del administrador y notificaciones de recordatorio |

## Funcionalidades

### Usuario final
- Inicio resumido: total por pagar del mes, próximo vencimiento y la lista de compromisos con **filtros por estado, organización y vencimiento**.
- Detalle de cada compromiso con su línea de tiempo (quién lo registró, si se modificó, cuándo se pagó).
- Pago con tarjeta guardada o nueva (solo se guardan la marca y los 4 últimos dígitos) y comprobante para compartir.
- Avance de cuotas por empresa ("cuota 11 de 12" hasta quedar al día), indicador de cumplimiento y reconocimientos.
- **Historial con dos pestañas**: pagos (filtrables por empresa, año y mes) y movimientos de la cuenta.
- Reporte de un cobro que no reconoce.
- Avisos de vencimiento con **notificaciones** diarias y botón para probar un aviso.
- Mi perfil: foto con **cámara o galería**, editar datos y cambiar contraseña.
- Modo oscuro y claro, y modo sin conexión de demostración.

### Administrador
- Panel con gráficos de cobranza, deuda por empresa y vencimientos próximos.
- Registrar, buscar, modificar y anular compromisos.
- Crear y editar clientes, restablecer su contraseña.
- Atender reportes y avanzar recomendaciones.
- **Historial de movimientos** de toda la app (auditoría), con búsqueda y filtros.

## Roles y permisos

Los permisos están centralizados en `model/PermisosRol.kt`. El ViewModel revisa el permiso antes de cada acción y la navegación no abre una pantalla si el rol no la tiene, así que aunque alguien llegue a la pantalla no puede guardar nada.

| Acción | Usuario final | Administrador |
|---|:---:|:---:|
| Ver sus compromisos, avance e historial | ✅ | — |
| Pagar sus propios compromisos | ✅ | — |
| Reportar un cobro | ✅ | — |
| Recomendar una organización | ✅ | — |
| Buscar y ver compromisos de todos los clientes | — | ✅ |
| **Registrar** compromisos de pago | — | ✅ |
| **Modificar** concepto, monto, vencimiento y cuotas | — | ✅ |
| **Anular** compromisos indicando el motivo | — | ✅ |
| Tomar y resolver reportes | — | ✅ |
| Crear y editar clientes | — | ✅ |
| Ver el historial de movimientos de toda la app | — | ✅ |

**Cómo se gestiona un compromiso:** el administrador lo registra desde "Compromisos" (elige la empresa, la persona por RUT y los datos del cobro), lo puede modificar mientras no esté pagado, anulado ni en revisión, y lo anula indicando un motivo. Cada uno de estos cambios queda guardado en la tabla `movimiento` con la fecha, el administrador que lo hizo y qué cambió, y el cliente lo ve en su historial. Un usuario final solo puede pagar o reportar compromisos que están a su nombre.

## Seguridad de la contraseña

- La contraseña debe cumplir **5 requisitos**: al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial. Mientras se escribe se ve una lista que se marca en verde y una barra de seguridad.
- Cada campo de contraseña tiene **su propio ícono de ojo**, así se puede mostrar u ocultar cada una por separado.
- Se guarda como hash SHA-256 con el RUT, nunca en texto plano.

### Recuperar la contraseña en 3 pasos

1. La persona escribe solo el correo de su cuenta, como en cualquier app. Por seguridad la app no dice si ese correo está registrado.
2. Se genera un **código de 6 dígitos** con `SecureRandom`. En la base de datos se guarda solo su hash. El código **dura 10 minutos**, permite **3 intentos** y al pedir uno nuevo el anterior deja de servir. Llega como notificación (simula el correo que se enviaría en producción).
3. Con el código correcto se crea la contraseña nueva, que también debe cumplir los 5 requisitos. El código se marca como usado y queda el movimiento "Contraseña recuperada con código".

## Recursos nativos

- **Cámara y galería** para la foto de perfil, con permisos en tiempo de ejecución.
- **Notificaciones** con `NotificationCompat` y **WorkManager**: un trabajo diario a las 9:00 revisa los vencimientos y avisa aunque la app esté cerrada. Se pide el permiso `POST_NOTIFICATIONS` en Android 13 o superior.

## Arquitectura

- **MVVM** con Jetpack Compose y Navigation Compose.
- **Capa de datos con Flow**: `PagoFlexRepositorio` expone `StateFlow` (datos, usuario actual, búsqueda, preferencias). Cada vez que se guarda algo en SQLite, el repositorio vuelve a leer la base y emite los datos nuevos, y la interfaz se actualiza sola con `collectAsStateWithLifecycle`.
- **Sincronización automática**: no hay botón "Actualizar". El ViewModel tiene un `Flow` que sincroniza al abrir la app y luego cada minuto (`stateIn` con `SharingStarted.WhileSubscribed`), así que solo trabaja mientras la app está en pantalla. Al volver la conexión también se sincroniza sola.
- **SQLite** con `SQLiteOpenHelper` (versión 6, 26 tablas). Las escrituras van en transacciones y las versiones nuevas se migran sin perder datos:
  - v4: contraseñas de prueba con los nuevos requisitos.
  - v5: tabla `codigo_recuperacion`.
  - v6: tablas `tipo_movimiento` y `movimiento`.
- **SharedPreferences** para la sesión, el modo oscuro y la última actualización.
- **Animaciones**: `AnimatedVisibility` en mensajes de error y filtros, `animateColorAsState` y `animateFloatAsState` en los requisitos de la contraseña, `AnimatedContent` en los pasos de recuperación, `animateIntAsState` en el total por pagar, `Crossfade` en el ojo de la contraseña y `animateItem` en las listas.

```
app/src/main/java/com/example/pagoflex
├── data            SQLite, repositorio con Flow y SharedPreferences
├── model           Modelos, reglas de negocio, permisos por rol y movimientos
├── navigation      Rutas y navegación según el rol
├── notificaciones  Canales, WorkManager y recordatorios
├── permisos        Permisos de cámara, galería y notificaciones
├── ui              Pantallas, componentes, estilos y tema
└── viewmodel       ViewModels de la app y de cada formulario
```

## Cómo ejecutar

1. Instalar una versión actual de **Android Studio** (el proyecto usa AGP 9.4, Kotlin 2.2 y compileSdk 37).
2. Clonar el repositorio:
   ```
   git clone https://github.com/Daniloko12/PagoFlex.git
   ```
3. Abrir la carpeta en Android Studio y esperar a que termine **Gradle Sync**.
4. Crear o abrir un emulador con **Android 8.0 (API 26) o superior**. Para ver la solicitud de permiso de notificaciones conviene uno con Android 13 o superior.
5. Presionar **Run 'app'**.

### Cuentas de prueba

Todas usan la contraseña **`Pago#2026`**. En la pantalla de inicio de sesión se pueden tocar para completar los datos.

| Rol | Correo |
|---|---|
| Usuario final | crojas@correo.ejemplo |
| Usuario final | lmunoz@correo.ejemplo |
| Administrador | alagos@pagoflex.ejemplo |
| Administrador | dortiz@pagoflex.ejemplo |

Para probar la recuperación usar, por ejemplo, `crojas@correo.ejemplo`.

### Pruebas

- Pruebas unitarias (reglas de negocio, contraseña, código de recuperación, filtros, permisos, movimientos):
  ```
  ./gradlew test
  ```
- Pruebas instrumentadas de la base de datos (necesitan un emulador encendido):
  ```
  ./gradlew connectedAndroidTest
  ```

En Windows se usa `gradlew.bat` en lugar de `./gradlew`, o se ejecutan desde Android Studio con clic derecho en la carpeta `test` o `androidTest` → **Run Tests**.

## Trabajo en equipo

- `main`: versión entregada.
- `develop`: integración de lo que se va terminando.
- `feature/*` y `docs/*`: una rama por funcionalidad, que entra a `develop` con un pull request.
