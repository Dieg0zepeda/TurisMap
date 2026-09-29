package com.turismap.app.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
                    onPuebloClick = { pueblo ->
                        when (pueblo.nombre) {
                            "Concepción de Ataco", "Ataco" -> controladorNavegacion.navigate(Pantalla.DetalleAtaco.ruta)
                            "Apaneca" -> controladorNavegacion.navigate(Pantalla.DetalleApaneca.ruta)
                            "Juayúa" -> controladorNavegacion.navigate(Pantalla.DetalleJuayua.ruta)
                            "Salcoatitán" -> controladorNavegacion.navigate(Pantalla.DetalleSalcoatitan.ruta)
                            "Nahuizalco" -> controladorNavegacion.navigate(Pantalla.DetalleNahuizalco.ruta)
                        }
                    }
                )
            }
        }
        
        composable(Pantalla.Mapa.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapaScreen(onAbrirDetalle = {})
            }
        }
        
        composable(Pantalla.Guia.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                GuiaScreen()
            }
        }
        
        composable(Pantalla.Perfil.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                PerfilScreen()
            }
        }
        
        // Screens de detalles de pueblos
        composable(Pantalla.DetalleAtaco.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleAtacoScreen(
                    onBackClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        composable(Pantalla.DetalleApaneca.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleApanecaScreen(
                    onBackClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        composable(Pantalla.DetalleJuayua.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleJuayuaScreen(
                    onBackClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        composable(Pantalla.DetalleSalcoatitan.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleSalcoatitanScreen(
                    onBackClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        composable(Pantalla.DetalleNahuizalco.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleNahuizalcoScreen(
                    onBackClick = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        // Screen de detalle de experiencia
        composable(Pantalla.DetalleExperiencia.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                DetalleExperienciaScreen(
                    onVolver = { controladorNavegacion.popBackStack() }
                )
            }
        }
        
        // Screen de inicio de sesión
        composable(Pantalla.InicioSesion.ruta) {
            Box(modifier = Modifier.fillMaxSize()) {
                PantallaInicioSesion(
                    onLoginExitoso = {
                        // TODO: PT2026-29 - Navegación condicional basada en auth
                        controladorNavegacion.navigate(Pantalla.Explorar.ruta) {
                            popUpTo(Pantalla.InicioSesion.ruta) { inclusive = true }
                        }
                    },
                    onNavegarARegistro = {
                        // TODO: Implementar RegisterScreen
                    }
                )
            }
        }
    }
}
