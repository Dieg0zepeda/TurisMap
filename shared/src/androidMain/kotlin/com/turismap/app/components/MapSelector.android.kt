package com.turismap.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.turismap.app.data.models.Coordenada

@Composable
actual fun MapSelector(
    coordenadaInicial: Coordenada,
    coordenadaSeleccionada: Coordenada?,
    onCoordenadaSeleccionada: (Coordenada) -> Unit,
    modifier: Modifier
) {
    val initialLatLng = if (coordenadaInicial.latitud in -90.0..90.0 &&
        coordenadaInicial.longitud in -180.0..180.0 &&
        (coordenadaInicial.latitud != 0.0 || coordenadaInicial.longitud != 0.0)
    ) {
        LatLng(coordenadaInicial.latitud, coordenadaInicial.longitud)
    } else {
        LatLng(13.8697, -89.8486)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(initialLatLng, 15f)
    }

    val markerLatLng = coordenadaSeleccionada?.let {
        LatLng(it.latitud, it.longitud)
    } ?: initialLatLng
    val markerState = rememberMarkerState(position = markerLatLng)

    LaunchedEffect(coordenadaInicial) {
        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngZoom(initialLatLng, 15f),
            durationMs = 500
        )
    }

    LaunchedEffect(markerLatLng) {
        markerState.position = markerLatLng
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = true,
            myLocationButtonEnabled = false
        )
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = mapUiSettings,
            onMapClick = { latLng ->
                markerState.position = latLng
                onCoordenadaSeleccionada(
                    Coordenada(
                        latitud = latLng.latitude,
                        longitud = latLng.longitude
                    )
                )
            }
        ) {
            if (coordenadaSeleccionada != null) {
                com.google.maps.android.compose.Marker(
                    state = markerState,
                    draggable = false
                )
            }
        }

        Box(
            modifier = Modifier
                .size(12.dp)
                .border(2.dp, Color.Red, CircleShape)
                .background(Color.Transparent, CircleShape)
                .align(Alignment.Center)
        )
    }
}
