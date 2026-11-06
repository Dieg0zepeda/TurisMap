package com.turismap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
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
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
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
    val color: Color,
    val semanticaCategoria: String
)

private fun estiloMarcadorComercio(categoria: String): MarcadorComercio {
    val tipo = categoria.lowercase()
    return when {
        listOf("restaurante", "restaurant", "gastronom", "comida").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.Restaurant, Color(0xFFD96B52), "Restaurante")
        listOf("cafe", "café", "cafeter").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.LocalCafe, Color(0xFF795548), "Cafetería")
        listOf("hotel", "hostal", "hosped", "alojamiento", "cabaña", "cabana").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.Hotel, Color(0xFF3977A8), "Alojamiento")
        listOf("tienda", "artesan", "mercado", "ropa", "souvenir").any(tipo::contains) ->
            MarcadorComercio(Icons.Default.ShoppingCart, Color(0xFF7651A8), "Tienda o Mercado")
        else -> MarcadorComercio(Icons.Default.Store, Color(0xFFE18A3B), "Comercio")
    }
}

@Composable
@OptIn(MapsComposeExperimentalApi::class)
actual fun MapaTuristico(
    lugares: List<Lugar>,
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier
) {
    val centroRuta = remember(lugares) {
        if (lugares.isNotEmpty()) {
            val avgLat = lugares.map { it.ubicacionCoordenadas.latitud }.average()
            val avgLng = lugares.map { it.ubicacionCoordenadas.longitud }.average()
            LatLng(avgLat, avgLng)
        } else {
            LatLng(13.8697, -89.8486)
        }
    }

    val trayectoRutaFlores = remember {
        listOf(
            LatLng(13.9214, -89.8450),
            LatLng(13.8703, -89.8486),
            LatLng(13.8569, -89.8028),
            LatLng(13.8417, -89.7456),
            LatLng(13.8306, -89.7547),
            LatLng(13.7778, -89.7361)
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current

    val esOscuro = isSystemInDarkTheme()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var shouldRequestPermission by remember { mutableStateOf(false) }
    var mapaCargado by remember { mutableStateOf(false) }

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

    if (shouldRequestPermission) {
        userLocation = centroRuta
        hasPermission = false
        shouldRequestPermission = false
    }

    // AJUSTE 1: Aterrizaje Táctico (Inicia más cerca, zoom 11.5f en lugar de 6f)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(centroRuta)
            .zoom(11.5f)
            .tilt(0f)
            .build()
    }

    LaunchedEffect(mapaCargado) {
        if (mapaCargado) {
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.Builder()
                        .target(userLocation ?: centroRuta)
                        .zoom(15f) // Nivel de acercamiento óptimo sin exagerar
                        .tilt(60f)
                        .build()
                ),
                durationMs = 1200 // Animación rápida y ágil
            )
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

    val limitesElSalvador = remember {
        LatLngBounds(
            LatLng(13.1000, -90.1500),
            LatLng(14.5000, -87.6500)
        )
    }

    val mapProperties = remember(hasPermission, esOscuro) {
        MapProperties(
            isMyLocationEnabled = hasPermission,
            mapStyleOptions = MapStyleOptions(if (esOscuro) estiloOscuro else estiloClaro),
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            latLngBoundsForCameraTarget = limitesElSalvador,
            minZoomPreference = 11f // AJUSTE 2: Impide que el usuario se aleje y se pierda
        )
    }

    val mapUiSettings = remember(hasPermission) {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = hasPermission,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            mapToolbarEnabled = false // AJUSTE 3: Borra los botones estorbo de Google Maps
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
        Polyline(
            points = trayectoRutaFlores,
            color = Color(0xFFD96B52).copy(alpha = 0.6f),
            width = 16f
        )

        trayectoRutaFlores.forEach { puebloCoords ->
            Circle(
                center = puebloCoords,
                radius = 1200.0,
                fillColor = Color(0xFF1B4934).copy(alpha = 0.15f),
                strokeColor = Color(0xFF1B4934).copy(alpha = 0.5f),
                strokeWidth = 3f
            )
        }

        Clustering(
            items = clusterItems,
            onClusterItemClick = { item ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)

                onLugarClick(item.lugar)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newCameraPosition(
                            CameraPosition.Builder()
                                .target(item.position)
                                .zoom(17.5f)
                                .tilt(60f)
                                .bearing(cameraPositionState.position.bearing)
                                .build()
                        ),
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

                val descripcionAccesible = if (esComercio) {
                    "${estiloComercio?.semanticaCategoria ?: "Comercio"}: ${item.lugar.nombre}. Toca para abrir información."
                } else {
                    "Destino turístico: ${item.lugar.nombre}. Toca para explorar."
                }

                val infiniteTransition = rememberInfiniteTransition(label = "animacion_pin")

                val ondaScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.8f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "scale"
                )
                val ondaAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1500),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "alpha"
                )

                val levitacionY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = -10f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "levitacion"
                )

                val sombraScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0.6f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "sombra"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { contentDescription = descripcionAccesible }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .offset(y = levitacionY.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (esComercio) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .scale(ondaScale)
                                        .alpha(ondaAlpha)
                                        .background(estiloComercio?.color ?: Color(0xFF1B4934), CircleShape)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .shadow(6.dp, CircleShape)
                                    .border(3.dp, Color.White, CircleShape)
                                    .background(
                                        estiloComercio?.color ?: Color(0xFF1B4934),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (estiloComercio != null) {
                                    Icon(
                                        imageVector = estiloComercio.icono,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
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

                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(14.dp)
                                .background(Color.White)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 16.dp, height = 6.dp)
                            .scale(sombraScale)
                            .background(
                                Color.Black.copy(alpha = 0.6f),
                                RoundedCornerShape(50)
                            )
                    )
                }
            }
        )
    }
}

@SuppressLint("MissingPermission")
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
        // Manejo silencioso
    }
}