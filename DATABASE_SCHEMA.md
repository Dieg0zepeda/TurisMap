# Esquema de Base de Datos (Firestore) - TurisMap

## Colección: `lugares`
Colección principal para el catálogo de destinos. Optimizado para 1 sola lectura por vista de detalle.

* **ID Documento:** Semántico (ej. `apaneca`, `ataco`).
* **Campos Base (String):** `id`, `nombre`, `subtitulo`, `ubicacionCorta`, `ubicacionLarga`, `elevacion`, `clima`, `historia`, `mejorTemporadaTitulo`, `mejorTemporadaDesc`.
* **Campos Numéricos (Number):** `calificacion`, `cantidadResenas`.
* **Campos Complejos:**
    * `ubicacionCoordenadas` (Map): `latitud` (Number), `longitud` (Number).
    * `directorioStats` (Map): `tituloGeneral` (String), `descripcionGeneral` (String), `stat1Icon` (String), `stat1Texto` (String).
    * `atractivos` (Array de Maps): `id` (String), `titulo` (String), `descripcion` (String), `etiqueta` (String), `etiquetaColorHex` (Number), `accion` (String).

## Colección: `usuarios`
Colección para la gestión de perfiles y permisos (RBAC).

* **ID Documento:** Firebase Auth UID (ej. `xYz123...`).
* **Campos Base:** `email` (String), `rol` (String: 'admin' | 'viajero').
* **Arrays:** `favoritos` (Array de IDs de lugares).

### Subcolección: `usuarios/{uid}/historial_rutas`
* **ID Documento:** Autogenerado.
* **Campos:** `lugarId` (String), `fechaVisita` (Timestamp), `tipoInteraccion` (String).