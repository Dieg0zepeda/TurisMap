package com.turismap.app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterHdr
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterItem
import com.turismap.app.data.models.CategoriaAtractivoPublico
import com.turismap.app.screens.mapa.MarcadorMapa

internal val CENTRO_RUTA_PREDETERMINADO = LatLng(13.8697, -89.8486)

internal val TRAYECTO_RUTA_FLORES = listOf(
    LatLng(13.9214, -89.8450),
    LatLng(13.8703, -89.8486),
    LatLng(13.8569, -89.8028),
    LatLng(13.8417, -89.7456),
    LatLng(13.8306, -89.7547),
    LatLng(13.7778, -89.7361)
)

internal val LIMITES_EL_SALVADOR = LatLngBounds(
    LatLng(13.1000, -90.1500),
    LatLng(14.5000, -87.6500)
)

internal const val ESTILO_MAPA_OSCURO = """
[
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] },
  { "elementType": "geometry", "stylers": [{ "color": "#242f3e" }] },
  { "elementType": "labels.text.fill", "stylers": [{ "color": "#746855" }] },
  { "elementType": "labels.text.stroke", "stylers": [{ "color": "#242f3e" }] },
  { "featureType": "administrative.locality", "stylers": [{ "color": "#d59563" }] },
  { "featureType": "road", "elementType": "geometry", "stylers": [{ "color": "#38414e" }] },
  { "featureType": "road", "elementType": "geometry.stroke", "stylers": [{ "color": "#212a37" }] },
  { "featureType": "road", "elementType": "labels.text.fill", "stylers": [{ "color": "#9ca5b3" }] },
  { "featureType": "water", "elementType": "geometry", "stylers": [{ "color": "#17263c" }] }
]
"""

internal const val ESTILO_MAPA_CLARO = """
[
  { "featureType": "poi", "stylers": [{ "visibility": "off" }] }
]
"""

internal data class EstiloPinMapa(
    val icono: ImageVector,
    val color: Color,
    val semanticaCategoria: String
)

private val PALABRAS_RESTAURANTE = listOf("restaurante", "restaurant", "gastronom", "comida")
private val PALABRAS_CAFE = listOf("cafe", "café", "cafeter")
private val PALABRAS_HOTEL = listOf("hotel", "hostal", "hosped", "alojamiento", "cabaña", "cabana")
private val PALABRAS_TIENDA = listOf("tienda", "artesan", "mercado", "ropa", "souvenir")

private val MARCADOR_RESTAURANTE = EstiloPinMapa(Icons.Default.Restaurant, Color(0xFFD96B52), "Restaurante")
private val MARCADOR_CAFE = EstiloPinMapa(Icons.Default.LocalCafe, Color(0xFF795548), "Cafetería")
private val MARCADOR_HOTEL = EstiloPinMapa(Icons.Default.Hotel, Color(0xFF3977A8), "Alojamiento")
private val MARCADOR_TIENDA = EstiloPinMapa(Icons.Default.ShoppingCart, Color(0xFF7651A8), "Tienda o Mercado")
private val MARCADOR_GENERICO = EstiloPinMapa(Icons.Default.Store, Color(0xFFE18A3B), "Comercio")

private val MARCADOR_MONTANA = EstiloPinMapa(Icons.Default.Terrain, Color(0xFF2D6A4F), "Montaña")
private val MARCADOR_MIRADOR = EstiloPinMapa(Icons.Default.Visibility, Color(0xFFC88A1E), "Mirador")
private val MARCADOR_LAGUNA = EstiloPinMapa(Icons.Default.Water, Color(0xFF0E7490), "Laguna")
private val MARCADOR_RIO = EstiloPinMapa(Icons.Default.Waves, Color(0xFF0284C7), "Río")
private val MARCADOR_CERRO = EstiloPinMapa(Icons.Default.FilterHdr, Color(0xFF9A5B25), "Cerro")

internal fun estiloMarcadorComercio(categoria: String): EstiloPinMapa {
    val tipo = categoria.lowercase()
    return when {
        PALABRAS_RESTAURANTE.any(tipo::contains) -> MARCADOR_RESTAURANTE
        PALABRAS_CAFE.any(tipo::contains) -> MARCADOR_CAFE
        PALABRAS_HOTEL.any(tipo::contains) -> MARCADOR_HOTEL
        PALABRAS_TIENDA.any(tipo::contains) -> MARCADOR_TIENDA
        else -> MARCADOR_GENERICO
    }
}

internal fun estiloMarcadorAtractivo(categoria: CategoriaAtractivoPublico): EstiloPinMapa =
    when (categoria) {
        CategoriaAtractivoPublico.MONTANA -> MARCADOR_MONTANA
        CategoriaAtractivoPublico.MIRADOR -> MARCADOR_MIRADOR
        CategoriaAtractivoPublico.LAGUNA -> MARCADOR_LAGUNA
        CategoriaAtractivoPublico.RIO -> MARCADOR_RIO
        CategoriaAtractivoPublico.CERRO -> MARCADOR_CERRO
    }

data class MarcadorClusterItem(val marcador: MarcadorMapa) : ClusterItem {
    private val posicionCalculada: LatLng = LatLng(
        marcador.coordenadas.latitud,
        marcador.coordenadas.longitud
    )
    internal val estiloPin: EstiloPinMapa? = when (marcador) {
        is MarcadorMapa.Destino -> null
        is MarcadorMapa.Comercio -> estiloMarcadorComercio(marcador.etiquetaCategoria)
        is MarcadorMapa.AtractivoPublico -> estiloMarcadorAtractivo(marcador.categoria)
    }

    override fun getPosition(): LatLng = posicionCalculada
    override fun getTitle(): String = marcador.titulo
    override fun getSnippet(): String = marcador.subtitulo
    override fun getZIndex(): Float? = null
}

@Composable
internal fun ContenidoMarcadorMapa(item: MarcadorClusterItem) {
    val estiloPin = item.estiloPin
    val descripcionAccesible = when (val marcador = item.marcador) {
        is MarcadorMapa.Comercio ->
            "${estiloPin?.semanticaCategoria ?: "Comercio"}: ${marcador.titulo}. Toca para abrir información."
        is MarcadorMapa.AtractivoPublico ->
            "${estiloPin?.semanticaCategoria ?: "Atractivo natural"}: ${marcador.titulo}. Toca para ver información."
        is MarcadorMapa.Destino ->
            "Destino turístico: ${marcador.titulo}. Toca para explorar."
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
                        estiloPin?.color ?: Color(0xFF1B4934),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (estiloPin != null) {
                    Icon(
                        imageVector = estiloPin.icono,
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

internal fun CameraPosition.aEstadoCamaraMapa(): EstadoCamaraMapa = EstadoCamaraMapa(
    latitud = target.latitude,
    longitud = target.longitude,
    zoom = zoom,
    tilt = tilt,
    bearing = bearing
)

internal fun tienePermisoUbicacion(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

@SuppressLint("MissingPermission")
internal fun iniciarActualizacionUbicacion(
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
