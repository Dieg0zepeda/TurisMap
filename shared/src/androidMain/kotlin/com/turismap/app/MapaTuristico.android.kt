package com.turismap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

data class LugarClusterItem(val lugar: Lugar) : ClusterItem {
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
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?,
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
    val lifecycleOwner = LocalLifecycleOwner.current

    val esOscuro = isSystemInDarkTheme()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember(context) { mutableStateOf(tienePermisoUbicacion(context)) }
    var permisoSolicitado by remember { mutableStateOf(false) }
    var mapaCargado by remember { mutableStateOf(false) }
    var centradoInicialRealizado by remember { mutableStateOf(false) }

    val launcherPermisoUbicacion = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { resultados ->
        hasPermission = resultados[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            resultados[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            tienePermisoUbicacion(context)
    }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = tienePermisoUbicacion(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(hasPermission, permisoSolicitado) {
        if (!hasPermission && !permisoSolicitado) {
            permisoSolicitado = true
            launcherPermisoUbicacion.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(context, hasPermission) {
        if (!hasPermission) {
            userLocation = null
            onDispose { }
        } else {
            val detenerUbicacion = iniciarActualizacionUbicacion(context) { location ->
                userLocation = location
            }
            onDispose {
                detenerUbicacion()
            }
        }
    }

    // Posición inicial: si vienen coordenadas específicas se fija en ellas
    val posicionInicio = remember(latInicial, lngInicial, centroRuta) {
        if (latInicial != null && lngInicial != null) {
            LatLng(latInicial, lngInicial)
        } else {
            centroRuta
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.Builder()
            .target(posicionInicio)
            .zoom(if (latInicial != null) (zoomInicial ?: 15.5f) else 11.5f)
            .tilt(0f)
            .build()
    }

    // Animación de cámara prioritaria: viaja a coordenadas recibidas (ruta o búsqueda)
    // y realiza el centrado inicial hacia el usuario/ruta una sola vez.
    LaunchedEffect(mapaCargado, latInicial, lngInicial, zoomInicial) {
        if (mapaCargado) {
            if (latInicial != null && lngInicial != null) {
                centradoInicialRealizado = true
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(latInicial, lngInicial))
                            .zoom(zoomInicial ?: 15.5f)
                            .tilt(45f)
                            .build()
                    ),
                    durationMs = 900
                )
            } else if (!centradoInicialRealizado) {
                centradoInicialRealizado = true
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(userLocation ?: centroRuta)
                            .zoom(15f)
                            .tilt(60f)
                            .build()
                    ),
                    durationMs = 1200
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
            minZoomPreference = 11f
        )
    }

    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = true,
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = true,
            mapToolbarEnabled = false
        )
    }

    val clusterItems = remember(lugares) { lugares.map { LugarClusterItem(it) } }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        contentPadding = PaddingValues(top = 72.dp),
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
            onClusterClick = { cluster ->
                view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                coroutineScope.launch {
                    val posicionesDistintas = cluster.items.map { it.position }.distinct()
                    val animarPorZoom: suspend () -> Unit = {
                        val nuevoZoom = (cameraPositionState.position.zoom + 2.2f).coerceAtMost(19f)
                        cameraPositionState.animate(
                            CameraUpdateFactory.newCameraPosition(
                                CameraPosition.Builder()
                                    .target(cluster.position)
                                    .zoom(nuevoZoom)
                                    .tilt(cameraPositionState.position.tilt)
                                    .bearing(cameraPositionState.position.bearing)
                                    .build()
                            ),
                            durationMs = 500
                        )
                    }
                    if (posicionesDistintas.size > 1 && cameraPositionState.position.zoom < 17f) {
                        val limitesBuilder = LatLngBounds.Builder()
                        posicionesDistintas.forEach { limitesBuilder.include(it) }
                        try {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(limitesBuilder.build(), 140),
                                durationMs = 500
                            )
                        } catch (error: CancellationException) {
                            throw error
                        } catch (_: IllegalStateException) {
                            animarPorZoom()
                        }
                    } else {
                        animarPorZoom()
                    }
                }
                true
            },
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
            clusterContent = { cluster ->
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .shadow(6.dp, CircleShape)
                        .border(3.dp, Color.White, CircleShape)
                        .background(Color(0xFF1B4934), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cluster.size.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
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

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.semantics { contentDescription = descripcionAccesible }
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    ) {
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
                            .background(
                                Color.Black.copy(alpha = 0.45f),
                                RoundedCornerShape(50)
                            )
                    )
                }
            }
        )
    }
}

private fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
private fun iniciarActualizacionUbicacion(
    context: Context,
    callback: (LatLng) -> Unit
): () -> Unit {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    var activo = true
    var locationCallback: LocationCallback? = null

    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (!activo) return@addOnSuccessListener
            if (location != null) {
                callback(LatLng(location.latitude, location.longitude))
            } else if (tienePermisoUbicacion(context)) {
                val locationRequest = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    10_000L
                ).build()

                val activeCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        if (!activo) return
                        locationResult.lastLocation?.let {
                            callback(LatLng(it.latitude, it.longitude))
                        }
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
                locationCallback = activeCallback

                try {
                    fusedLocationClient.requestLocationUpdates(
                        locationRequest,
                        activeCallback,
                        Looper.getMainLooper()
                    )
                } catch (_: SecurityException) {
                    // Manejo silencioso ante revocación de permiso
                }
            }
        }
    } catch (_: SecurityException) {
        // Manejo silencioso ante revocación de permiso
    }

    return {
        activo = false
        locationCallback?.let { activeCallback ->
            fusedLocationClient.removeLocationUpdates(activeCallback)
        }
    }
}