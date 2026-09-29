package com.turismap.app.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
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
            val lugarCompleto = LugaresRepository.obtenerPorId(lugarId)

            if (lugarCompleto != null) {
                Box(modifier = Modifier.fillMaxSize()) {
                    DetalleLugarScreen(
                        lugar = lugarCompleto,
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