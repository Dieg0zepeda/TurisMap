package com.turismap.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalUriHandler

@Composable
actual fun rememberIniciarRutaExterna(): (latitud: Double, longitud: Double, nombreDestino: String) -> Boolean {
    val uriHandler = LocalUriHandler.current
    return remember(uriHandler) {
        { latitud, longitud, _ ->
            if (!esCoordenadaNavegable(latitud, longitud)) {
                false
            } else {
                try {
                    uriHandler.openUri(construirUrlFallbackGoogleMaps(latitud, longitud))
                    true
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
