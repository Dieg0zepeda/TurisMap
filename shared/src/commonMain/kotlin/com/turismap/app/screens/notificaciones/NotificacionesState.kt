package com.turismap.app.screens.notificaciones

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.turismap.app.data.models.Notificacion
import com.turismap.app.data.models.contarNotificacionesNoLeidas
import com.turismap.app.data.repository.NotificacionesRepository
import com.turismap.app.data.repository.UsuarioRepository
import com.turismap.app.theme.FondoBlancoAdaptable
import com.turismap.app.theme.GrisBorde
import com.turismap.app.theme.GrisFondo
import com.turismap.app.theme.GrisTextoSecundario
import com.turismap.app.theme.TerracotaAcento
import com.turismap.app.theme.TerracotaTextoAdaptable
import com.turismap.app.theme.TextoPrincipalOscuro
import com.turismap.app.theme.VerdePrimario
import com.turismap.app.theme.VerdeTextoAdaptable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import turismap.shared.generated.resources.Res
import turismap.shared.generated.resources.notificaciones_error_cargar
import turismap.shared.generated.resources.notificaciones_error_marcar_leida
import turismap.shared.generated.resources.notificaciones_error_marcar_todas
import turismap.shared.generated.resources.notificaciones_error_sesion
import turismap.shared.generated.resources.notificaciones_todas_marcadas

internal data class NotificacionesPaleta(
    val fondoPantalla: Color,
    val fondoTarjeta: Color,
    val fondoTarjetaNoLeida: Color,
    val bordeSuave: Color,
    val bordeNoLeida: Color,
    val textoPrincipal: Color,
    val textoSecundario: Color,
    val verdeAcento: Color,
    val verdeBoton: Color,
    val terracotaAcento: Color,
    val terracotaTexto: Color
)

@Composable
internal fun rememberNotificacionesPaleta(): NotificacionesPaleta {
    val esOscuro = isSystemInDarkTheme()
    val fondoPantalla = GrisFondo
    val fondoTarjeta = FondoBlancoAdaptable
    val bordeSuave = GrisBorde
    val textoPrincipal = TextoPrincipalOscuro
    val textoSecundario = GrisTextoSecundario
    val verdeAcento = VerdeTextoAdaptable
    val terracotaTexto = TerracotaTextoAdaptable

    return remember(
        esOscuro,
        fondoPantalla,
        fondoTarjeta,
        bordeSuave,
        textoPrincipal,
        textoSecundario,
        verdeAcento,
        terracotaTexto
    ) {
        NotificacionesPaleta(
            fondoPantalla = fondoPantalla,
            fondoTarjeta = fondoTarjeta,
            fondoTarjetaNoLeida = if (esOscuro) Color(0xFF1F2D27) else Color(0xFFF0F7F4),
            bordeSuave = bordeSuave,
            bordeNoLeida = if (esOscuro) Color(0xFF4CAF50).copy(alpha = 0.45f) else VerdePrimario.copy(alpha = 0.35f),
            textoPrincipal = textoPrincipal,
            textoSecundario = textoSecundario,
            verdeAcento = verdeAcento,
            verdeBoton = VerdePrimario,
            terracotaAcento = TerracotaAcento,
            terracotaTexto = terracotaTexto
        )
    }
}

internal class NotificacionesState(
    private val coroutineScope: CoroutineScope,
    val snackbarHostState: SnackbarHostState
) {
    var notificaciones by mutableStateOf<List<Notificacion>>(emptyList())
    var cargando by mutableStateOf(true)
    var errorCarga by mutableStateOf<String?>(null)
    var intentoCarga by mutableIntStateOf(0)
    var marcandoTodas by mutableStateOf(false)
    var idsProcesando by mutableStateOf<Set<String>>(emptySet())

    val cantidadNoLeidas: Int by derivedStateOf {
        contarNotificacionesNoLeidas(notificaciones)
    }

    val idsNoLeidas: List<String> by derivedStateOf {
        notificaciones
            .filter { !it.leida && it.id.isNotBlank() }
            .map(Notificacion::id)
    }

    fun reintentarCarga() {
        intentoCarga++
    }

    fun marcarComoLeida(notificacion: Notificacion) {
        val id = notificacion.id.trim()
        if (notificacion.leida || id.isEmpty() || marcandoTodas || idsProcesando.contains(id)) {
            return
        }

        idsProcesando = idsProcesando + id
        coroutineScope.launch {
            try {
                NotificacionesRepository.marcarComoLeida(id)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mensaje = error.message?.takeIf(String::isNotBlank)
                    ?: getString(Res.string.notificaciones_error_marcar_leida)
                snackbarHostState.showSnackbar(mensaje)
            } finally {
                idsProcesando = idsProcesando - id
            }
        }
    }

    fun marcarTodasComoLeidas() {
        val pendientes = idsNoLeidas
        if (marcandoTodas || pendientes.isEmpty()) return

        marcandoTodas = true
        coroutineScope.launch {
            try {
                NotificacionesRepository.marcarTodasComoLeidas(pendientes)
                snackbarHostState.showSnackbar(getString(Res.string.notificaciones_todas_marcadas))
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                val mensaje = error.message?.takeIf(String::isNotBlank)
                    ?: getString(Res.string.notificaciones_error_marcar_todas)
                snackbarHostState.showSnackbar(mensaje)
            } finally {
                marcandoTodas = false
            }
        }
    }
}

@Composable
internal fun rememberNotificacionesState(): NotificacionesState {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val state = remember(coroutineScope, snackbarHostState) {
        NotificacionesState(
            coroutineScope = coroutineScope,
            snackbarHostState = snackbarHostState
        )
    }

    val uidActual = UsuarioRepository.obtenerIdUsuarioActual()

    LaunchedEffect(uidActual, state.intentoCarga) {
        state.cargando = true
        state.errorCarga = null

        if (uidActual.isNullOrBlank()) {
            state.notificaciones = emptyList()
            state.errorCarga = getString(Res.string.notificaciones_error_sesion)
            state.cargando = false
            return@LaunchedEffect
        }

        try {
            NotificacionesRepository.observarNotificaciones(uidActual).collect { lista ->
                state.notificaciones = lista
                state.cargando = false
                state.errorCarga = null
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            state.errorCarga = error.message?.takeIf(String::isNotBlank)
                ?: getString(Res.string.notificaciones_error_cargar)
            state.cargando = false
        }
    }

    return state
}
