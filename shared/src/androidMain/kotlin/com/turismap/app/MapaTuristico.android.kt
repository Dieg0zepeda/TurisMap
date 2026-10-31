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
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class LugarClusterItem(val lugar: Lugar) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lugar.ubicacionCoordenadas.latitud, lugar.ubicacionCoordenadas.longitud)
    override fun getTitle(): String = lugar.nombre
    override fun getSnippet(): String = lugar.subtitulo
    override fun getZIndex(): Float? = null
}

@Composable
actual fun MapaTuristico(
    lugares: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier
) {
    // Centroide dinámico calculado 100% desde las coordenadas de la Base de Datos
    val centroRuta = remember(lugares) {
        if (lugares.isNotEmpty()) {
            val avgLat = lugares.map { it.ubicacionCoordenadas.latitud }.average()
            val avgLng = lugares.map { it.ubicacionCoordenadas.longitud }.average()
            LatLng(avgLat, avgLng)
        } else {
            LatLng(13.8697, -89.8486) // Fallback inicial de milisegundos mientras responde Firebase
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val esOscuro = isSystemInDarkTheme()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var shouldRequestPermission by remember { mutableStateOf(false) }
    var lugaresCercanos by remember { mutableStateOf<List<Lugar>>(emptyList()) }

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

    // Ubicación por defecto basada en la BD si no hay permisos
    if (shouldRequestPermission) {
        userLocation = centroRuta
        hasPermission = false
        shouldRequestPermission = false
    }

    // Calcular lugares más cercanos en hilo de fondo para no bloquear UI
    LaunchedEffect(userLocation, lugares) {
        if (userLocation != null && lugares.isNotEmpty()) {
            withContext(Dispatchers.Default) {
                val loc = userLocation!!
                val cercanos = lugares.map { lugar ->
                    val distancia = calcularDistancia(
                        loc.latitude,
                        loc.longitude,
                        lugar.ubicacionCoordenadas.latitud,
                        lugar.ubicacionCoordenadas.longitud
                    )
                    lugar to distancia
                }.sortedBy { it.second }.take(3).map { it.first }
                lugaresCercanos = cercanos
            }
        } else if (lugares.isNotEmpty()) {
            lugaresCercanos = lugares
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
            userLocation ?: centroRuta,
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

    val estiloOscuro = """
        [
          { "elementType": "geometry", "stylers": [{ "color": "#242f3e" }] },
          { "elementType": "labels.text.fill", "stylers": [{ "color": "#746855" }] },
          { "elementType": "labels.text.stroke", "stylers": [{ "color": "#242f3e" }] },
          { "featureType": "administrative.locality", "elementType": "labels.text.fill", "stylers": [{ "color": "#d59563" }] },
          { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#38414e" }] },
          { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#212a37" }] },
          { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#9ca5b3" }] },
          { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#17263c" }] }
        ]
    """.trimIndent()

    val mapProperties = remember(hasPermission, esOscuro) {
        MapProperties(
            isMyLocationEnabled = hasPermission,
            mapStyleOptions = if (esOscuro) MapStyleOptions(estiloOscuro) else null
        )
    }

    val mapUiSettings = remember(hasPermission) {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = hasPermission
        )
    }

    val clusterItems = remember(lugares) { lugares.map { LugarClusterItem(it) } }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings
    ) {
        Clustering(
            items = clusterItems,
            onClusterItemClick = { item ->
                onLugarClick(item.lugar)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        update = CameraUpdateFactory.newLatLngZoom(item.position, 15f),
                        durationMs = 500
                    )
                }
                true
            }
        )
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