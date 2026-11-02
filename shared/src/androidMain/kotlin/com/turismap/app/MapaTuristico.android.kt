package com.turismap.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import androidx.compose.foundation.isSystemInDarkTheme
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.clustering.ClusterItem
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.launch

class LugarClusterItem(val lugar: Lugar) : ClusterItem {
    override fun getPosition(): LatLng = LatLng(lugar.ubicacionCoordenadas.latitud, lugar.ubicacionCoordenadas.longitud)
    override fun getTitle(): String = lugar.nombre
    override fun getSnippet(): String = lugar.subtitulo
    override fun getZIndex(): Float? = null
}

private data class MarcadorComercio(
    val icono: ImageVector,
    val color: Color
)

private fun estiloMarcadorComercio(categoria: String): MarcadorComercio {
    val tipo = categoria.lowercase()
    return when {
        listOf("restaurante", "restaurant", "gastronom", "comida").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.Restaurant, Color(0xFFD96B52))
        listOf("cafe", "café", "cafeter").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.LocalCafe, Color(0xFF795548))
        listOf("hotel", "hostal", "hosped", "alojamiento", "cabaña", "cabana").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.Hotel, Color(0xFF3977A8))
        listOf("tienda", "artesan", "mercado", "ropa", "souvenir").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.ShoppingCart, Color(0xFF7651A8))
        else -> MarcadorComercio(Icons.Default.Store, Color(0xFFE18A3B))
    }
}

@Composable
@OptIn(MapsComposeExperimentalApi::class)
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
    var mapaCargado by remember { mutableStateOf(false) }

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

    val bounds = remember(lugares) {
        if (lugares.isEmpty()) {
            null
        } else {
            val builder = LatLngBounds.Builder()
            lugares.forEach { lugar ->
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

    LaunchedEffect(bounds, mapaCargado) {
        if (!mapaCargado) return@LaunchedEffect
        bounds?.let {
            if (lugares.size == 1) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngZoom(it.center, 13f),
                    durationMs = 500
                )
            } else {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(it, 72),
                    durationMs = 500
                )
            }
        }
    }

    val estiloOscuro = """
        [
          { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
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
    val estiloClaro = """
        [
          { "featureType": "poi", "stylers": [{ "visibility": "off" }] }
        ]
    """.trimIndent()

    val mapProperties = remember(hasPermission, esOscuro) {
        MapProperties(
            isMyLocationEnabled = hasPermission,
            mapStyleOptions = MapStyleOptions(if (esOscuro) estiloOscuro else estiloClaro)
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
        uiSettings = mapUiSettings,
        onMapLoaded = { mapaCargado = true }
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
            },
            clusterItemContent = { item ->
                val esComercio = item.lugar.id.startsWith("comercio:")
                val estiloComercio = if (esComercio) {
                    estiloMarcadorComercio(item.lugar.subtitulo)
                } else {
                    null
                }
                Box(
                    modifier = Modifier
                        .size(if (esComercio) 40.dp else 38.dp)
                        .border(2.dp, Color.White, CircleShape)
                        .background(
                            estiloComercio?.color ?: Color(0xFF1B4934),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (estiloComercio != null) {
                        Icon(
                            imageVector = estiloComercio.icono,
                            contentDescription = item.lugar.subtitulo,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    } else {
                        Text(
                            text = "D",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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
