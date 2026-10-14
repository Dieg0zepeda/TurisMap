# Esquema de base de datos (Cloud Firestore)

## `lugares/{lugarId}`

Documento público de cada municipio. El ID del documento es semántico (por ejemplo, `ataco`).

| Campo | Tipo |
|---|---|
| `id`, `nombre`, `subtitulo`, `ubicacionCorta`, `ubicacionLarga`, `elevacion`, `clima`, `historia`, `mejorTemporadaTitulo`, `mejorTemporadaDesc` | String |
| `categoria` | String: `TODOS`, `CAFE`, `MIRADOR`, `ARTESANIA` o `RESTAURANTE` |
| `calificacion` | Number |
| `cantidadResenas` | Number entero |
| `ubicacionCoordenadas` | Map con `latitud` y `longitud` (Number) |
| `directorioStats` | Map con `tituloGeneral`, `descripcionGeneral` y campos `stat1Icon`-`stat4Icon` / `stat1Texto`-`stat4Texto` (String) |
| `atractivos` | Array de Maps con `id`, `etiqueta`, `etiquetaColorHex` (Number), `titulo`, `descripcion`, `accion` e `imagenUrl` |

La aplicación solo lee lugares desde el cliente; las escrituras son administrativas.

## `usuarios/{uid}`

El ID del documento debe ser el UID de Firebase Authentication.

| Campo | Tipo |
|---|---|
| `email` | String |
| `rol` | String: `admin` o `viajero` |
| `nombre`, `intereses`, `fotoPerfil`, `fotoPortada` | String |
| `favoritos` | Array de IDs de documentos de `lugares` |
| `preferencias` | Map opcional para preferencias sincronizadas |

El cliente puede crear su propio documento como `viajero` y editar solo los campos de perfil permitidos. No puede asignarse ni cambiarse el rol.

### `usuarios/{uid}/historial_rutas/{visitaId}`

| Campo | Tipo |
|---|---|
| `lugarId` | String: ID de un documento de `lugares` |
| `fechaVisita` | Timestamp |
| `tipoInteraccion` | String |

Cada viajero solo puede consultar y escribir su propio historial.

## `comercios/{comercioId}`

Un comercio pertenece a una cuenta y permanece pendiente hasta que un administrador lo publique.

| Campo | Tipo |
|---|---|
| `propietarioUid`, `lugarId`, `nombre`, `descripcion`, `categoria`, `direccion`, `telefono`, `sitioWeb`, `horario`, `estado` | String |
| `estado` | String: `pendiente` o `publicado` |
| `precioDesde`, `calificacionPromedio` | Number |
| `cantidadResenas` | Number entero |
| `servicios`, `imagenUrls` | Array de String |
| `creadoEn`, `actualizadoEn` | Timestamp |

Los visitantes pueden leer comercios publicados. El propietario autenticado puede crear un comercio propio en estado `pendiente` y editar sus datos sin cambiar propietario ni estado. Solo un administrador puede publicarlo o eliminarlo.

## `resenas/{resenaId}`

Se usa un documento por usuario y destino para evitar reseñas duplicadas. El ID determinista es `{tipoDestino}_{destinoId}_{uid}`, usando `lugar` o `comercio` como tipo.

| Campo | Tipo |
|---|---|
| `tipoDestino` | String: `lugar` o `comercio` |
| `destinoId`, `usuarioUid`, `comentario` | String |
| `calificacion` | Integer de 1 a 5 |
| `creadoEn`, `actualizadoEn` | Timestamp |

Las reseñas se pueden leer públicamente. Un usuario autenticado puede crear o editar solo su propia reseña; no puede reseñar su propio comercio. El promedio y el conteo agregados en `lugares` o `comercios` deben actualizarse desde un entorno confiable (por ejemplo, Cloud Functions), no desde el cliente.

## Reglas de cliente

`firestore.rules` aplica lectura pública solo a lugares y reseñas, publicación administrativa de comercios, propiedad de cuentas y validación básica de reseñas. Los cambios de esquema deben acompañarse de cambios en esas reglas.

## Configuración de iOS

La implementación iOS usa Firebase Kotlin SDK y requiere enlazar el SDK nativo Firestore con CocoaPods. Desde `iosApp`, ejecutar `pod install` y abrir `iosApp.xcworkspace`; `GoogleService-Info.plist` debe estar incluido en el bundle de la aplicación.
