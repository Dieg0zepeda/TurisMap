package com.turismap.app.screens

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.turismap.app.MapaTuristico
import com.turismap.app.data.models.Lugar
import com.turismap.app.screens.mapa.CabeceraMapa
import com.turismap.app.screens.mapa.ModalCapasMapa
import com.turismap.app.screens.mapa.TarjetaMarcadorMapa
import com.turismap.app.screens.mapa.rememberMapaPaleta
import com.turismap.app.screens.mapa.rememberMapaState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    latInicial: Double? = null,
    lngInicial: Double? = null,
    zoomInicial: Float? = null,
    onAbrirDetalle: (Lugar) -> Unit = {},
    onComercioClick: (String) -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current
    val paleta = rememberMapaPaleta(isSystemInDarkTheme())
    val state = rememberMapaState(latInicial = latInicial, lngInicial = lngInicial)

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val alturaMaximaTarjeta = maxHeight * 0.65f

        // 1. Google Maps / Mapa Turístico con centrado reactivo
        MapaTuristico(
            lugares = state.lugaresParaElMapa,
            latInicial = latInicial,
            lngInicial = lngInicial,
            zoomInicial = zoomInicial,
            onLugarClick = { lugarTocado -> state.lugarSeleccionado = lugarTocado },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Cabecera limpia sobre el mapa (sin icono de notificaciones)
        CabeceraMapa(
            textoBusqueda = state.textoBusqueda,
            filtroTipoMarcadores = state.filtroTipoMarcadores,
            cantidadLugaresEnMapa = state.lugaresParaElMapa.size,
            paleta = paleta,
            onBusquedaChange = state::actualizarBusqueda,
            onLimpiarBusqueda = state::limpiarBusqueda,
            onAbrirMenuCapas = { state.mostrarMenuWaze = true }
        )

        // 3. Modal estilo Waze con la mascota
        if (state.mostrarMenuWaze) {
            ModalCapasMapa(
                filtroTipoMarcadores = state.filtroTipoMarcadores,
                cantidadLugares = state.lugaresFiltrados.size,
                cantidadComercios = state.comerciosFiltrados.size,
                paleta = paleta,
                onSeleccionarFiltro = state::seleccionarFiltro,
                onDismiss = { state.mostrarMenuWaze = false }
            )
        }

        // 4. Tarjeta flotante inferior al seleccionar un marcador
        TarjetaMarcadorMapa(
            lugarSeleccionado = state.lugarSeleccionado,
            comercioSeleccionado = state.comercioSeleccionado,
            municipiosMap = state.municipiosMap,
            alturaMaximaTarjeta = alturaMaximaTarjeta,
            paleta = paleta,
            onCerrar = { state.lugarSeleccionado = null },
            onAbrirGoogleMaps = { url ->
                try {
                    uriHandler.openUri(url)
                } catch (error: Exception) {
                    state.mostrarMensajeError(error.message ?: "No se pudo abrir Google Maps.")
                }
            },
            onAbrirDetalle = onAbrirDetalle,
            onComercioClick = onComercioClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (state.cargando) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = paleta.turismapPrimario
            )
        } else if (state.lugaresParaElMapa.isEmpty()) {
            Column(
                modifier = Modifier.align(Alignment.Center).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.errorCarga ?: "No hay destinos ni comercios con ubicación para mostrar.",
                    color = paleta.airbnbOscuro
                )
                if (state.errorCarga != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = state::reintentarCarga) {
                        Text("Reintentar")
                    }
                }
            }
        }

        SnackbarHost(
            hostState = state.snackbarHostState,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 150.dp)
        )
    }
}
