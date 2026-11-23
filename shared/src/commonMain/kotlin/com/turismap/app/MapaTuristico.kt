package com.turismap.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.turismap.app.data.models.Lugar
import kotlinx.coroutines.flow.MutableStateFlow

data class EstadoCamaraMapa(
    val latitud: Double,
    val longitud: Double,
    val zoom: Float,
    val tilt: Float = 0f,
    val bearing: Float = 0f
)

/**
 * Preserva en memoria la última posición de cámara del mapa durante la sesión del usuario activo,
 * evitando reinicios al cambiar entre pestañas y limpiándose al cerrar sesión o cambiar de UID.
 */
internal object MapaCamaraCache {
    private val estadoFlow = MutableStateFlow<Pair<String?, EstadoCamaraMapa>?>(null)

    fun obtener(uidActual: String?): EstadoCamaraMapa? {
        val actual = estadoFlow.value ?: return null
        return if (actual.first == uidActual) actual.second else null
    }

    fun guardar(uidActual: String?, estado: EstadoCamaraMapa) {
        estadoFlow.value = uidActual to estado
    }

    fun limpiar() {
        estadoFlow.value = null
    }
}

@Composable
expect fun MapaTuristico(
    lugares: List<Lugar>,
    latInicial: Double? = null,
    lngInicial: Double? = null,
    zoomInicial: Float? = null,
    camaraGuardada: EstadoCamaraMapa? = null,
    onCamaraCambiada: (EstadoCamaraMapa) -> Unit = {},
    onLugarClick: (Lugar) -> Unit,
    modifier: Modifier = Modifier
)