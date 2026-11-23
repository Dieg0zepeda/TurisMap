package com.turismap.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.turismap.app.components.FigmaBottomBar
import com.turismap.app.data.repository.AutenticacionRepository
import com.turismap.app.data.repository.NotificacionesRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.navegacion.Pantalla
import com.turismap.app.navegacion.TurisMapNavHost
import com.turismap.app.screens.LoginScreen
import com.turismap.app.screens.perfil.PerfilCacheSesion
import com.turismap.app.theme.BosqueContenedorPrimario
import com.turismap.app.theme.BosqueSobreContenedorPrimario
import com.turismap.app.theme.BosqueSobrePrimario
import com.turismap.app.theme.ObsidianaFondo
import com.turismap.app.theme.PinoBorde
import com.turismap.app.theme.PinoContenedor
import com.turismap.app.theme.PinoContenedorAlto
import com.turismap.app.theme.PinoSuperficie
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.TextoClaroBosque
import com.turismap.app.theme.TextoSecundarioBosque
import com.turismap.app.theme.VerdeBosqueMarca
import com.turismap.app.theme.VerdeSalviaOscuro
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

private val LightColorScheme = lightColorScheme(
    primary = VerdeBosqueMarca,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2EA),
    onPrimaryContainer = VerdeBosqueMarca,
    secondary = TerracotaAcento,
    onSecondary = Color.White,
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF333333),
    surface = Color.White,
    onSurface = Color(0xFF333333),
    surfaceVariant = Color(0xFFF0F4F1),
    onSurfaceVariant = Color.Gray,
    surfaceContainerLow = Color.White,
    surfaceContainer = Color(0xFFF6F8F7),
    surfaceContainerHigh = Color(0xFFEEF3F0),
    outlineVariant = Color(0xFFE2E8F0)
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeSalviaOscuro,
    onPrimary = BosqueSobrePrimario,
    primaryContainer = BosqueContenedorPrimario,
    onPrimaryContainer = BosqueSobreContenedorPrimario,
    secondary = TerracotaAcento,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3D1F18),
    onSecondaryContainer = Color(0xFFFFDAD2),
    background = ObsidianaFondo,
    onBackground = TextoClaroBosque,
    surface = PinoSuperficie,
    onSurface = TextoClaroBosque,
    surfaceVariant = PinoContenedor,
    onSurfaceVariant = TextoSecundarioBosque,
    surfaceContainerLowest = Color(0xFF0A0F0C),
    surfaceContainerLow = PinoSuperficie,
    surfaceContainer = PinoContenedor,
    surfaceContainerHigh = PinoContenedorAlto,
    surfaceContainerHighest = PinoBorde,
    outline = Color(0xFF34483D),
    outlineVariant = PinoBorde
)

@Composable
fun App(
    onSolicitarGoogleSignIn: (((String?) -> Unit) -> Unit)? = null
) {
    val esModoOscuro = isSystemInDarkTheme()
    val usuarioAuth by remember {
        Firebase.auth.authStateChanged
    }.collectAsState(initial = AutenticacionRepository.usuarioActual)
    val uidAutenticado = usuarioAuth?.uid
    val usuarioAutenticado = uidAutenticado != null
    val cantidadNotificacionesNoLeidas by NotificacionesRepository.cantidadNoLeidasFlow.collectAsState()
    var ultimoUidObservado by remember { mutableStateOf(uidAutenticado) }

    // Limpia cachés específicas de usuario al cerrar sesión o cambiar de cuenta (UID)
    LaunchedEffect(uidAutenticado) {
        if (uidAutenticado != ultimoUidObservado) {
            UsuarioRepository.limpiarCacheUsuario()
            PerfilCacheSesion.limpiar()
            MapaCamaraCache.limpiar()
            ultimoUidObservado = uidAutenticado
        }
    }

    MaterialTheme(
        colorScheme = if (esModoOscuro) DarkColorScheme else LightColorScheme
    ) {
        if (!usuarioAutenticado) {
            LoginScreen(
                onLoginExitoso = { /* authStateChanged actualiza el estado reactivamente */ },
                onSolicitarGoogleSignIn = onSolicitarGoogleSignIn
            )
        } else {
            key(uidAutenticado) {
                val controladorNavegacion = rememberNavController()
                val navBackStackEntry by controladorNavegacion.currentBackStackEntryAsState()
                val rutaActual = navBackStackEntry?.destination?.route

                // Comprueba si la ruta actual coincide o inicia con alguna de las pantallas principales
                val mostrarBarraInferior = rutaActual == null || listOf(
                    Pantalla.Explorar.ruta,
                    Pantalla.Mapa.ruta,
                    Pantalla.Guia.ruta,
                    Pantalla.Perfil.ruta
                ).any { rutaBase -> rutaActual.startsWith(rutaBase) }

                // Sincronización del índice activo reconociendo rutas con argumentos
                val indiceSeleccionado = when {
                    rutaActual?.startsWith(Pantalla.Explorar.ruta) == true -> 0
                    rutaActual?.startsWith(Pantalla.Mapa.ruta) == true -> 1
                    rutaActual?.startsWith(Pantalla.Guia.ruta) == true -> 2
                    rutaActual?.startsWith(Pantalla.Perfil.ruta) == true -> 3
                    else -> 0
                }

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
                                    val rutaPatronDestino = if (indice == 1) {
                                        Pantalla.Mapa.rutaPatron
                                    } else {
                                        destino
                                    }

                                    val rutaActualNav = controladorNavegacion.currentDestination?.route
                                    val yaEstaEnPestana = rutaActualNav == rutaPatronDestino ||
                                        rutaActualNav?.startsWith(destino) == true

                                    if (!yaEstaEnPestana) {
                                        controladorNavegacion.navigate(destino) {
                                            popUpTo(Pantalla.Explorar.ruta) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    TurisMapNavHost(
                        controladorNavegacion = controladorNavegacion,
                        modificador = Modifier.padding(paddingValues),
                        cantidadNotificacionesNoLeidas = cantidadNotificacionesNoLeidas,
                        onCerrarSesion = { /* authStateChanged cierra la sesión reactivamente */ }
                    )
                }
            }
        }
    }
}