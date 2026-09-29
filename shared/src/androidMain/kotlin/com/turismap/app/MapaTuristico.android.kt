package com.turismap.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.turismap.app.data.models.Lugar
import com.google.maps.android.compose.rememberCameraPositionState

@Composable
actual fun MapaTuristico(
    lugares: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier
) {
    val ataco = LatLng(13.8697, -89.8486)

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(ataco, 15f)
    }

    // Estilo JSON: apaga comercios y puntos genéricos de Google
    val estiloLimpio = """
        [
          {
            "featureType": "poi",
            "elementType": "all",
            "stylers": [
              { "visibility": "off" }
            ]
          }
        ]
    """.trimIndent()

    val mapProperties = remember {
        MapProperties(
            mapStyleOptions = MapStyleOptions(estiloLimpio)
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        lugares.forEach { lugar ->
            // Asumiendo que usas Google Maps Compose
            Marker(
                state = MarkerState(position = LatLng(lugar.ubicacionCoordenadas.latitud, lugar.ubicacionCoordenadas.longitud)),
                title = lugar.nombre,
                snippet = lugar.subtitulo, // Antes era descripcion
                onClick = {
                    onLugarClick(lugar)
                    false
                }
            )
        }
    }
}