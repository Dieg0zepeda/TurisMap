package com.turismap.app.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private const val PAQUETE_GOOGLE_MAPS = "com.google.android.apps.maps"

@Composable
actual fun rememberIniciarRutaExterna(): (latitud: Double, longitud: Double, nombreDestino: String) -> Boolean {
    val context = LocalContext.current
    return remember(context) {
        { latitud, longitud, _ ->
            if (!esCoordenadaNavegable(latitud, longitud)) {
                false
            } else {
                val flagsActividad = if (context !is Activity) Intent.FLAG_ACTIVITY_NEW_TASK else 0

                val intentNativo = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(construirUriNavegacionAndroid(latitud, longitud))
                ).apply {
                    setPackage(PAQUETE_GOOGLE_MAPS)
                    if (flagsActividad != 0) addFlags(flagsActividad)
                }

                try {
                    context.startActivity(intentNativo)
                    true
                } catch (_: ActivityNotFoundException) {
                    val intentFallback = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(construirUrlFallbackGoogleMaps(latitud, longitud))
                    ).apply {
                        if (flagsActividad != 0) addFlags(flagsActividad)
                    }
                    try {
                        context.startActivity(intentFallback)
                        true
                    } catch (_: Exception) {
                        false
                    }
                } catch (_: Exception) {
                    false
                }
            }
        }
    }
}
