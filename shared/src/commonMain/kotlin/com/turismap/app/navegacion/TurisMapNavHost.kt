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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.turismap.app.data.models.Lugar
import com.turismap.app.data.repository.LugaresRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.screens.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun TurisMapNavHost(
    controladorNavegacion: NavHostController,
    modificador: Modifier = Modifier,
    mostrarBarraInferior: Boolean = true,
    onCerrarSesion: () -> Unit = {},
    destinoInicial: String = Pantalla.Explorar.ruta
) {
    val modificadorConPadding = if (mostrarBarraInferior) {
        modificador.padding(bottom = 68.dp)
    } else {
        modificador
    }

    NavHost(
        navController = controladorNavegacion,
        startDestination = destinoInicial,
        modifier = modificadorConPadding
    ) {
        // Tabs principales
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

        composable(Pantalla.Mapa.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapaScreen(
                    onAbrirDetalle = { lugar ->
                        controladorNavegacion.navigate(Pantalla.DetalleLugar.crearRuta(lugar.id))
                    }
                )
            }
        }
        composable(Pantalla.Guia.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                GuiaScreen(onComercioClick = { comercioId ->
                    controladorNavegacion.navigate(Pantalla.DetalleExperiencia.crearRuta(comercioId))
                })
            }
        }
        composable(Pantalla.Perfil.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                PerfilScreen(onCerrarSesionClick = onCerrarSesion)
            }
        }

        // NAVEGACIÓN DINÁMICA DE DETALLE
        composable(
            route = Pantalla.DetalleLugar.ruta,
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: return@composable

            // 1. Estados para manejar la asincronía y el renderizado
            var lugarCompleto by remember { mutableStateOf<Lugar?>(null) }
            var cargando by remember { mutableStateOf(true) }
            var errorCarga by remember { mutableStateOf<String?>(null) }
            var intentoCarga by remember { mutableStateOf(0) }
            var favoritoGuardado by remember { mutableStateOf(false) }
            var favoritoCargando by remember { mutableStateOf(false) }
            var mensajeAccion by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            LaunchedEffect(lugarId, intentoCarga) {
                cargando = true
                errorCarga = null
                mensajeAccion = null
                try {
                    lugarCompleto = LugaresRepository.obtenerPorId(lugarId)
                    if (lugarCompleto != null) {
                        try {
                            val uid = UsuarioRepository.obtenerIdUsuarioActual()
                                ?: throw IllegalStateException("No hay una sesión autenticada.")
                            val perfil = UsuarioRepository.obtenerUsuarioPorId(uid)
                            favoritoGuardado = perfil?.favoritos?.contains(lugarId) == true
                        } catch (error: CancellationException) {
                            throw error
                        } catch (error: Exception) {
                            mensajeAccion = error.message ?: "No se pudieron consultar tus favoritos."
                        }
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
                        onToggleFavorito = {
                            if (!favoritoCargando) {
                                favoritoCargando = true
                                mensajeAccion = null
                                scope.launch {
                                    try {
                                        val nuevoEstado = !favoritoGuardado
                                        UsuarioRepository.cambiarFavorito(lugarId, nuevoEstado)
                                        favoritoGuardado = nuevoEstado
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
                            Button(onClick = { intentoCarga++ }) {
                                Text("Reintentar")
                            }
                        }
                    }
                }
            }
        }

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

        composable(Pantalla.InicioSesion.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                PantallaInicioSesion(
                    onLoginExitoso = {
                        controladorNavegacion.navigate(Pantalla.Explorar.ruta) {
                            popUpTo(Pantalla.InicioSesion.ruta) { inclusive = true }
                        }
                    },
                    onNavegarARegistro = {}
                )
            }
        }
    }
}