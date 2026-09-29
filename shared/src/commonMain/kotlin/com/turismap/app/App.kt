package com.turismap.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.turismap.app.components.FigmaBottomBar
import com.turismap.app.navegacion.Pantalla
import com.turismap.app.navegacion.TurisMapNavHost

@Composable
fun App() {
    val controladorNavegacion = rememberNavController()

    // Observador reactivo del estado de navegación
    val navBackStackEntry by controladorNavegacion.currentBackStackEntryAsState()
    val rutaActual = navBackStackEntry?.destination?.route

    // Rutas con visibilidad de barra inferior
    val rutasConBarra = listOf(
        Pantalla.Explorar.ruta,
        Pantalla.Mapa.ruta,
        Pantalla.Guia.ruta,
        Pantalla.Perfil.ruta
    )
    val mostrarBarraInferior = rutaActual == null || rutaActual in rutasConBarra

    // Sincronización del índice activo según la ruta actual de la pila
    val indiceSeleccionado = when (rutaActual) {
        Pantalla.Explorar.ruta -> 0
        Pantalla.Mapa.ruta -> 1
        Pantalla.Guia.ruta -> 2
        Pantalla.Perfil.ruta -> 3
        else -> 0
    }

    MaterialTheme {
        Scaffold(
            bottomBar = {
                if (mostrarBarraInferior) {
                    FigmaBottomBar(
                        selectedIndex = indiceSeleccionado,
                        onTabSelected = { indice ->
                            val destino = when (indice) {
                                0 -> Pantalla.Explorar.ruta
                                1 -> Pantalla.Mapa.ruta
                                2 -> Pantalla.Guia.ruta
                                3 -> Pantalla.Perfil.ruta
                                else -> Pantalla.Explorar.ruta
                            }

                            controladorNavegacion.navigate(destino) {
                                // Usamos el 'route' del destino inicial, o hacemos fallback a la ruta de Explorar
                                val rutaInicio = controladorNavegacion.graph.findStartDestination().route ?: Pantalla.Explorar.ruta

                                popUpTo(route = rutaInicio) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            TurisMapNavHost(
                controladorNavegacion = controladorNavegacion,
                modificador = Modifier.padding(paddingValues),
                mostrarBarraInferior = mostrarBarraInferior
            )
        }
    }
}