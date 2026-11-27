package com.turismap.app.components

import androidx.compose.runtime.Composable

internal fun esCoordenadaNavegable(latitud: Double, longitud: Double): Boolean =
    latitud.isFinite() &&
        longitud.isFinite() &&
        latitud in -90.0..90.0 &&
        longitud in -180.0..180.0 &&
        latitud != 0.0 &&
        longitud != 0.0

internal fun construirUriNavegacionAndroid(latitud: Double, longitud: Double): String =
    "google.navigation:q=$latitud,$longitud"

internal fun construirUrlFallbackGoogleMaps(latitud: Double, longitud: Double): String =
    "https://www.google.com/maps/dir/?api=1&destination=$latitud,$longitud"

/**
 * Devuelve una acción para iniciar navegación externa turno a turno hacia (latitud, longitud)
 * en Google Maps, con fallback automático a la URL HTTPS de direcciones si la app nativa no está disponible.
 * Devuelve `true` si se logró iniciar la navegación o `false` si no fue posible abrir una app compatible.
 */
@Composable
expect fun rememberIniciarRutaExterna(): (latitud: Double, longitud: Double, nombreDestino: String) -> Boolean
