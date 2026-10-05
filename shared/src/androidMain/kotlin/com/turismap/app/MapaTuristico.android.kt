package com.turismap.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.turismap.app.data.models.Lugar
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.*

@Composable
actual fun MapaTuristico(
    lugares: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier
) {
    val context = LocalContext.current
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var shouldRequestPermission by remember { mutableStateOf(false) }

    // Verificar permisos de ubicación
    LaunchedEffect(Unit) {
        hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            obtenerUbicacionActual(context) { location ->
                userLocation = location
            }
        } else {
            shouldRequestPermission = true
        }
    }

    // Solicitar permisos si no están concedidos
    if (shouldRequestPermission) {
        // Nota: En una implementación completa, usaría ActivityCompat.requestPermissions
        // Por ahora, asumimos que el usuario concedió los permisos manualmente
        // o usamos la ubicación por defecto (Ataco)
        userLocation = LatLng(13.8697, -89.8486)
        hasPermission = false
        shouldRequestPermission = false
    }

    // Calcular lugares más cercanos (2-3)
    val lugaresCercanos = remember(userLocation, lugares) {
        if (userLocation == null || lugares.isEmpty()) {
            lugares
        } else {
            lugares.map { lugar ->
                val distancia = calcularDistancia(
                    userLocation!!.latitude,
                    userLocation!!.longitude,
                    lugar.ubicacionCoordenadas.latitud,
                    lugar.ubicacionCoordenadas.longitud
                )
                lugar to distancia
            }.sortedBy { it.second }.take(3).map { it.first }
        }
    }

    // Calcular bounds para mostrar los lugares cercanos
    val bounds = remember(lugaresCercanos) {
        if (lugaresCercanos.isEmpty()) {
            null
        } else {
            val builder = LatLngBounds.Builder()
            lugaresCercanos.forEach { lugar ->
                builder.include(
                    LatLng(
                        lugar.ubicacionCoordenadas.latitud,
                        lugar.ubicacionCoordenadas.longitud
                    )
                )
            }
            builder.build()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            userLocation ?: LatLng(13.8697, -89.8486),
            12f
        )
    }

    // Ajustar cámara para mostrar los bounds
    LaunchedEffect(bounds) {
        bounds?.let {
            cameraPositionState.move(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(it.center, 12f)
                )
            )
        }
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
            mapStyleOptions = MapStyleOptions(estiloLimpio),
            isMyLocationEnabled = hasPermission
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = hasPermission
        )
    }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        // Mostrar todos los marcadores
        lugares.forEach { lugar ->
            Marker(
                state = MarkerState(
                    position = LatLng(
                        lugar.ubicacionCoordenadas.latitud,
                        lugar.ubicacionCoordenadas.longitud
                    )
                ),
                title = lugar.nombre,
                snippet = lugar.subtitulo,
                onClick = {
                    onLugarClick(lugar)
                    false
                }
            )
        }
    }
}

// Función para obtener ubicación actual
private fun obtenerUbicacionActual(context: Context, callback: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(LatLng(location.latitude, location.longitude))
            } else {
                // Si no hay última ubicación, solicitar actual
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    10000
                ).build()

                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.lastLocation?.let {
                            callback(LatLng(it.latitude, it.longitude))
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }

                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        locationCallback,
                        Looper.getMainLooper()
                    )
                }
            }
        }
    } catch (e: SecurityException) {
        // Permisos no concedidos
    }
}

// Calcular distancia entre dos coordenadas (fórmula Haversine)
private fun calcularDistancia(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val r = 6371.0 // Radio de la Tierra en km
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return r * c
}