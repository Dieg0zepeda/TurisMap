package com.turismap.app.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.turismap.app.screens.*

@Composable
fun TurisMapNavHost(
    controladorNavegacion: NavHostController,
    modificador: Modifier = Modifier,
    mostrarBarraInferior: Boolean = true,
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
                    }
                )
            }
        }

        composable(Pantalla.Mapa.ruta) { Box(modifier = Modifier.fillMaxSize()) { MapaScreen(onAbrirDetalle = {}) } }
        composable(Pantalla.Guia.ruta) { Box(modifier = Modifier.fillMaxSize()) { GuiaScreen() } }
        composable(Pantalla.Perfil.ruta) { Box(modifier = Modifier.fillMaxSize()) { PerfilScreen() } }

        // NAVEGACIÓN DINÁMICA DE DETALLE
        composable(
            route = Pantalla.DetalleLugar.ruta,
            arguments = listOf(navArgument("lugarId") { type = NavType.StringType })
        ) { backStackEntry ->
            val lugarId = backStackEntry.arguments?.getString("lugarId") ?: return@composable

            // 1. Estados para manejar la asincronía y el renderizado
            var lugarCompleto by remember { mutableStateOf<Lugar?>(null) }
            var cargando by remember { mutableStateOf(true) }

            // 2. LaunchedEffect crea una corrutina para llamar a funciones suspend
            LaunchedEffect(key1 = lugarId) {
                cargando = true
                lugarCompleto = LugaresRepository.obtenerPorId(lugarId)
                cargando = false
            }

            // 3. Lógica de renderizado condicional
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (cargando) {
                    CircularProgressIndicator() // Muestra un spinner de carga mientras consulta Firebase
                } else if (lugarCompleto != null) {
                    DetalleLugarScreen(
                        lugar = lugarCompleto!!,
                        onBackClick = { controladorNavegacion.popBackStack() }
                    )
                }
            }
        }

        composable(Pantalla.DetalleExperiencia.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleExperienciaScreen(onVolver = { controladorNavegacion.popBackStack() })
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