# PagoFlex

App Android para revisar y pagar compromisos de pago (cuotas, arriendos, mensualidades) con las organizaciones que cobran a través de PagoFlex. Proyecto del ramo **DSY1105 Desarrollo de Aplicaciones Móviles** de Duoc UC.

La app tiene dos roles que entran por el mismo inicio de sesión: el **usuario final** ve, paga y reporta sus compromisos, y el **administrador** registra, modifica y gestiona los compromisos de todos los clientes.

## Integrantes

| Integrante | GitHub | Aporte principal |
|---|---|---|
| Jose Veliz | [velizntd](https://github.com/velizntd) | Cámara y galería, contraseña segura, recuperación con código, sincronización con Flow, permisos por rol, historial de movimientos, inicio con filtros, pagos agregados por el cliente, fotos de boletas, microservicios de usuarios y pagos, animaciones y pruebas |
| (nombre del compañero) | [Daniloko12](https://github.com/Daniloko12) | Base del proyecto, base de datos, navegación por rol, panel del administrador, notificaciones de recordatorio, escáner de boletas, contacto por correo, microservicios de compromisos y movimientos, conexión de la app con Retrofit y presentación con varios PCs |

## Funcionalidades

### Usuario final
- Inicio resumido: total por pagar del mes, próximo vencimiento y la lista de compromisos con **filtros por estado, organización y vencimiento**.
- **Agregar sus propias cuentas o boletas por pagar** desde Inicio, escribiendo los datos o **escaneando la boleta con la cámara**.
- Detalle de cada compromiso con su línea de tiempo (quién lo registró, si se modificó, cuándo se pagó).
- Pago con tarjeta guardada o nueva (solo se guardan la marca y los 4 últimos dígitos) y comprobante para compartir.
- Avance de cuotas por empresa ("cuota 11 de 12" hasta quedar al día), indicador de cumplimiento y reconocimientos.
- **Historial con dos pestañas**: pagos (filtrables por empresa, año y mes) y movimientos de la cuenta.
- Reporte de un cobro que no reconoce.
- **Contacto por correo**: escribir a la organización que cobra desde el detalle del compromiso, o a soporte desde Mi perfil y desde la recuperación de contraseña.
- Avisos de vencimiento con **notificaciones** diarias y botón para probar un aviso.
- Mi perfil: foto con **cámara o galería**, editar datos y cambiar contraseña.
- Modo oscuro y claro, y modo sin conexión de demostración.

### Administrador
- Panel con gráficos de cobranza, deuda por empresa y vencimientos próximos.
- Registrar, buscar, modificar y anular compromisos.
- Ver quién registró cada compromiso (un administrador o el propio cliente desde la app) y la **foto de la boleta** si se escaneó.
- **Escribir por correo al cliente** desde el detalle del compromiso.
- Crear y editar clientes, restablecer su contraseña.
- Atender reportes y avanzar recomendaciones.
- **Historial de movimientos** de toda la app (auditoría), con búsqueda y filtros.

## Roles y permisos

Los permisos están centralizados en `model/PermisosRol.kt`. El ViewModel revisa el permiso antes de cada acción y la navegación no abre una pantalla si el rol no la tiene, así que aunque alguien llegue a la pantalla no puede guardar nada.

| Acción | Usuario final | Administrador |
|---|:---:|:---:|
| Ver sus compromisos, avance e historial | ✅ | — |
| **Agregar sus propias cuentas o boletas por pagar** | ✅ | — |
| Pagar sus propios compromisos | ✅ | — |
| Reportar un cobro | ✅ | — |
| Recomendar una organización | ✅ | — |
| Buscar y ver compromisos de todos los clientes | — | ✅ |
| **Registrar** compromisos de pago para cualquier cliente | — | ✅ |
| **Modificar** concepto, monto, vencimiento y cuotas | — | ✅ |
| **Anular** compromisos indicando el motivo | — | ✅ |
| Tomar y resolver reportes | — | ✅ |
| Crear y editar clientes | — | ✅ |
| Ver el historial de movimientos de toda la app | — | ✅ |
| Escribir por correo (organización, cliente o soporte) | ✅ | ✅ |

**Cómo se gestiona un compromiso:** el administrador lo registra desde "Compromisos" (elige la empresa, la persona por RUT y los datos del cobro), lo puede modificar mientras no esté pagado, anulado ni en revisión, y lo anula indicando un motivo. Cada uno de estos cambios queda guardado en la tabla `movimiento` con la fecha, el administrador que lo hizo y qué cambió, y el cliente lo ve en su historial. Un usuario final solo puede pagar o reportar compromisos que están a su nombre.

**El cliente también agrega sus pagos:** desde Inicio → "+ Agregar una cuenta por pagar" elige quién le cobra y escribe o escanea los datos. La cuenta queda Pendiente a su nombre y se puede pagar desde PagoFlex. El movimiento guarda que la agregó él mismo, y en el detalle el administrador ve "Registrado por: El cliente, desde la app". Así el administrador no tiene que cargar cada boleta. El cliente no puede agregar cuentas a nombre de otra persona.

## Escanear una boleta

1. En "Agregar cuenta por pagar" se toca **Tomar foto** (pide el permiso de cámara) o **Elegir foto** (selector de fotos de Android, no necesita permiso).
2. La foto se lee con **ML Kit Text Recognition** de Google, que funciona en el teléfono y sin internet.
3. `model/LectorBoleta.kt` busca en el texto:
   - **Monto**: el valor con `$` o el que aparece junto a "Total", "Monto" o "A pagar" (`$ 185.000`, `45990`).
   - **Vencimiento**: la fecha junto a "Vence" o "Vencimiento", en formatos `25-10-2026`, `25/10/2026`, `05.12.26` o `15 de noviembre de 2026`.
   - **Organización**: por su nombre, razón social o RUT.
4. Se completan los campos y se muestra qué se leyó. La persona revisa antes de agregar, y lo que no se leyó lo escribe a mano.
5. La foto se envía a `ms-compromisos` en Base64 y se guarda en la tabla `boleta` (JPEG de máximo 1280 px) junto al compromiso, en la misma transacción. El cliente y el administrador la ven en el detalle como "Boleta escaneada".

## Seguridad de la contraseña

- La contraseña debe cumplir **5 requisitos**: al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial. Mientras se escribe se ve una lista que se marca en verde y una barra de seguridad.
- Cada campo de contraseña tiene **su propio ícono de ojo**, así se puede mostrar u ocultar cada una por separado.
- `ms-usuarios` la guarda como hash SHA-256 con el RUT, nunca en texto plano, y la revisa al iniciar sesión. El hash nunca sale del servidor.

### Recuperar la contraseña en 3 pasos

1. La persona escribe solo el correo de su cuenta, como en cualquier app. Por seguridad la app no dice si ese correo está registrado.
2. Se genera un **código de 6 dígitos** con `SecureRandom`. `ms-usuarios` guarda solo su hash y es el que revisa el código. El código **dura 10 minutos**, permite **3 intentos** y al pedir uno nuevo el anterior deja de servir. Llega como notificación (simula el correo que se enviaría en producción).
3. Con el código correcto se crea la contraseña nueva, que también debe cumplir los 5 requisitos. El código se marca como usado y queda el movimiento "Contraseña recuperada con código".

## Recursos nativos

- **Cámara y galería** para la foto de perfil y para **escanear boletas**, con permisos en tiempo de ejecución. La foto de la boleta se toma en resolución completa con `ActivityResultContracts.TakePicture` y un `FileProvider`.
- **Lectura de texto** con ML Kit Text Recognition (modelo incluido en la app, funciona sin internet).
- **Correo** con un `Intent` `ACTION_SENDTO` (`mailto:`) que abre la app de correo con el destinatario, el asunto y el mensaje listos. Si el teléfono no tiene app de correo, se muestra la dirección para escribir.
- **Notificaciones** con `NotificationCompat` y **WorkManager**: un trabajo diario a las 9:00 revisa los vencimientos y avisa aunque la app esté cerrada. Se pide el permiso `POST_NOTIFICATIONS` en Android 13 o superior.

## Arquitectura

- **MVVM** con Jetpack Compose y Navigation Compose.
- **Datos en microservicios**: la app ya no tiene base de datos local. Todo se lee y se guarda en los 4 microservicios del backend (ver [Microservicios](#microservicios)) con **Retrofit** y **Gson**. Hay una interfaz por microservicio en `data/remoto` (`UsuariosApi`, `CompromisosApi`, `PagosApi`, `MovimientosApi`) con funciones `suspend`, y `ServidorPagoFlex` arma los clientes con la dirección del servidor y los adaptadores de fechas y fotos (Base64).
- **Capa de datos con Flow**: `PagoFlexRepositorio` expone `StateFlow` (datos, usuario actual, búsqueda, preferencias). Cada vez que se guarda algo, el repositorio vuelve a pedir los datos a los microservicios y emite los nuevos, y la interfaz se actualiza sola con `collectAsStateWithLifecycle`. Si el servidor responde con un error, la app muestra su mensaje ("Ese correo ya está registrado", "Ese compromiso ya está pagado").
- **Corrutinas**: las llamadas al servidor son `suspend` y se lanzan desde la interfaz con `rememberCoroutineScope`, así la pantalla nunca se congela.
- **Sincronización automática**: no hay botón "Actualizar". El ViewModel tiene un `Flow` que sincroniza con el servidor al abrir la app y luego cada minuto (`stateIn` con `SharingStarted.WhileSubscribed`), así que lo que registra otra persona aparece solo. Si el servidor no responde, la app avisa "Sin conexión" y muestra la última información conocida.
- **Pantalla de conexión**: al abrir la app se ve "Conectando con los servicios de PagoFlex…" mientras llegan los datos. Si el servidor está apagado aparecen **Reintentar** y **Cambiar la IP del servidor**. La IP elegida se guarda en `SharedPreferences`.
- **SharedPreferences** para la sesión, el modo oscuro, la IP del servidor y la última actualización.
- **Animaciones**: `AnimatedVisibility` en mensajes de error y filtros, `animateColorAsState` y `animateFloatAsState` en los requisitos de la contraseña, `AnimatedContent` en los pasos de recuperación, `animateIntAsState` en el total por pagar, `Crossfade` en el ojo de la contraseña y `animateItem` en las listas.

```
app/src/main/java/com/example/pagoflex
├── contacto        Correo con Intent ACTION_SENDTO
├── data            Repositorio con Flow, SharedPreferences y data/remoto con Retrofit
├── escaner         Foto de la boleta y lectura de texto con ML Kit
├── model           Modelos, reglas de negocio, permisos, movimientos, lector de boletas y correos
├── navigation      Rutas y navegación según el rol
├── notificaciones  Canales, WorkManager y recordatorios
├── permisos        Permisos de cámara, galería y notificaciones
├── ui              Pantallas, componentes, estilos y tema
└── viewmodel       Los 4 ViewModels de la app
```

La app tiene **4 ViewModels**:

| ViewModel | Qué maneja |
|---|---|
| `PagoFlexViewModel` | El principal: datos del servidor, sesión, conexión, sincronización y todas las acciones que guardan en los microservicios |
| `CuentaViewModel` | Formularios de la cuenta: iniciar sesión, crear cuenta, cambiar contraseña, recuperar contraseña, editar perfil y datos de un cliente |
| `CompromisoViewModel` | Formularios de compromisos: registrar (con la boleta escaneada), editar, reportar un problema y recomendar una organización |
| `PagoViewModel` | Formularios de pago: pagar un compromiso y agregar una tarjeta |

## Microservicios

El backend está en la carpeta `backend/`: 4 microservicios en **Java 21 con Spring Boot 3.5** que se abren y se ejecutan desde **VS Code**. Cada microservicio tiene su propia base de datos **H2** (un archivo en la carpeta `pagoflex-datos` del usuario), así que no hay que instalar un motor de base de datos. Si se quiere usar **MySQL**, cada microservicio trae el perfil `mysql`.

| Microservicio | Puerto | Base de datos | Qué maneja |
|---|---|---|---|
| `ms-usuarios` | 8081 | `usuarios` | Cuentas, inicio de sesión, contraseñas, recuperación con código, comunas y reconocimientos |
| `ms-compromisos` | 8082 | `compromisos` | Organizaciones, compromisos, fotos de boletas, anulaciones, reportes y recomendaciones |
| `ms-pagos` | 8083 | `pagos` | Pagos y tarjetas guardadas |
| `ms-movimientos` | 8084 | `movimientos` | Historial de movimientos (quién hizo qué y cuándo) |

**Comunicación entre microservicios:** con `RestClient`. Por ejemplo, al registrar un pago, `ms-pagos` le pide el compromiso a `ms-compromisos`, revisa que no esté pagado ni anulado, guarda el pago, deja el compromiso como pagado en `ms-compromisos`, envía los reconocimientos a `ms-usuarios` y el movimiento a `ms-movimientos`. Si otro microservicio no responde, el pago no se guarda.

Cada microservicio sigue las capas `controller` → `service` → `repository` (Spring Data JPA) → `model`, y al partir con la base vacía carga los mismos datos de prueba de la app (`datos.sql`).

### Levantar el backend

1. Instalar **JDK 21** (por ejemplo Eclipse Temurin 21) y **VS Code** con las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack** (VS Code las sugiere al abrir la carpeta).
2. En VS Code: **File → Open Folder** → la carpeta `backend`. Esperar a que la barra de abajo diga que terminó de cargar los proyectos Java; la primera vez descarga las librerías.
3. Ir a **Run and Debug** (Ctrl+Shift+D), elegir **Todos los microservicios** y presionar ▶. Se levantan los 4.
4. Probar en el navegador:
   - http://localhost:8082/api/empresas: tiene que mostrar el JSON con las organizaciones.
   - Swagger de cada uno: http://localhost:8081/swagger-ui.html, :8082, :8083 y :8084.
   - Consola de la base de datos: http://localhost:8081/h2-console, con JDBC URL `jdbc:h2:file:~/pagoflex-datos/usuarios`, usuario `sa` y sin contraseña (en cada puerto se cambia `usuarios` por el nombre de su base).

Para volver a los datos de prueba: `POST /api/admin/restablecer` en cada microservicio, o el botón "Restablecer datos de prueba" de la app.

### Presentar con varios PCs

Un PC levanta los 4 microservicios (el **servidor**) y los demás PCs abren la app en su emulador y se conectan a la IP del servidor. Lo que se hace en un PC se ve en los otros, porque todos usan las mismas bases de datos.

1. **En el servidor**, buscar su IP en la red (`ip addr` en Linux, `ipconfig` en Windows), por ejemplo `192.168.1.20`.
2. **Abrir los puertos 8081 a 8084 en el servidor:**
   - Linux (Fedora/Bazzite): `sudo firewall-cmd --permanent --add-port=8081-8084/tcp` y luego `sudo firewall-cmd --reload`.
   - Windows: al levantar los microservicios por primera vez, permitir Java en **redes privadas**, y que la red wifi esté como **privada**.
3. **Desde otro PC**, abrir en el navegador `http://192.168.1.20:8082/api/empresas`. Si aparece el JSON, la red funciona.
4. **En la app del otro PC**, en la pantalla de inicio de sesión tocar **Servidor: … · Cambiar**, escribir `192.168.1.20` y **Conectar**. La app lo recuerda.

En el emulador del mismo PC que el servidor se usa `10.0.2.2`, que viene por defecto.

### Usar MySQL en vez de H2 (opcional)

Con un MySQL encendido (por ejemplo XAMPP), cada microservicio crea su base al iniciar. En `.vscode/launch.json` se agrega a cada configuración `"args": "--spring.profiles.active=mysql"`. El usuario y la clave se cambian con las variables `DB_USER` y `DB_PASSWORD` (por defecto `root` sin clave).

## Cómo ejecutar

1. Instalar una versión actual de **Android Studio** (el proyecto usa AGP 9.4, Kotlin 2.2 y compileSdk 37).
2. Clonar el repositorio:
   ```
   git clone https://github.com/Daniloko12/PagoFlex.git
   ```
3. Abrir la carpeta en Android Studio y esperar a que termine **Gradle Sync** (necesita internet la primera vez para descargar las librerías, incluida ML Kit).
4. Crear o abrir un emulador con **Android 8.0 (API 26) o superior**. Para ver la solicitud de permiso de notificaciones conviene uno con Android 13 o superior.
5. Levantar el backend desde VS Code (ver [Levantar el backend](#levantar-el-backend)).
6. Presionar **Run 'app'**.

La app se conecta a `10.0.2.2`, que es la dirección del PC visto desde el emulador. Para usar otro PC o un teléfono real, en el inicio de sesión se toca **Servidor: … · Cambiar** y se escribe la IP del PC que tiene los microservicios.

### Cuentas de prueba

Todas usan la contraseña **`Pago#2026`**. En la pantalla de inicio de sesión se pueden tocar para completar los datos.

| Rol | Correo |
|---|---|
| Usuario final | crojas@correo.ejemplo |
| Usuario final | lmunoz@correo.ejemplo |
| Administrador | alagos@pagoflex.ejemplo |
| Administrador | dortiz@pagoflex.ejemplo |

Para probar la recuperación usar, por ejemplo, `crojas@correo.ejemplo`.

Para probar el escáner en el emulador: arrastrar una imagen de una boleta a la ventana del emulador (queda en Descargas) y en "Agregar cuenta por pagar" tocar **Elegir foto**. La cámara del emulador muestra una escena virtual, así que para probar **Tomar foto** con una boleta real conviene un teléfono.

### Pruebas

- Pruebas unitarias de los ViewModels, **4 por cada uno**:

  | Archivo | Qué prueba |
  |---|---|
  | `PagoFlexViewModelTest` | Carga de datos y sesión guardada, cambio de contraseña sin sesión o insegura, compromiso ajeno o sin conexión, búsqueda del administrador y cierre de sesión |
  | `CuentaViewModelTest` | Inicio de sesión con errores, registro con RUT repetido y sin términos, cambio de contraseña rechazado y cambio de contraseña válido |
  | `CompromisoViewModelTest` | Registro sin organización o con cuota repetida, boleta escaneada, edición con fecha pasada o cuota repetida, reporte "Otro" sin comentario |
  | `PagoViewModelTest` | Pago sin medio de pago o sin conexión, pago con tarjeta guardada, pago con tarjeta nueva y agregar tarjeta vencida o válida |

  `PagoFlexViewModelTest` usa un repositorio con datos de prueba y `PreferenciasEnMemoria` en lugar del servidor y del teléfono, y `ReglaCorrutinas` con `kotlinx-coroutines-test` para las corrutinas.
- Además hay pruebas del modelo: reglas de negocio, contraseña, código de recuperación, filtros, permisos, movimientos, búsqueda, IP del servidor, lector de boletas, mensajes de correo y formato JSON del servidor.
- Para ejecutar todas:
  ```
  ./gradlew test
  ```

En Windows se usa `gradlew.bat` en lugar de `./gradlew`, o se ejecutan desde Android Studio con clic derecho en la carpeta `test` → **Run Tests**.

## Trabajo en equipo

- `main`: versión entregada.
- `develop`: integración de lo que se va terminando.
- `feature/*` y `docs/*`: una rama por funcionalidad, que entra a `develop` con un pull request.
