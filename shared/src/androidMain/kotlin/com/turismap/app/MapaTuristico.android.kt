package com.turismap.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
actual fun MapaTuristico(
    lugares: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier
) {
    val ataco = LatLng(13.8697, -89.8486)
    val coroutineScope = rememberCoroutineScope()
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

    // Ubicación por defecto si no hay permisos
    if (shouldRequestPermission) {
        userLocation = ataco
        hasPermission = false
        shouldRequestPermission = false
    }

    // Calcular lugares más cercanos (2-3)
    val lugaresCercanos = remember(userLocation, lugares) {
        if (userLocation == null || lugares.isEmpty()) {
            lugares
        } else {
            val loc = userLocation!!
            lugares.map { lugar ->
                val distancia = calcularDistancia(
                    loc.latitude,
                    loc.longitude,
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
            userLocation ?: ataco,
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

    val estiloVerdeNaturaleza = """
        [
          {
            "stylers": [
              { "hue": "#bbff00" },
              { "weight": 0.5 },
              { "gamma": 0.5 }
            ]
          },
          {
            "elementType": "labels",
            "stylers": [
              { "visibility": "off" }
            ]
          },
          {
            "featureType": "landscape.natural",
            "stylers": [
              { "color": "#a4cc48" }
            ]
          },
          {
            "featureType": "road",
            "elementType": "geometry",
            "stylers": [
              { "color": "#ffffff" },
              { "visibility": "on" },
              { "weight": 1 }
            ]
          },
          {
            "featureType": "administrative",
            "elementType": "labels",
            "stylers": [
              { "visibility": "on" }
            ]
          },
          {
            "featureType": "road.highway",
            "elementType": "labels",
            "stylers": [
              { "visibility": "simplified" },
              { "gamma": 1.14 },
              { "saturation": -18 }
            ]
          },
          {
            "featureType": "road.highway.controlled_access",
            "elementType": "labels",
            "stylers": [
              { "saturation": 30 },
              { "gamma": 0.76 }
            ]
          },
          {
            "featureType": "road.local",
            "stylers": [
              { "visibility": "simplified" },
              { "weight": 0.4 },
              { "lightness": -8 }
            ]
          },
          {
            "featureType": "water",
            "stylers": [
              { "color": "#4aaecc" }
            ]
          },
          {
            "featureType": "landscape.man_made",
            "stylers": [
              { "color": "#718e32" }
            ]
          },
          {
            "featureType": "poi.business",
            "stylers": [
              { "saturation": 68 },
              { "lightness": -61 }
            ]
          },
          {
            "featureType": "administrative.locality",
            "elementType": "labels.text.stroke",
            "stylers": [
              { "weight": 2.7 },
              { "color": "#f4f9e8" }
            ]
          },
          {
            "featureType": "road.highway.controlled_access",
            "elementType": "geometry.stroke",
            "stylers": [
              { "weight": 1.5 },
              { "color": "#e53013" },
              { "saturation": -42 },
              { "lightness": 28 }
            ]
          }
        ]
    """.trimIndent()

    val mapProperties = remember(hasPermission) {
        MapProperties(
            isMyLocationEnabled = hasPermission,
            mapStyleOptions = MapStyleOptions(estiloVerdeNaturaleza)
        )
    }

    val mapUiSettings = remember(hasPermission) {
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
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
                onClick = {
                    onLugarClick(lugar)
                    coroutineScope.launch {
                        cameraPositionState.animate(
                            update = CameraUpdateFactory.newLatLngZoom(
                                LatLng(
                                    lugar.ubicacionCoordenadas.latitud,
                                    lugar.ubicacionCoordenadas.longitud
                                ),
                                15f
                            ),
                            durationMs = 500
                        )
                    }
                    true
                }
            )
        }
    }
}

private fun obtenerUbicacionActual(context: Context, callback: (LatLng) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                callback(LatLng(location.latitude, location.longitude))
            } else {
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
        // Manejo silencioso de permisos no concedidos
    }
}

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
    val c = 2 * atan2(sqrt(a), sqrt(1.0 - a))
    return r * c
}