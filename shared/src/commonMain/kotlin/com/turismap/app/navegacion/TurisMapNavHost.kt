package com.turismap.app.navegacion

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
import com.turismap.app.screens.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TurisMapNavHost(
    controladorNavegacion: NavHostController = rememberNavController(),
    modificador: Modifier = Modifier,
    mostrarBarraInferior: Boolean = true,
    onCerrarSesion: () -> Unit = {},
    destinoInicial: String = Pantalla.Explorar.ruta,
    onSolicitarGoogleSignIn: (((String) -> Unit) -> Unit)? = null
) {
    NavHost(
        navController = controladorNavegacion,
        startDestination = destinoInicial,
        modifier = modificador
    ) {
        // 1. Explorar
        composable(Pantalla.Explorar.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                ExplorarScreen(
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
            route = "${Pantalla.Mapa.ruta}?lat={lat}&lng={lng}&zoom={zoom}",
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
                },
                onIniciarRuta = { _ ->
                    controladorNavegacion.navigate(Pantalla.Mapa.ruta) {
                        popUpTo(Pantalla.Guia.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // 4. Perfil
        composable(Pantalla.Perfil.ruta) {
            PerfilScreen(
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

            var lugarCompleto by remember { mutableStateOf<Lugar?>(null) }
            var cargando by remember { mutableStateOf(true) }
            var errorCarga by remember { mutableStateOf<String?>(null) }
            var intentoCarga by remember { mutableStateOf(0) }
            var favoritoCargando by remember { mutableStateOf(false) }
            var mensajeAccion by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            val usuarioActual by UsuarioRepository.observarUsuarioActual().collectAsState(initial = null)
            val favoritoGuardado = remember(usuarioActual?.favoritos, lugarId) {
                usuarioActual?.favoritos?.contains(lugarId) == true
            }

            LaunchedEffect(lugarId, intentoCarga) {
                cargando = true
                errorCarga = null
                mensajeAccion = null
                try {
                    lugarCompleto = LugaresRepository.obtenerPorId(lugarId)
                    if (lugarCompleto != null) {
                        try {
                            UsuarioRepository.registrarVisita(lugarId)
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            mensajeAccion = error.message ?: "No se pudo guardar esta visita en tu historial."
                        }
                    }
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    errorCarga = error.message ?: "No se pudo cargar este lugar."
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
                        onVerAtractivoEnMapa = { _ ->
                            lugarCompleto?.let { lugar ->
                                val coords = CoordenadasRuta.obtener(lugar.id)
                                controladorNavegacion.navigate(
                                    "${Pantalla.Mapa.ruta}?lat=${coords.latitud}&lng=${coords.longitud}&zoom=${coords.zoom}"
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
                                        mensajeAccion = error.message ?: "No se pudo actualizar el favorito."
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
                        Text(errorCarga ?: "No se encontró el lugar.")
                        if (errorCarga != null) {
                            Button(onClick = { intentoCarga++ }) { Text("Reintentar") }
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
                    onVolver = { controladorNavegacion.popBackStack() }
                )
            }
        }

        // 7. Inicio de Sesión
        composable(Pantalla.InicioSesion.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                LoginScreen(
                    onLoginExitoso = {
                        controladorNavegacion.navigate(Pantalla.Explorar.ruta) {
                            popUpTo(Pantalla.InicioSesion.ruta) { inclusive = true }
                        }
                    },
                    onSolicitarGoogleSignIn = onSolicitarGoogleSignIn
                )
            }
        }
    }
}