package com.turismap.app

import android.Manifest
import android.view.HapticFeedbackConstants
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.turismap.app.screens.mapa.MarcadorMapa
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Volatile
private var permisoUbicacionSolicitadoEnSesion: Boolean = false

@Composable
@OptIn(MapsComposeExperimentalApi::class)
actual fun MapaTuristico(
    marcadores: List<MarcadorMapa>,
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?,
    camaraGuardada: EstadoCamaraMapa?,
    onCamaraCambiada: (EstadoCamaraMapa) -> Unit,
    onMarcadorClick: (MarcadorMapa) -> Unit,
    modifier: Modifier
) {
    val centroRuta = remember(marcadores) {
        if (marcadores.isNotEmpty()) {
            var sumaLat = 0.0
            var sumaLng = 0.0
            marcadores.forEach { marcador ->
                sumaLat += marcador.coordenadas.latitud
                sumaLng += marcador.coordenadas.longitud
            }
            LatLng(sumaLat / marcadores.size, sumaLng / marcadores.size)
        } else {
            CENTRO_RUTA_PREDETERMINADO
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val onCamaraCambiadaActual by rememberUpdatedState(onCamaraCambiada)

    val esOscuro = isSystemInDarkTheme()
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    var hasPermission by remember(context) { mutableStateOf(tienePermisoUbicacion(context)) }
    var mapaCargado by remember { mutableStateOf(false) }

    val tieneObjetivoInicial = latInicial != null && lngInicial != null
    var centradoInicialRealizado by remember {
        mutableStateOf(camaraGuardada != null || tieneObjetivoInicial)
    }
    var ultimoObjetivoAnimado by remember {
        mutableStateOf(
            if (latInicial != null && lngInicial != null) {
                Triple(latInicial, lngInicial, zoomInicial)
            } else {
                null
            }
        )
    }

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

    LaunchedEffect(hasPermission) {
        if (!hasPermission && !permisoUbicacionSolicitadoEnSesion) {
            permisoUbicacionSolicitadoEnSesion = true
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

    // Restaura la última cámara de sesión o posiciona directamente en coordenadas de destino sin saltos
    val posicionCamaraInicial = remember {
        when {
            latInicial != null && lngInicial != null -> CameraPosition.Builder()
                .target(LatLng(latInicial, lngInicial))
                .zoom(zoomInicial ?: 15.5f)
                .tilt(45f)
                .build()
            camaraGuardada != null -> CameraPosition.Builder()
                .target(LatLng(camaraGuardada.latitud, camaraGuardada.longitud))
                .zoom(camaraGuardada.zoom)
                .tilt(camaraGuardada.tilt)
                .bearing(camaraGuardada.bearing)
                .build()
            else -> CameraPosition.Builder()
                .target(centroRuta)
                .zoom(11.5f)
                .tilt(0f)
                .build()
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = posicionCamaraInicial
    }

    // Guarda automáticamente la cámara cuando el usuario termina de mover el mapa
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.isMoving }
            .distinctUntilChanged()
            .filter { moviendose -> !moviendose }
            .collect {
                if (mapaCargado || centradoInicialRealizado) {
                    val pos = cameraPositionState.position
                    onCamaraCambiadaActual(pos.aEstadoCamaraMapa())
                }
            }
    }

    DisposableEffect(cameraPositionState) {
        onDispose {
            if (mapaCargado || centradoInicialRealizado) {
                onCamaraCambiadaActual(cameraPositionState.position.aEstadoCamaraMapa())
            }
        }
    }

    // Anima solo ante un nuevo objetivo explícito (búsqueda/ruta) o el primer centrado de la sesión
    LaunchedEffect(mapaCargado, latInicial, lngInicial, zoomInicial) {
        if (!mapaCargado) return@LaunchedEffect

        if (latInicial != null && lngInicial != null) {
            val objetivo = Triple(latInicial, lngInicial, zoomInicial)
            if (objetivo != ultimoObjetivoAnimado) {
                ultimoObjetivoAnimado = objetivo
                centradoInicialRealizado = true
                cameraPositionState.animate(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition.Builder()
                            .target(LatLng(latInicial, lngInicial))
                            .zoom(zoomInicial ?: 15.5f)
                            .tilt(45f)
                            .build()
                    ),
                    durationMs = 650
                )
            }
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
                durationMs = 900
            )
            onCamaraCambiadaActual(cameraPositionState.position.aEstadoCamaraMapa())
        }
    }

    val mapStyleOptions = remember(esOscuro) {
        MapStyleOptions(if (esOscuro) ESTILO_MAPA_OSCURO else ESTILO_MAPA_CLARO)
    }

    val mapProperties = remember(hasPermission, mapStyleOptions) {
        MapProperties(
            isMyLocationEnabled = hasPermission,
            mapStyleOptions = mapStyleOptions,
            isBuildingEnabled = false,
            isIndoorEnabled = false,
            latLngBoundsForCameraTarget = LIMITES_EL_SALVADOR,
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

    val clusterItems = remember(marcadores) { marcadores.map(::MarcadorClusterItem) }

    GoogleMap(
        modifier = modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        properties = mapProperties,
        uiSettings = mapUiSettings,
        contentPadding = PaddingValues(top = 72.dp),
        onMapLoaded = { mapaCargado = true }
    ) {
        Polyline(
            points = TRAYECTO_RUTA_FLORES,
            color = Color(0xFFD96B52).copy(alpha = 0.6f),
            width = 16f
        )

        TRAYECTO_RUTA_FLORES.forEach { puebloCoords ->
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
                            durationMs = 450
                        )
                    }
                    if (posicionesDistintas.size > 1 && cameraPositionState.position.zoom < 17f) {
                        val limitesBuilder = LatLngBounds.Builder()
                        posicionesDistintas.forEach { limitesBuilder.include(it) }
                        try {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngBounds(limitesBuilder.build(), 140),
                                durationMs = 450
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
                onMarcadorClick(item.marcador)
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
                        durationMs = 450
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
                ContenidoMarcadorMapa(item = item)
            }
        )
    }
}
