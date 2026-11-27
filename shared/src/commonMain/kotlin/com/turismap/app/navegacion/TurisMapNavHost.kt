package com.turismap.app.navegacion

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turismap.app.data.CoordenadasRuta
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.data.repository.esCoordenadaValida
import com.turismap.app.screens.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.btn_reintentar
import turismap.shared.generated.resources.nav_error_actualizar_favorito
import turismap.shared.generated.resources.nav_error_cargar_lugar
import turismap.shared.generated.resources.nav_error_guardar_visita
import turismap.shared.generated.resources.nav_lugar_no_encontrado

private val RUTAS_PESTANAS_PRINCIPALES = listOf(
    Pantalla.Explorar.ruta,
    Pantalla.Mapa.ruta,
    Pantalla.Guia.ruta,
    Pantalla.Perfil.ruta
)

private fun esRutaPestanaPrincipal(ruta: String?): Boolean =
    ruta != null && RUTAS_PESTANAS_PRINCIPALES.any { base -> ruta.startsWith(base) }

@Composable
fun TurisMapNavHost(
    controladorNavegacion: NavHostController = rememberNavController(),
    modificador: Modifier = Modifier,
    cantidadNotificacionesNoLeidas: Int = 0,
    onCerrarSesion: () -> Unit = {},
    destinoInicial: String = Pantalla.Explorar.ruta
) {
    val abrirNotificaciones = {
        controladorNavegacion.navigate(Pantalla.Notificaciones.ruta) {
            launchSingleTop = true
        }
    }

    NavHost(
        navController = controladorNavegacion,
        startDestination = destinoInicial,
        modifier = modificador,
        enterTransition = {
            if (esRutaPestanaPrincipal(initialState.destination.route) &&
                esRutaPestanaPrincipal(targetState.destination.route)
            ) {
                fadeIn(animationSpec = tween(durationMillis = 120))
            } else {
                fadeIn(animationSpec = tween(durationMillis = 190)) +
                    slideInHorizontally(animationSpec = tween(durationMillis = 200)) { it / 12 }
            }
        },
        exitTransition = {
            if (esRutaPestanaPrincipal(initialState.destination.route) &&
                esRutaPestanaPrincipal(targetState.destination.route)
            ) {
                fadeOut(animationSpec = tween(durationMillis = 120))
            } else {
                fadeOut(animationSpec = tween(durationMillis = 160))
            }
        },
        popEnterTransition = {
            if (esRutaPestanaPrincipal(initialState.destination.route) &&
                esRutaPestanaPrincipal(targetState.destination.route)
            ) {
                fadeIn(animationSpec = tween(durationMillis = 120))
            } else {
                fadeIn(animationSpec = tween(durationMillis = 180))
            }
        },
        popExitTransition = {
            if (esRutaPestanaPrincipal(initialState.destination.route) &&
                esRutaPestanaPrincipal(targetState.destination.route)
            ) {
                fadeOut(animationSpec = tween(durationMillis = 120))
            } else {
                fadeOut(animationSpec = tween(durationMillis = 180)) +
                    slideOutHorizontally(animationSpec = tween(durationMillis = 180)) { it / 12 }
            }
        }
    ) {
        // 1. Explorar
        composable(Pantalla.Explorar.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                ExplorarScreen(
                    cantidadNotificacionesNoLeidas = cantidadNotificacionesNoLeidas,
                    onAbrirNotificaciones = abrirNotificaciones,
                    onLugarClick = { lugar ->
                        controladorNavegacion.navigate(Pantalla.DetalleLugar.crearRuta(lugar.id))
                    },
                    onComercioClick = { comercioId ->
                        controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                    }
                )
            }
        }

        // 2. Mapa con soporte de centrado en destino
        composable(
            route = Pantalla.Mapa.rutaPatron,
            arguments = listOf(
                navArgument("lat") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("lng") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("zoom") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val lat = backStackEntry.arguments?.getString("lat")?.toDoubleOrNull()
            val lng = backStackEntry.arguments?.getString("lng")?.toDoubleOrNull()
            val zoom = backStackEntry.arguments?.getString("zoom")?.toFloatOrNull()

            Box(modifier = Modifier.fillMaxSize()) {
                MapaScreen(
                    latInicial = lat,
                    lngInicial = lng,
                    zoomInicial = zoom,
                    onAbrirDetalle = { lugar ->
                        controladorNavegacion.navigate(Pantalla.DetalleLugar.crearRuta(lugar.id))
                    },
                    onComercioClick = { comercioId ->
                        controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                    }
                )
            }
        }

        // 3. Guía Comercial
        composable(route = Pantalla.Guia.ruta) {
            GuiaScreen(
                onComercioClick = { comercioId ->
                    controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                }
            )
        }

        // 4. Perfil
        composable(Pantalla.Perfil.ruta) {
            PerfilScreen(
                cantidadNotificacionesNoLeidas = cantidadNotificacionesNoLeidas,
                onAbrirNotificaciones = abrirNotificaciones,
                onCerrarSesionClick = onCerrarSesion,
                onVerPlanesClick = {
                    controladorNavegacion.navigate(Pantalla.PlanesComercio.ruta)
                },
                onComercioClick = { comercioId ->
                    controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                },
                onLugarClick = { lugarId ->
                    controladorNavegacion.navigate(Pantalla.DetalleLugar.crearRuta(lugarId))
                }
            )
        }

        // Notificaciones in-app
        composable(Pantalla.Notificaciones.ruta) {
            NotificacionesScreen(
                onVolver = { controladorNavegacion.popBackStack() },
                onVerComercio = { comercioId ->
                    controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                },
                onVerPlanes = {
                    controladorNavegacion.navigate(Pantalla.PlanesComercio.ruta)
                }
            )
        }

        // Planes de Comercio
        composable(Pantalla.PlanesComercio.ruta) {
            PlanesComercioScreen(
                onBack = { controladorNavegacion.popBackStack() },
                onSolicitarPlan = { plan ->
                    controladorNavegacion.navigate(Pantalla.RegistroComercio.crearRuta(plan.documentId))
                }
            )
        }

        // Solicitud de Registro de Comercio
        composable(
            route = Pantalla.RegistroComercio.ruta,
            arguments = listOf(navArgument("planId") { type = NavType.StringType })
        ) { backStackEntry ->
            val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
            ComercioSolicitudScreen(
                nombreCompletoInicial = AutenticacionRepository.usuarioActual?.displayName.orEmpty(),
                emailInicial = AutenticacionRepository.usuarioActual?.email.orEmpty(),
                planId = planId,
                onBack = { controladorNavegacion.popBackStack() },
                onSubmitted = {
                    controladorNavegacion.popBackStack(Pantalla.PlanesComercio.ruta, inclusive = false)
                }
            )
        }

        // 5. Detalle de Lugar
        composable(
            route = Pantalla.DetalleLugar.ruta,
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: return@composable

            val lugarEnCache = remember(lugarId) { LugaresRepository.obtenerEnCachePorId(lugarId) }
            var lugarCompleto by remember(lugarId) { mutableStateOf(lugarEnCache) }
            var cargando by remember(lugarId) { mutableStateOf(lugarEnCache == null) }
            var errorCarga by remember { mutableStateOf<String?>(null) }
            var intentoCarga by remember { mutableStateOf(0) }
            var favoritoCargando by remember { mutableStateOf(false) }
            var mensajeAccion by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            val usuarioActual by UsuarioRepository.usuarioActualFlow.collectAsState()
            val favoritoGuardado = remember(usuarioActual?.favoritos, lugarId) {
                usuarioActual?.favoritos?.contains(lugarId) == true
            }

            LaunchedEffect(lugarId, intentoCarga) {
                val esReintentoManual = intentoCarga > 0
                if (lugarCompleto == null || esReintentoManual) {
                    cargando = true
                }
                errorCarga = null
                mensajeAccion = null
                try {
                    val lugarResuelto = LugaresRepository.obtenerPorId(
                        id = lugarId,
                        forzarRecarga = esReintentoManual
                    )
                    lugarCompleto = lugarResuelto
                    if (lugarResuelto != null) {
                        try {
                            UsuarioRepository.registrarVisita(lugarId)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            mensajeAccion = error.message ?: getString(Res.string.nav_error_guardar_visita)
                        }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    if (lugarCompleto == null) {
                        errorCarga = error.message ?: getString(Res.string.nav_error_cargar_lugar)
                    }
                } finally {
                    cargando = false
                }
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (cargando) {
                    CircularProgressIndicator()
                } else if (lugarCompleto != null) {
                    DetalleLugarScreen(
                        lugar = lugarCompleto!!,
                        onBackClick = { controladorNavegacion.popBackStack() },
                        favoritoGuardado = favoritoGuardado,
                        favoritoCargando = favoritoCargando,
                        onIrAGuia = { controladorNavegacion.navigate(Pantalla.Guia.ruta) },
                        onVerAtractivoEnMapa = { atractivo ->
                            lugarCompleto?.let { lugar ->
                                val coordsAtractivo = atractivo.ubicacionCoordenadas?.takeIf(::esCoordenadaValida)
                                val coordsLugar = lugar.ubicacionCoordenadas.takeIf(::esCoordenadaValida)
                                val coordsFallback = CoordenadasRuta.obtener(lugar.id)
                                val lat = coordsAtractivo?.latitud ?: coordsLugar?.latitud ?: coordsFallback.latitud
                                val lng = coordsAtractivo?.longitud ?: coordsLugar?.longitud ?: coordsFallback.longitud
                                val zoom = if (coordsAtractivo != null) 16.5f else coordsFallback.zoom
                                controladorNavegacion.navigate(
                                    "${Pantalla.Mapa.ruta}?lat=$lat&lng=$lng&zoom=$zoom"
                                )
                            }
                        },
                        onToggleFavorito = {
                            if (!favoritoCargando) {
                                favoritoCargando = true
                                mensajeAccion = null
                                scope.launch {
                                    try {
                                        UsuarioRepository.cambiarFavorito(lugarId, !favoritoGuardado)
                                    } catch (error: CancellationException) {
                                        throw error
                                    } catch (error: Exception) {
                                        mensajeAccion = error.message ?: getString(Res.string.nav_error_actualizar_favorito)
                                    } finally {
                                        favoritoCargando = false
                                    }
                                }
                            }
                        }
                    )
                    mensajeAccion?.let { mensaje ->
                        Text(
                            text = mensaje,
                            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(errorCarga ?: stringResource(Res.string.nav_lugar_no_encontrado))
                        if (errorCarga != null) {
                            Button(onClick = { intentoCarga++ }) { Text(stringResource(Res.string.btn_reintentar)) }
                        }
                    }
                }
            }
        }

        // 6. Detalle de Experiencia / Comercio
        composable(
            route = Pantalla.DetalleExperiencia.ruta,
            arguments = listOf(navArgument("comercioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val comercioId = backStackEntry.arguments?.getString("comercioId") ?: return@composable
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleExperienciaScreen(
                    comercioId = comercioId,
                    onVolver = { controladorNavegacion.popBackStack() },
                    onVerEnMapa = { lat, lng, zoom ->
                        controladorNavegacion.navigate("${Pantalla.Mapa.ruta}?lat=$lat&lng=$lng&zoom=$zoom") {
                            popUpTo(Pantalla.Mapa.rutaPatron) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}