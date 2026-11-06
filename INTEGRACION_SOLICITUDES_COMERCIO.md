# Integración de solicitudes de registro de comercios

Este documento describe cómo la aplicación registra las solicitudes de comercios en Cloud Firestore y cómo integrar el mismo flujo desde otra plataforma, por ejemplo una aplicación web.

## 1. Modelo de datos y colección

Las solicitudes de cualquier plan se guardan en la colección:

```text
solicitudes_comercio/{solicitudId}
```

No se crea un documento en `comercios` cuando se envía la solicitud. `comercios` es otra colección y se usa para los comercios publicados o para el flujo independiente que registra un comercio directamente. La solicitud conserva el plan elegido en `planId`; por eso no se necesita una colección distinta por tipo de plan.

El documento actual tiene este contrato:

| Campo | Tipo Firestore | Descripción |
|---|---|---|
| `id` | string | ID del documento; debe ser igual a `{solicitudId}`. |
| `propietarioUid` | string | UID del usuario de Firebase Authentication que envía la solicitud. |
| `nombreCompleto` | string | Nombre del solicitante. |
| `email` | string | Correo electrónico asociado a la sesión Firebase. |
| `municipio` | string | Nombre del documento de `lugares` seleccionado en el formulario. Actualmente se guarda el nombre, no el ID del lugar. |
| `nombreNegocio` | string | Nombre del negocio. |
| `categoria` | string | ID string del documento activo en `categorias_comercio`, no la etiqueta que se muestra en pantalla. |
| `descripcion` | string | Descripción del negocio. |
| `direccion` | string | Dirección textual del negocio. |
| `telefono` | string | Teléfono de contacto. |
| `sitioWeb` | string | URL proporcionada; se guarda como string y puede estar vacío. La interfaz exige `https://` cuando no está vacío. |
| `horario` | string | Horario del negocio; se guarda como string y puede estar vacío. |
| `planId` | string | ID string del documento activo en `planes_comercio` que el solicitante eligió. |
| `coordenadas` | map | Mapa con los campos numéricos `latitud` y `longitud`. |
| `estado` | string | Estado inicial literal `PENDIENTE`. |
| `fechaRegistro` | string | Fecha y hora ISO 8601 producida por `Clock.System.now().toString()` en el cliente actual. No es un Firestore Timestamp. |

Ejemplo de documento:

```json
{
  "id": "14b758a0-4b79-4b57-90d3-50d652c24f43",
  "propietarioUid": "firebase-auth-uid",
  "nombreCompleto": "Ana Ejemplo",
  "email": "ana@example.com",
  "municipio": "Suchitoto",
  "nombreNegocio": "Artesanías El Centro",
  "categoria": "artesanias_souvenirs",
  "descripcion": "Venta de artesanía local y recuerdos.",
  "direccion": "2a Avenida Sur, local 4",
  "telefono": "+503 2222 3333",
  "sitioWeb": "https://ejemplo.com",
  "horario": "Lunes: 09:00 - 17:00",
  "planId": "presencia_basica",
  "coordenadas": {
    "latitud": 13.9384,
    "longitud": -89.0276
  },
  "estado": "PENDIENTE",
  "fechaRegistro": "2026-11-04T19:49:48.244Z"
}
```

Los valores de ejemplo son ilustrativos. La plataforma debe usar los IDs y datos que realmente existan en Firestore.

### Estructura de los planes en `planes_comercio`

La app móvil lee los planes desde `planes_comercio/{documentId}`. El `documentId` de Firestore es el ID oficial del plan; el cliente no usa ni necesita un campo interno `id`. Los campos esperados son:

| Campo | Tipo Firestore | Comportamiento |
|---|---|---|
| `nombre` | string | Nombre visible del plan. |
| `descripcion` | string | Texto descriptivo. |
| `beneficios` | array de strings | Beneficios enumerados en la tarjeta. |
| `precio` | number | Importe numérico; puede faltar en documentos antiguos y se trata como precio no disponible. |
| `moneda` | string | Código recibido de Firestore, por ejemplo `USD`; no se infiere desde el cliente. |
| `periodicidad` | string | Periodicidad recibida de Firestore, por ejemplo `MES`; no se infiere desde el cliente. |
| `orden` | number | Orden ascendente de presentación. |
| `activo` | boolean | Sólo se muestran planes activos. |

Ejemplo para cargar desde Firestore:

```json
{
  "nombre": "Presencia Básica",
  "descripcion": "Presencia de tu negocio en el directorio.",
  "beneficios": ["Perfil del negocio en TurisMap"],
  "precio": 5,
  "moneda": "USD",
  "periodicidad": "MES",
  "orden": 1,
  "activo": true
}
```

Los valores iniciales que deben configurarse en Firestore son: `presencia_basica` = 5 USD/MES, `socio_turismap` = 15 USD/MES y `socio_premium` = 25 USD/MES. La app sólo presenta los valores leídos de Firestore; no los define en la interfaz ni los escribe en la base de datos. El precio se muestra con la moneda y periodicidad del documento (por ejemplo, `5 USD / MES`). Si `precio` falta o no es un número finito no negativo, se presenta “Precio no disponible”; no se asigna un valor predeterminado.

Al seleccionar un plan, la app transporta el ID del documento Firestore como `planId` hasta `solicitudes_comercio`. El campo interno `id`, si existiera en un documento de plan, no se usa como identidad y no afecta a la selección.

## 2. Flujo desde la aplicación actual

El flujo de interfaz está implementado en `shared/src/commonMain/kotlin/com/turismap/app/screens/ComercioSolicitudScreen.kt` y `SeleccionUbicacionComercioScreen.kt`.

1. `PlanesComercioScreen` carga los planes activos de `planes_comercio`. Al elegir uno, la navegación pasa el ID del documento como `planId`.
2. El formulario vuelve a cargar ese plan y las opciones de `categorias_comercio`. Las categorías tienen un ID string; para mostrarlas, la interfaz utiliza `nombre`, luego `descripcion` y finalmente el ID como etiqueta. Al guardar la solicitud utiliza el ID de la categoría.
3. El usuario elige un documento de `lugares`. La app conserva el objeto de lugar seleccionado y abre la pantalla de selección de ubicación.
4. En Android, `MapSelector` muestra Google Maps y el toque en el mapa produce una `Coordenada`. La pantalla también permite escribir latitud y longitud. En iOS el mapa es un placeholder; las coordenadas se pueden proporcionar manualmente.
5. El botón “Guardar ubicación y volver” valida que la latitud esté entre -90 y 90 y la longitud entre -180 y 180, entrega ambas al formulario y regresa a él. El formulario conserva sus demás campos mientras el selector está abierto.
6. El botón de envío llama a `ComercioRepository.enviarSolicitudComercio(...)`, en `shared/src/commonMain/kotlin/com/turismap/app/data/repository/ComercioRepository.kt`. Sólo se muestra la confirmación después de que la escritura asíncrona de Firestore termina correctamente. Una excepción se muestra como error y no se presenta como éxito.

El repositorio vuelve a validar los campos, exige sesión y correo autenticados, verifica que `planId` no esté vacío y valida que las coordenadas sean finitas y estén dentro de rango. Luego crea un documento con UUID en `solicitudes_comercio` y escribe el contrato descrito arriba.

## 3. Integración de escritura en una plataforma web

La plataforma web debe inicializar Firebase con el proyecto correcto, autenticar al usuario con Firebase Authentication y escribir usando el SDK cliente de Firestore. Ejemplo con el SDK modular de Firebase para TypeScript:

```ts
import {
  collection,
  doc,
  setDoc,
  type Firestore,
} from "firebase/firestore";
import type { Auth } from "firebase/auth";

type CoordenadasSolicitud = {
  latitud: number;
  longitud: number;
};

type DatosSolicitudComercio = {
  nombreCompleto: string;
  municipio: string;
  nombreNegocio: string;
  categoria: string; // ID de categorias_comercio
  descripcion: string;
  direccion: string;
  telefono: string;
  sitioWeb: string;
  horario: string;
  planId: string; // ID de planes_comercio
  coordenadas: CoordenadasSolicitud;
};

export async function crearSolicitudComercio(
  auth: Auth,
  db: Firestore,
  datos: DatosSolicitudComercio,
): Promise<string> {
  const usuario = auth.currentUser;
  if (!usuario) {
    throw new Error("Inicia sesión para enviar una solicitud.");
  }
  if (!usuario.email) {
    throw new Error("La sesión no tiene un correo electrónico asociado.");
  }

  const latitud = datos.coordenadas.latitud;
  const longitud = datos.coordenadas.longitud;

  if (
    !Number.isFinite(latitud) ||
    latitud < -90 ||
    latitud > 90 ||
    !Number.isFinite(longitud) ||
    longitud < -180 ||
    longitud > 180
  ) {
    throw new Error("Las coordenadas no son válidas.");
  }

  const nombreCompleto = datos.nombreCompleto.trim();
  const municipio = datos.municipio.trim();
  const nombreNegocio = datos.nombreNegocio.trim();
  const descripcion = datos.descripcion.trim();
  const direccion = datos.direccion.trim();
  const telefono = datos.telefono.trim();

  if (nombreCompleto.length < 1 || nombreCompleto.length > 120) {
    throw new Error("El nombre completo debe tener entre 1 y 120 caracteres.");
  }
  if (municipio.length < 1 || municipio.length > 100) {
    throw new Error("La ubicación debe tener entre 1 y 100 caracteres.");
  }
  if (nombreNegocio.length < 1 || nombreNegocio.length > 120) {
    throw new Error("El nombre del negocio debe tener entre 1 y 120 caracteres.");
  }
  if (!datos.categoria.trim() || !datos.planId.trim()) {
    throw new Error("Selecciona una categoría y un plan.");
  }
  if (descripcion.length < 1 || descripcion.length > 2000) {
    throw new Error("La descripción debe tener entre 1 y 2000 caracteres.");
  }
  if (direccion.length < 1 || direccion.length > 300) {
    throw new Error("La dirección debe tener entre 1 y 300 caracteres.");
  }
  if (telefono.length < 1 || telefono.length > 40) {
    throw new Error("El teléfono debe tener entre 1 y 40 caracteres.");
  }
  if (datos.sitioWeb && !datos.sitioWeb.startsWith("https://")) {
    throw new Error("El sitio web debe comenzar con https://.");
  }

  // Genera un ID de documento en el cliente; el mismo ID se guarda en el campo id.
  const referencia = doc(collection(db, "solicitudes_comercio"));
  const solicitud = {
    id: referencia.id,
    propietarioUid: usuario.uid,
    nombreCompleto,
    email: usuario.email,
    municipio,
    nombreNegocio,
    categoria: datos.categoria.trim(),
    descripcion,
    direccion,
    telefono,
    sitioWeb: datos.sitioWeb.trim(),
    horario: datos.horario.trim(),
    planId: datos.planId.trim(),
    coordenadas: { latitud, longitud },
    estado: "PENDIENTE" as const,
    fechaRegistro: new Date().toISOString(),
  };

  await setDoc(referencia, solicitud);
  return referencia.id;
}
```

`setDoc` es asíncrono: debe esperarse con `await`. Si Firestore rechaza la operación, por ejemplo por sesión inválida, documento relacionado inactivo o reglas no publicadas, `setDoc` falla; la plataforma debe comunicar ese error y no indicar que la solicitud se registró.

El ejemplo valida los límites de campos principales para anticipar rechazos de reglas. La regla de Firestore es la autoridad final y también verifica la existencia y estado activo de categoría y plan. La validación en cliente no reemplaza las reglas.

### Carga de opciones para los controles

La plataforma debe leer documentos activos y conservar sus IDs de documento:

```ts
import { collection, getDocs, query, where, type Firestore } from "firebase/firestore";

export async function cargarCategorias(db: Firestore) {
  const consulta = query(
    collection(db, "categorias_comercio"),
    where("activo", "==", true),
  );
  const resultado = await getDocs(consulta);

  return resultado.docs.map((documento) => {
    const data = documento.data();
    return {
      id: documento.id, // guardar este valor en solicitud.categoria
      etiqueta: data.nombre || data.descripcion || documento.id,
    };
  });
}

export async function cargarPlanes(db: Firestore) {
  const consulta = query(
    collection(db, "planes_comercio"),
    where("activo", "==", true),
  );
  const resultado = await getDocs(consulta);

  return resultado.docs.map((documento) => ({
    id: documento.id, // guardar este valor en solicitud.planId
    nombre: documento.data().nombre || documento.id,
  }));
}
```

La etiqueta es sólo para mostrar. No se debe enviar `nombre` o `descripcion` en vez del ID del documento. Los documentos que alimentan los controles deben tener `activo: true`; para una etiqueta informativa de categoría, se recomienda además configurar `nombre`.

## 4. Reglas de Firestore para solicitudes

En `firestore.rules`, la ruta `solicitudes_comercio/{solicitudId}` contiene las reglas del flujo:

- La creación requiere una sesión autenticada.
- `propietarioUid` debe ser igual a `request.auth.uid`.
- `email` debe coincidir con `request.auth.token.email`.
- `id` debe coincidir con el ID de documento.
- `hasAll(...)` exige los campos del esquema y `hasOnly(...)` prohíbe campos adicionales.
- La categoría debe ser el ID de un documento existente y activo de `categorias_comercio`.
- El plan debe ser el ID de un documento existente y activo de `planes_comercio`.
- `coordenadas` debe ser un mapa que sólo tenga `latitud` y `longitud`, ambos numéricos y dentro de sus rangos geográficos.
- El estado de creación debe ser exactamente `PENDIENTE`.
- El usuario puede leer documentos propios; un administrador puede leer solicitudes. Sólo el administrador puede actualizar o eliminar.

El usuario no administrador no puede modificar la solicitud después de crearla. Si el producto requiere edición, revisión o cambio de estado, debe definirse una operación y reglas específicas para ello; no se debe habilitar de forma general `update` a cualquier solicitante.

Las reglas de Firestore no sirven como filtros para una consulta. Para listar las solicitudes propias desde un cliente, consultar con `where("propietarioUid", "==", usuario.uid)`. Para el panel administrativo, la regla reconoce como administrador a un usuario cuyo documento `usuarios/{uid}` tiene `rol == "admin"`.

**Publicación:** modificar `firestore.rules` en el repositorio no publica las reglas automáticamente. Deben desplegarse al proyecto Firebase correspondiente, por ejemplo:

```sh
firebase deploy --only firestore:rules --project turismap-2026
```

Antes de ejecutar el despliegue, confirmar que `turismap-2026` es el proyecto destino correcto y que el usuario de Firebase CLI está autenticado con permisos para publicar reglas.

## 5. Integración con un servidor propio

Si la plataforma escribe desde un backend con Firebase Admin SDK, las reglas de Firestore **no se aplican** a esa escritura privilegiada. El servidor debe:

1. Verificar el token Firebase ID del usuario recibido.
2. Obtener UID y correo desde el token verificado, no confiar en valores equivalentes enviados en el cuerpo.
3. Validar esquema, longitudes, coordenadas y estado inicial.
4. Consultar que `categoria` y `planId` existan y tengan `activo: true`.
5. Forzar `propietarioUid`, `email`, `id`, `estado` y `fechaRegistro` en el servidor.
6. Escribir sólo en `solicitudes_comercio` y devolver el ID únicamente después de que la escritura termine correctamente.

Las credenciales de una cuenta de servicio nunca deben incluirse en código cliente ni distribuirse con la plataforma web.

## 6. Errores frecuentes al integrar

| Síntoma | Causa probable |
|---|---|
| `PERMISSION_DENIED` al crear | Reglas no publicadas, usuario no autenticado, correo/UID diferentes, campo adicional o faltante, categoría o plan no activos, o coordenadas inválidas. |
| El selector de categoría aparece vacío | No hay documentos con `activo: true`, faltan permisos de lectura, o los documentos no tienen un campo que pueda mostrarse. |
| La solicitud muestra un nombre de categoría pero se rechaza | Se envió la etiqueta visible en lugar del ID del documento en `categoria`. |
| El plan elegido no corresponde | Se envió el nombre del plan o un ID distinto de un documento activo de `planes_comercio` en `planId`. |
| El cliente muestra éxito antes de guardar | No esperó a que terminara `setDoc`/la operación Firestore. |
| Una consulta de solicitudes propias es denegada | La consulta no incluye el filtro `propietarioUid == uid`; las reglas no filtran resultados automáticamente. |
| La regla falla al validar coordenadas | Se guardó `coordenadas` como texto, como campos planos o con nombres distintos; el contrato exige un mapa con números `latitud` y `longitud`. |

## 7. Archivos relevantes del proyecto

- `shared/src/commonMain/kotlin/com/turismap/app/screens/PlanesComercioScreen.kt`: lista los planes activos y permite elegir uno.
- `shared/src/commonMain/kotlin/com/turismap/app/screens/ComercioSolicitudScreen.kt`: formulario, carga de opciones y envío.
- `shared/src/commonMain/kotlin/com/turismap/app/screens/SeleccionUbicacionComercioScreen.kt`: selección y validación de coordenadas antes de regresar al formulario.
- `shared/src/commonMain/kotlin/com/turismap/app/components/MapSelector.kt`: contrato multiplataforma del selector.
- `shared/src/androidMain/kotlin/com/turismap/app/components/MapSelector.android.kt`: selección con Google Maps en Android.
- `shared/src/iosMain/kotlin/com/turismap/app/components/MapSelector.ios.kt`: estado actual de la selección en iOS; permite recurrir a entrada manual de coordenadas.
- `shared/src/commonMain/kotlin/com/turismap/app/data/models/Coordenada.kt`: modelo con `Double` para `latitud` y `longitud`.
- `shared/src/commonMain/kotlin/com/turismap/app/data/models/CategoriaComercio.kt`: modelo y etiqueta de categoría.
- `shared/src/commonMain/kotlin/com/turismap/app/data/repository/ComercioRepository.kt`: consultas Firestore y escritura de `solicitudes_comercio`.
- `firestore.rules`: autorización y validación en el servidor de Firestore.
