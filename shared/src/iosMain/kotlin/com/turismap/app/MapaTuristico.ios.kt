package com.turismap.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.turismap.app.screens.mapa.MarcadorMapa

@Composable
actual fun MapaTuristico(
    marcadores: List<MarcadorMapa>,
    latInicial: Double?,
    lngInicial: Double?,
    zoomInicial: Float?,
    camaraGuardada: EstadoCamaraMapa?,
    onCamaraCambiada: (EstadoCamaraMapa) -> Unit,
    onMarcadorClick: (MarcadorMapa) -> Unit,
    modifier: Modifier
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Mapa iOS (MapKit pendiente)")
    }
}
